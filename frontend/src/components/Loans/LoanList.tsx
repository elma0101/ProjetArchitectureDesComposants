import React, { useState, useEffect } from 'react';
import { Loan, LoanSearchFilters, PagedResponse, LoanStatus } from '../../types';
import { loansAPI } from '../../services/api';
import LoadingSpinner from '../Common/LoadingSpinner';
import Pagination from '../Common/Pagination';
import LoanCard from './LoanCard';
import LoanSearch from './LoanSearch';

const LoanList: React.FC = () => {
  const [loans, setLoans] = useState<PagedResponse<Loan> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'all' | 'active' | 'overdue' | 'returned'>('all');
  const [filters, setFilters] = useState<LoanSearchFilters>({
    page: 0,
    size: 20,
    sortBy: 'loanDate',
    sortDir: 'desc'
  });

  const fetchLoans = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;
      
      switch (activeTab) {
        case 'active':
          response = await loansAPI.getActive(filters.page, filters.size, filters.sortBy, filters.sortDir);
          break;
        case 'overdue':
          response = await loansAPI.getOverdue(filters.page, filters.size);
          break;
        case 'returned':
          response = await loansAPI.search({ ...filters, status: LoanStatus.RETURNED });
          break;
        default:
          if (Object.keys(filters).some(key => key !== 'page' && key !== 'size' && key !== 'sortBy' && key !== 'sortDir' && filters[key as keyof LoanSearchFilters])) {
            response = await loansAPI.search(filters);
          } else {
            response = await loansAPI.search(filters);
          }
      }
      
      setLoans(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch loans');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLoans();
  }, [filters, activeTab]);

  const handleSearch = (searchFilters: LoanSearchFilters) => {
    setFilters({ ...searchFilters, page: 0 });
    setActiveTab('all');
  };

  const handlePageChange = (page: number) => {
    setFilters({ ...filters, page });
  };

  const handleTabChange = (tab: 'all' | 'active' | 'overdue' | 'returned') => {
    setActiveTab(tab);
    setFilters({ ...filters, page: 0 });
  };

  const handleLoanAction = async (loanId: number, action: 'return' | 'extend') => {
    try {
      if (action === 'return') {
        await loansAPI.return(loanId);
        alert('Book returned successfully!');
      } else if (action === 'extend') {
        await loansAPI.extend(loanId, { additionalDays: 7 });
        alert('Loan extended by 7 days!');
      }
      fetchLoans(); // Refresh the list
    } catch (err: any) {
      alert(err.response?.data?.message || `Failed to ${action} loan`);
    }
  };

  const getTabCount = (status: string) => {
    // This would ideally come from an API endpoint that returns counts
    return '';
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading loans..." />;
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">❌ {error}</div>
        <button
          onClick={fetchLoans}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Loan Management</h1>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {[
            { key: 'all', label: 'All Loans', icon: '📚' },
            { key: 'active', label: 'Active', icon: '🔄' },
            { key: 'overdue', label: 'Overdue', icon: '⚠️' },
            { key: 'returned', label: 'Returned', icon: '✅' }
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => handleTabChange(tab.key as any)}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === tab.key
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab.icon} {tab.label} {getTabCount(tab.key)}
            </button>
          ))}
        </nav>
      </div>

      <LoanSearch onSearch={handleSearch} />

      {loans && (
        <>
          <div className="text-sm text-gray-600 mb-4">
            Found {loans.totalElements} loans
          </div>

          {loans.content.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-gray-500 text-lg mb-4">📋 No loans found</div>
              <p className="text-gray-400">Try adjusting your search criteria</p>
            </div>
          ) : (
            <>
              <div className="space-y-4">
                {loans.content.map((loan) => (
                  <LoanCard
                    key={loan.id}
                    loan={loan}
                    onAction={handleLoanAction}
                  />
                ))}
              </div>

              <Pagination
                currentPage={loans.number}
                totalPages={loans.totalPages}
                totalElements={loans.totalElements}
                onPageChange={handlePageChange}
              />
            </>
          )}
        </>
      )}
    </div>
  );
};

export default LoanList;