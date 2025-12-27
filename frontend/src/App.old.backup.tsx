import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { User, JwtResponse } from './types';
import { authAPI } from './services/api';
import Layout from './components/Layout/Layout';
import LoadingSpinner from './components/Common/LoadingSpinner';

// Pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BooksPage from './pages/BooksPage';
import BookDetailPage from './pages/BookDetailPage';
import AuthorsPage from './pages/AuthorsPage';
import AuthorDetailPage from './pages/AuthorDetailPage';
import LoansPage from './pages/LoansPage';
import RecommendationsPage from './pages/RecommendationsPage';
import AdminPage from './pages/AdminPage';
import ProfilePage from './pages/ProfilePage';
import MyLoansPage from './pages/MyLoansPage';

// Auth Context
interface AuthContextType {
  user: User | null;
  login: (credentials: { username: string; password: string }) => Promise<void>;
  logout: () => void;
  loading: boolean;
}

export const AuthContext = React.createContext<AuthContextType>({
  user: null,
  login: async () => {},
  logout: () => {},
  loading: false
});

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    if (token) {
      // Verify token and get user info
      authAPI.getProfile()
        .then(response => {
          setUser(response.data);
        })
        .catch(() => {
          // Token is invalid, remove it
          localStorage.removeItem('token');
        })
        .finally(() => {
          setLoading(false);
        });
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (credentials: { username: string; password: string }) => {
    try {
      const response = await authAPI.login(credentials);
      const jwtResponse: JwtResponse = response.data;
      
      localStorage.setItem('token', jwtResponse.token);
      
      // Get user profile
      const profileResponse = await authAPI.getProfile();
      setUser(profileResponse.data);
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="large" message="Loading application..." />
      </div>
    );
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      <Router>
        <Layout user={user} onLogout={logout}>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={user ? <Navigate to="/" /> : <LoginPage />} />
            <Route path="/register" element={user ? <Navigate to="/" /> : <RegisterPage />} />
            
            {/* Protected Routes */}
            <Route path="/books" element={<BooksPage />} />
            <Route path="/books/:id" element={<BookDetailPage />} />
            <Route path="/authors" element={<AuthorsPage />} />
            <Route path="/authors/:id" element={<AuthorDetailPage />} />
            <Route path="/loans" element={<LoansPage />} />
            <Route path="/recommendations" element={<RecommendationsPage />} />
            
            {/* User-specific routes */}
            <Route 
              path="/profile" 
              element={user ? <ProfilePage /> : <Navigate to="/login" />} 
            />
            <Route 
              path="/my-loans" 
              element={user ? <MyLoansPage /> : <Navigate to="/login" />} 
            />
            
            {/* Admin Routes */}
            <Route 
              path="/admin" 
              element={
                user?.roles.includes('ADMIN' as any) ? 
                <AdminPage /> : 
                <Navigate to="/" />
              } 
            />
            
            {/* Catch all route */}
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Layout>
      </Router>
    </AuthContext.Provider>
  );
}

export default App;