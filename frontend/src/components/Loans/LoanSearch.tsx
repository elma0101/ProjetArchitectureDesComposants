import React, { useState } from 'react';
import { LoanSearchFilters, LoanStatus } from '../../types';

interface LoanSearchProps {
  onSearch: (filters: LoanSearchFilters) => void;
}

const LoanSearch: React.FC<LoanSearchProps> = ({ onSearch }) => {
  const [filters, setFilters] = useState<LoanSearchFilters>({
    borrowerEmail: '',
    borrowerName: '',
    status: undefined,
    startDate: '',
    endDate: '',
    size: 20,
    sortBy: 'loanDate',
    sortDir: 'desc'
  });

  const [isAdvancedOpen, setIsAdvancedOpen] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch(filters);
  };

  const handleReset = () => {
    const resetFilters: LoanSearchFilters = {
      borrowerEmail: '',
      borrowerName: '',
      status: undefined,
      startDate: '',
      endDate: '',
      size: 20,
      sortBy: 'loanDate',
      sortDir: 'desc'
    };
    setFilters(resetFilters);
    onSearch(resetFilters);
  };

  const handleInputChange = (field: keyof LoanSearchFilters, value: any) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Basic Search */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label htmlFor="borrowerName" className="block text-sm font-medium text-gray-700 mb-1">
              Borrower Name
            </label>
            <input
              type="text"
              id="borrowerName"
              value={filters.borrowerName || ''}
              onChange={(e) => handleInputChange('borrowerName', e.target.value)}
              placeholder="Search by borrower name..."
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label htmlFor="borrowerEmail" className="block text-sm font-medium text-gray-700 mb-1">
              Borrower Email
            </label>
            <input
              type="email"
              id="borrowerEmail"
              value={filters.borrowerEmail || ''}
              onChange={(e) => handleInputChange('borrowerEmail', e.target.value)}
              placeholder="Search by email..."
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
              Status
            </label>
            <select
              id="status"
              value={filters.status || ''}
              onChange={(e) => handleInputChange('status', e.target.value || undefined)}
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Statuses</option>
              <option value={LoanStatus.ACTIVE}>Active</option>
              <option value={LoanStatus.OVERDUE}>Overdue</option>
              <option value={LoanStatus.RETURNED}>Returned</option>
            </select>
          </div>
        </div>

        {/* Advanced Search Toggle */}
        <div className="flex items-center justify-between">
          <button
            type="button"
            onClick={() => setIsAdvancedOpen(!isAdvancedOpen)}
            className="text-blue-600 hover:text-blue-800 text-sm font-medium"
          >
            {isAdvancedOpen ? '▼' : '▶'} Advanced Search
          </button>
        </div>

        {/* Advanced Search Options */}
        {isAdvancedOpen && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 pt-4 border-t border-gray-200">
            <div>
              <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-1">
                Start Date
              </label>
              <input
                type="date"
                id="startDate"
                value={filters.startDate || ''}
                onChange={(e) => handleInputChange('startDate', e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-1">
                End Date
              </label>
              <input
                type="date"
                id="endDate"
                value={filters.endDate || ''}
                onChange={(e) => handleInputChange('endDate', e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label htmlFor="sortBy" className="block text-sm font-medium text-gray-700 mb-1">
                Sort By
              </label>
              <select
                id="sortBy"
                value={filters.sortBy || 'loanDate'}
                onChange={(e) => handleInputChange('sortBy', e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="loanDate">Loan Date</option>
                <option value="dueDate">Due Date</option>
                <option value="returnDate">Return Date</option>
                <option value="borrowerName">Borrower Name</option>
                <option value="createdAt">Created Date</option>
              </select>
            </div>

            <div>
              <label htmlFor="sortDir" className="block text-sm font-medium text-gray-700 mb-1">
                Sort Direction
              </label>
              <select
                id="sortDir"
                value={filters.sortDir || 'desc'}
                onChange={(e) => handleInputChange('sortDir', e.target.value as 'asc' | 'desc')}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="desc">Newest First</option>
                <option value="asc">Oldest First</option>
              </select>
            </div>

            <div>
              <label htmlFor="pageSize" className="block text-sm font-medium text-gray-700 mb-1">
                Results per page
              </label>
              <select
                id="pageSize"
                value={filters.size || 20}
                onChange={(e) => handleInputChange('size', parseInt(e.target.value))}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
                <option value={100}>100</option>
              </select>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex space-x-3 pt-4">
          <button
            type="submit"
            className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition-colors"
          >
            🔍 Search Loans
          </button>
          <button
            type="button"
            onClick={handleReset}
            className="bg-gray-500 text-white px-6 py-2 rounded-md hover:bg-gray-600 transition-colors"
          >
            Clear
          </button>
        </div>
      </form>
    </div>
  );
};

export default LoanSearch;