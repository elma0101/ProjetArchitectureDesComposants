import React from 'react';
import { Link } from 'react-router-dom';
import { Loan, LoanStatus } from '../../types';

interface LoanCardProps {
  loan: Loan;
  onAction: (loanId: number, action: 'return' | 'extend') => void;
  showActions?: boolean;
}

const LoanCard: React.FC<LoanCardProps> = ({ loan, onAction, showActions = true }) => {
  const getStatusBadge = (status: LoanStatus) => {
    switch (status) {
      case LoanStatus.ACTIVE:
        return {
          text: 'Active',
          bgColor: 'bg-blue-100',
          textColor: 'text-blue-800'
        };
      case LoanStatus.OVERDUE:
        return {
          text: 'Overdue',
          bgColor: 'bg-red-100',
          textColor: 'text-red-800'
        };
      case LoanStatus.RETURNED:
        return {
          text: 'Returned',
          bgColor: 'bg-green-100',
          textColor: 'text-green-800'
        };
      default:
        return {
          text: status,
          bgColor: 'bg-gray-100',
          textColor: 'text-gray-800'
        };
    }
  };

  const isOverdue = () => {
    if (loan.status !== LoanStatus.ACTIVE) return false;
    return new Date(loan.dueDate) < new Date();
  };

  const getDaysUntilDue = () => {
    const dueDate = new Date(loan.dueDate);
    const today = new Date();
    const diffTime = dueDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const statusBadge = getStatusBadge(loan.status);
  const daysUntilDue = getDaysUntilDue();
  const overdue = isOverdue();

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 p-6">
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between">
        {/* Loan Info */}
        <div className="flex-1">
          <div className="flex items-start justify-between mb-3">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-1">
                <Link
                  to={`/books/${loan.book.id}`}
                  className="hover:text-blue-600 transition-colors"
                >
                  {loan.book.title}
                </Link>
              </h3>
              <p className="text-sm text-gray-600">
                by {loan.book.authors?.map(a => `${a.firstName} ${a.lastName}`).join(', ') || 'Unknown Author'}
              </p>
            </div>
            <div className={`px-2 py-1 rounded-full text-xs font-medium ${statusBadge.bgColor} ${statusBadge.textColor}`}>
              {statusBadge.text}
            </div>
          </div>

          {/* Borrower Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-3">
            <div>
              <span className="text-sm font-medium text-gray-700">Borrower:</span>
              <span className="ml-2 text-sm text-gray-600">{loan.borrowerName}</span>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-700">Email:</span>
              <span className="ml-2 text-sm text-gray-600">{loan.borrowerEmail}</span>
            </div>
            {loan.borrowerId && (
              <div>
                <span className="text-sm font-medium text-gray-700">ID:</span>
                <span className="ml-2 text-sm text-gray-600">{loan.borrowerId}</span>
              </div>
            )}
          </div>

          {/* Dates */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-3">
            <div>
              <span className="text-sm font-medium text-gray-700">Borrowed:</span>
              <span className="ml-2 text-sm text-gray-600">{formatDate(loan.loanDate)}</span>
            </div>
            <div>
              <span className="text-sm font-medium text-gray-700">Due:</span>
              <span className={`ml-2 text-sm ${overdue ? 'text-red-600 font-medium' : 'text-gray-600'}`}>
                {formatDate(loan.dueDate)}
                {loan.status === LoanStatus.ACTIVE && (
                  <span className={`ml-1 ${overdue ? 'text-red-600' : daysUntilDue <= 3 ? 'text-orange-600' : 'text-gray-500'}`}>
                    ({overdue ? `${Math.abs(daysUntilDue)} days overdue` : `${daysUntilDue} days left`})
                  </span>
                )}
              </span>
            </div>
            {loan.returnDate && (
              <div>
                <span className="text-sm font-medium text-gray-700">Returned:</span>
                <span className="ml-2 text-sm text-gray-600">{formatDate(loan.returnDate)}</span>
              </div>
            )}
          </div>

          {/* Notes */}
          {loan.notes && (
            <div className="mb-3">
              <span className="text-sm font-medium text-gray-700">Notes:</span>
              <span className="ml-2 text-sm text-gray-600">{loan.notes}</span>
            </div>
          )}

          {/* Due Date Warning */}
          {loan.status === LoanStatus.ACTIVE && daysUntilDue <= 3 && daysUntilDue > 0 && (
            <div className="bg-orange-50 border border-orange-200 rounded-md p-2 mb-3">
              <p className="text-sm text-orange-800">
                ⚠️ Due soon! This book is due in {daysUntilDue} day{daysUntilDue !== 1 ? 's' : ''}.
              </p>
            </div>
          )}

          {/* Overdue Warning */}
          {overdue && (
            <div className="bg-red-50 border border-red-200 rounded-md p-2 mb-3">
              <p className="text-sm text-red-800">
                🚨 This book is {Math.abs(daysUntilDue)} day{Math.abs(daysUntilDue) !== 1 ? 's' : ''} overdue!
              </p>
            </div>
          )}
        </div>

        {/* Actions */}
        {showActions && loan.status === LoanStatus.ACTIVE && (
          <div className="flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-2 mt-4 lg:mt-0 lg:ml-4">
            <button
              onClick={() => onAction(loan.id, 'return')}
              className="bg-green-600 text-white px-4 py-2 rounded text-sm hover:bg-green-700 transition-colors"
            >
              ✅ Return Book
            </button>
            <button
              onClick={() => onAction(loan.id, 'extend')}
              className="bg-blue-600 text-white px-4 py-2 rounded text-sm hover:bg-blue-700 transition-colors"
            >
              📅 Extend Loan
            </button>
          </div>
        )}
      </div>

      {/* Loan ID and timestamps */}
      <div className="mt-4 pt-4 border-t border-gray-200 text-xs text-gray-500">
        <div className="flex justify-between">
          <span>Loan ID: {loan.id}</span>
          <span>Created: {formatDate(loan.createdAt)}</span>
        </div>
      </div>
    </div>
  );
};

export default LoanCard;