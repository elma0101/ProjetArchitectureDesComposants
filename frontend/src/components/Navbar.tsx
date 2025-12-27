import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
    setMobileMenuOpen(false);
  };

  return (
    <nav className="bg-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <span className="text-2xl">📚</span>
            <span className="text-xl font-bold text-gray-900">Library System</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center space-x-6">
            <Link to="/books" className="text-gray-700 hover:text-blue-600 transition">
              Books
            </Link>
            {isAuthenticated && (
              <>
                <Link to="/my-loans" className="text-gray-700 hover:text-blue-600 transition">
                  My Loans
                </Link>
                <Link to="/recommendations" className="text-gray-700 hover:text-blue-600 transition">
                  Recommendations
                </Link>
              </>
            )}
          </div>

          {/* User Menu */}
          <div className="hidden md:flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                <span className="text-gray-700">
                  Hello, {user?.username}
                </span>
                <button
                  onClick={handleLogout}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="px-4 py-2 text-gray-700 hover:text-blue-600 transition"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                  Register
                </Link>
              </>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 rounded-lg hover:bg-gray-100"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              {mobileMenuOpen ? (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              ) : (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              )}
            </svg>
          </button>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden py-4 border-t">
            <div className="flex flex-col space-y-3">
              <Link
                to="/books"
                className="text-gray-700 hover:text-blue-600 transition px-2 py-1"
                onClick={() => setMobileMenuOpen(false)}
              >
                Books
              </Link>
              {isAuthenticated && (
                <>
                  <Link
                    to="/my-loans"
                    className="text-gray-700 hover:text-blue-600 transition px-2 py-1"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    My Loans
                  </Link>
                  <Link
                    to="/recommendations"
                    className="text-gray-700 hover:text-blue-600 transition px-2 py-1"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Recommendations
                  </Link>
                </>
              )}
              <div className="border-t pt-3">
                {isAuthenticated ? (
                  <>
                    <p className="text-gray-700 px-2 py-1 mb-2">
                      Hello, {user?.username}
                    </p>
                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-2 py-1 text-red-600 hover:bg-red-50 rounded"
                    >
                      Logout
                    </button>
                  </>
                ) : (
                  <>
                    <Link
                      to="/login"
                      className="block px-2 py-1 text-gray-700 hover:text-blue-600 transition mb-2"
                      onClick={() => setMobileMenuOpen(false)}
                    >
                      Login
                    </Link>
                    <Link
                      to="/register"
                      className="block px-2 py-1 text-blue-600 hover:text-blue-700 transition"
                      onClick={() => setMobileMenuOpen(false)}
                    >
                      Register
                    </Link>
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
