import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { User } from '../../types';

interface HeaderProps {
  user: User | null;
  onLogout: () => void;
}

const Header: React.FC<HeaderProps> = ({ user, onLogout }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <header className="bg-blue-600 text-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center py-4">
          {/* Logo */}
          <Link to="/" className="text-2xl font-bold">
            📚 Library Management
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex space-x-6">
            <Link
              to="/books"
              className={`hover:text-blue-200 transition-colors ${
                isActive('/books') ? 'text-blue-200 border-b-2 border-blue-200' : ''
              }`}
            >
              Books
            </Link>
            <Link
              to="/authors"
              className={`hover:text-blue-200 transition-colors ${
                isActive('/authors') ? 'text-blue-200 border-b-2 border-blue-200' : ''
              }`}
            >
              Authors
            </Link>
            <Link
              to="/loans"
              className={`hover:text-blue-200 transition-colors ${
                isActive('/loans') ? 'text-blue-200 border-b-2 border-blue-200' : ''
              }`}
            >
              Loans
            </Link>
            <Link
              to="/recommendations"
              className={`hover:text-blue-200 transition-colors ${
                isActive('/recommendations') ? 'text-blue-200 border-b-2 border-blue-200' : ''
              }`}
            >
              Recommendations
            </Link>
            {user?.roles.includes('ADMIN' as any) && (
              <Link
                to="/admin"
                className={`hover:text-blue-200 transition-colors ${
                  isActive('/admin') ? 'text-blue-200 border-b-2 border-blue-200' : ''
                }`}
              >
                Admin
              </Link>
            )}
          </nav>

          {/* User Menu */}
          <div className="flex items-center space-x-4">
            {user ? (
              <div className="relative">
                <button
                  onClick={() => setIsMenuOpen(!isMenuOpen)}
                  className="flex items-center space-x-2 hover:text-blue-200 transition-colors"
                >
                  <span>👤</span>
                  <span>{user.username}</span>
                  <span className="text-xs">▼</span>
                </button>
                
                {isMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white text-gray-800 rounded-md shadow-lg z-50">
                    <div className="py-1">
                      <Link
                        to="/profile"
                        className="block px-4 py-2 hover:bg-gray-100"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        Profile
                      </Link>
                      <Link
                        to="/my-loans"
                        className="block px-4 py-2 hover:bg-gray-100"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        My Loans
                      </Link>
                      <button
                        onClick={handleLogout}
                        className="block w-full text-left px-4 py-2 hover:bg-gray-100"
                      >
                        Logout
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="space-x-2">
                <Link
                  to="/login"
                  className="bg-blue-500 hover:bg-blue-400 px-4 py-2 rounded transition-colors"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="bg-green-500 hover:bg-green-400 px-4 py-2 rounded transition-colors"
                >
                  Register
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            className="md:hidden"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            ☰
          </button>
        </div>

        {/* Mobile Navigation */}
        {isMenuOpen && (
          <nav className="md:hidden pb-4">
            <div className="flex flex-col space-y-2">
              <Link to="/books" className="hover:text-blue-200 transition-colors">
                Books
              </Link>
              <Link to="/authors" className="hover:text-blue-200 transition-colors">
                Authors
              </Link>
              <Link to="/loans" className="hover:text-blue-200 transition-colors">
                Loans
              </Link>
              <Link to="/recommendations" className="hover:text-blue-200 transition-colors">
                Recommendations
              </Link>
              {user?.roles.includes('ADMIN' as any) && (
                <Link to="/admin" className="hover:text-blue-200 transition-colors">
                  Admin
                </Link>
              )}
            </div>
          </nav>
        )}
      </div>
    </header>
  );
};

export default Header;