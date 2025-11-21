import React, { useState, useEffect, useContext } from 'react';
import { Loan, PagedResponse } from '../types';
import { loansAPI } from '../services/api';
import { AuthContext } from '../App';
import LoadingSpinner from '../components/Common/LoadingSpinner';
import LoanCard from '../components/Loans/LoanCard';
import Pagination from '../components/Common/Pagination';

const MyLoansPage: React.FC = () => {
  const { user } = useContext(AuthContext);
  const [loans, setLoans] = useState<PagedResponse<Loan> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);

  useEffect(() => {
    if (user) {
      fetchMyLoans();
    }
  }, [user, page]);

  const fetchMyLoans = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);
      const response = await loansAPI.getByBorrowerEmail(user.email, page, 20);
      setLoans(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch your loans');
    } finally {
      setLoading(false);
    }
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
      fetchMyLoans(); // Refresh the list
    } catch (err: any) {
      alert(err.response?.data?.message || `Failed to ${action} loan`);
    }
  };

  if (!user) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-600">Please log in to view your loans.</div>
      </div>
    );
  }

  if (loading) {
    return <LoadingSpinner size="large" message="Loading your loans..." />;
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">❌ {error}</div>
        <button
          onClick={fetchMyLoans}
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
        <h1 className="text-3xl font-bold text-gray-900">My Loans</h1>
      </div>

      {loans && (
        <>
          <div className="text-sm text-gray-600 mb-4">
            You have {loans.totalElements} loan{loans.totalElements !== 1 ? 's' : ''} in total
          </div>

          {loans.content.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-gray-500 text-lg mb-4">📚 No loans found</div>
              <p className="text-gray-400 mb-4">You haven't borrowed any books yet.</p>
              <a
                href="/books"
                className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
              >
                Browse Books
              </a>
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
                onPageChange={setPage}
              />
            </>
          )}
        </>
      )}
    </div>
  );
};

export default MyLoansPage;