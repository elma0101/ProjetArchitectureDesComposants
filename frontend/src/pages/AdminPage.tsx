import React, { useState, useEffect } from 'react';
import { BookStatistics, LoanStatistics } from '../types';
import { booksAPI, loansAPI, adminAPI } from '../services/api';
import LoadingSpinner from '../components/Common/LoadingSpinner';

const AdminPage: React.FC = () => {
  const [bookStats, setBookStats] = useState<BookStatistics | null>(null);
  const [loanStats, setLoanStats] = useState<LoanStatistics | null>(null);
  const [systemHealth, setSystemHealth] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchAdminData();
  }, []);

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [bookStatsRes, loanStatsRes, healthRes] = await Promise.allSettled([
        booksAPI.getStatistics(),
        loansAPI.getStatistics(),
        adminAPI.getSystemHealth()
      ]);

      if (bookStatsRes.status === 'fulfilled') {
        setBookStats(bookStatsRes.value.data);
      }
      
      if (loanStatsRes.status === 'fulfilled') {
        setLoanStats(loanStatsRes.value.data);
      }
      
      if (healthRes.status === 'fulfilled') {
        setSystemHealth(healthRes.value.data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch admin data');
    } finally {
      setLoading(false);
    }
  };

  const updateOverdueLoans = async () => {
    try {
      await loansAPI.updateOverdue();
      alert('Overdue loans updated successfully!');
      fetchAdminData(); // Refresh data
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update overdue loans');
    }
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading admin dashboard..." />;
  }

  return (
    <div className="space-y-8">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
        <button
          onClick={updateOverdueLoans}
          className="bg-orange-600 text-white px-4 py-2 rounded hover:bg-orange-700"
        >
          Update Overdue Loans
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* System Health */}
      {systemHealth && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">System Health</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="text-center">
              <div className="text-2xl mb-2">
                {systemHealth.status === 'UP' ? '✅' : '❌'}
              </div>
              <div className="font-semibold">System Status</div>
              <div className="text-sm text-gray-600">{systemHealth.status}</div>
            </div>
            <div className="text-center">
              <div className="text-2xl mb-2">💾</div>
              <div className="font-semibold">Database</div>
              <div className="text-sm text-gray-600">Connected</div>
            </div>
            <div className="text-center">
              <div className="text-2xl mb-2">🌐</div>
              <div className="font-semibold">API</div>
              <div className="text-sm text-gray-600">Operational</div>
            </div>
          </div>
        </div>
      )}

      {/* Statistics Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Book Statistics */}
        {bookStats && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4">📚 Book Statistics</h2>
            <div className="space-y-4">
              <div className="flex justify-between">
                <span className="text-gray-600">Total Books:</span>
                <span className="font-semibold">{bookStats.totalBooks}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Available Books:</span>
                <span className="font-semibold text-green-600">{bookStats.availableBooks}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Loaned Books:</span>
                <span className="font-semibold text-orange-600">{bookStats.loanedBooks}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Total Authors:</span>
                <span className="font-semibold">{bookStats.totalAuthors}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Total Genres:</span>
                <span className="font-semibold">{bookStats.totalGenres}</span>
              </div>
              {bookStats.mostPopularGenre && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Most Popular Genre:</span>
                  <span className="font-semibold">{bookStats.mostPopularGenre}</span>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Loan Statistics */}
        {loanStats && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4">📋 Loan Statistics</h2>
            <div className="space-y-4">
              <div className="flex justify-between">
                <span className="text-gray-600">Total Loans:</span>
                <span className="font-semibold">{loanStats.totalLoans}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Active Loans:</span>
                <span className="font-semibold text-blue-600">{loanStats.activeLoans}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Overdue Loans:</span>
                <span className="font-semibold text-red-600">{loanStats.overdueLoans}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Returned Loans:</span>
                <span className="font-semibold text-green-600">{loanStats.returnedLoans}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Avg. Loan Duration:</span>
                <span className="font-semibold">{loanStats.averageLoanDuration} days</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <div className="text-2xl mr-3">📚</div>
            <div className="text-left">
              <h3 className="font-semibold">Manage Books</h3>
              <p className="text-sm text-gray-600">Add, edit, or remove books</p>
            </div>
          </button>
          
          <button className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <div className="text-2xl mr-3">👥</div>
            <div className="text-left">
              <h3 className="font-semibold">Manage Users</h3>
              <p className="text-sm text-gray-600">View and manage user accounts</p>
            </div>
          </button>
          
          <button className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <div className="text-2xl mr-3">📊</div>
            <div className="text-left">
              <h3 className="font-semibold">View Reports</h3>
              <p className="text-sm text-gray-600">Generate system reports</p>
            </div>
          </button>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4">Recent Activity</h2>
        <div className="text-center py-8 text-gray-500">
          <div className="text-4xl mb-2">📈</div>
          <p>Activity monitoring coming soon...</p>
        </div>
      </div>
    </div>
  );
};

export default AdminPage;