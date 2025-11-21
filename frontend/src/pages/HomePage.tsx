import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Book, Loan, BookStatistics, LoanStatistics } from '../types';
import { booksAPI, loansAPI } from '../services/api';
import LoadingSpinner from '../components/Common/LoadingSpinner';
import BookCard from '../components/Books/BookCard';

const HomePage: React.FC = () => {
  const [popularBooks, setPopularBooks] = useState<Book[]>([]);
  const [recentBooks, setRecentBooks] = useState<Book[]>([]);
  const [dueSoonLoans, setDueSoonLoans] = useState<Loan[]>([]);
  const [bookStats, setBookStats] = useState<BookStatistics | null>(null);
  const [loanStats, setLoanStats] = useState<LoanStatistics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Fetch data in parallel
      const [
        popularResponse,
        recentResponse,
        dueSoonResponse,
        bookStatsResponse,
        loanStatsResponse
      ] = await Promise.allSettled([
        booksAPI.getPopular(0, 8),
        booksAPI.getRecent(0, 8),
        loansAPI.getDueWithin(7),
        booksAPI.getStatistics(),
        loansAPI.getStatistics()
      ]);

      if (popularResponse.status === 'fulfilled') {
        setPopularBooks(popularResponse.value.data.content);
      }
      
      if (recentResponse.status === 'fulfilled') {
        setRecentBooks(recentResponse.value.data.content);
      }
      
      if (dueSoonResponse.status === 'fulfilled') {
        setDueSoonLoans(dueSoonResponse.value.data);
      }
      
      if (bookStatsResponse.status === 'fulfilled') {
        setBookStats(bookStatsResponse.value.data);
      }
      
      if (loanStatsResponse.status === 'fulfilled') {
        setLoanStats(loanStatsResponse.value.data);
      }
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading dashboard..." />;
  }

  return (
    <div className="space-y-8">
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg p-8 text-center">
        <h1 className="text-4xl font-bold mb-4">📚 Welcome to Library Management System</h1>
        <p className="text-xl mb-6">Discover, borrow, and manage your favorite books</p>
        <div className="flex justify-center space-x-4">
          <Link
            to="/books"
            className="bg-white text-blue-600 px-6 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
          >
            Browse Books
          </Link>
          <Link
            to="/recommendations"
            className="bg-blue-500 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-400 transition-colors"
          >
            Get Recommendations
          </Link>
        </div>
      </div>

      {/* Statistics Cards */}
      {(bookStats || loanStats) && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {bookStats && (
            <>
              <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center">
                  <div className="text-3xl text-blue-600 mr-4">📚</div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Total Books</p>
                    <p className="text-2xl font-bold text-gray-900">{bookStats.totalBooks}</p>
                  </div>
                </div>
              </div>
              
              <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center">
                  <div className="text-3xl text-green-600 mr-4">✅</div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Available Books</p>
                    <p className="text-2xl font-bold text-gray-900">{bookStats.availableBooks}</p>
                  </div>
                </div>
              </div>
            </>
          )}
          
          {loanStats && (
            <>
              <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center">
                  <div className="text-3xl text-orange-600 mr-4">🔄</div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Active Loans</p>
                    <p className="text-2xl font-bold text-gray-900">{loanStats.activeLoans}</p>
                  </div>
                </div>
              </div>
              
              <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center">
                  <div className="text-3xl text-red-600 mr-4">⚠️</div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Overdue Loans</p>
                    <p className="text-2xl font-bold text-gray-900">{loanStats.overdueLoans}</p>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      {/* Due Soon Alerts */}
      {dueSoonLoans.length > 0 && (
        <div className="bg-orange-50 border border-orange-200 rounded-lg p-6">
          <h2 className="text-xl font-bold text-orange-800 mb-4">⚠️ Books Due Soon</h2>
          <div className="space-y-2">
            {dueSoonLoans.slice(0, 5).map((loan) => (
              <div key={loan.id} className="flex justify-between items-center bg-white p-3 rounded">
                <div>
                  <span className="font-medium">{loan.book.title}</span>
                  <span className="text-sm text-gray-600 ml-2">- {loan.borrowerName}</span>
                </div>
                <div className="text-sm text-orange-600">
                  Due: {new Date(loan.dueDate).toLocaleDateString()}
                </div>
              </div>
            ))}
          </div>
          {dueSoonLoans.length > 5 && (
            <Link
              to="/loans"
              className="text-orange-600 hover:text-orange-800 text-sm font-medium mt-2 inline-block"
            >
              View all {dueSoonLoans.length} loans due soon →
            </Link>
          )}
        </div>
      )}

      {/* Popular Books */}
      {popularBooks.length > 0 && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">🔥 Popular Books</h2>
            <Link
              to="/books?sort=popular"
              className="text-blue-600 hover:text-blue-800 font-medium"
            >
              View all →
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {popularBooks.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        </div>
      )}

      {/* Recent Books */}
      {recentBooks.length > 0 && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">🆕 Recently Added</h2>
            <Link
              to="/books?sort=recent"
              className="text-blue-600 hover:text-blue-800 font-medium"
            >
              View all →
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {recentBooks.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link
            to="/books"
            className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="text-2xl mr-3">🔍</div>
            <div>
              <h3 className="font-semibold">Search Books</h3>
              <p className="text-sm text-gray-600">Find books by title, author, or genre</p>
            </div>
          </Link>
          
          <Link
            to="/loans"
            className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="text-2xl mr-3">📋</div>
            <div>
              <h3 className="font-semibold">Manage Loans</h3>
              <p className="text-sm text-gray-600">View and manage book loans</p>
            </div>
          </Link>
          
          <Link
            to="/authors"
            className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="text-2xl mr-3">👥</div>
            <div>
              <h3 className="font-semibold">Browse Authors</h3>
              <p className="text-sm text-gray-600">Explore books by your favorite authors</p>
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default HomePage;