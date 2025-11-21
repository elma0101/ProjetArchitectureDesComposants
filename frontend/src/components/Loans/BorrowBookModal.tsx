import React, { useState } from 'react';
import { Book, BorrowBookRequest } from '../../types';

interface BorrowBookModalProps {
  book: Book;
  onBorrow: (request: BorrowBookRequest) => void;
  onClose: () => void;
  loading: boolean;
}

const BorrowBookModal: React.FC<BorrowBookModalProps> = ({
  book,
  onBorrow,
  onClose,
  loading
}) => {
  const [formData, setFormData] = useState({
    borrowerName: '',
    borrowerEmail: '',
    borrowerId: '',
    notes: ''
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate form
    const newErrors: Record<string, string> = {};
    
    if (!formData.borrowerName.trim()) {
      newErrors.borrowerName = 'Borrower name is required';
    }
    
    if (!formData.borrowerEmail.trim()) {
      newErrors.borrowerEmail = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.borrowerEmail)) {
      newErrors.borrowerEmail = 'Invalid email format';
    }
    
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    // Submit form
    const borrowRequest: BorrowBookRequest = {
      bookId: book.id,
      borrowerName: formData.borrowerName.trim(),
      borrowerEmail: formData.borrowerEmail.trim(),
      borrowerId: formData.borrowerId.trim() || undefined,
      notes: formData.notes.trim() || undefined
    };

    onBorrow(borrowRequest);
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold text-gray-900">Borrow Book</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
              disabled={loading}
            >
              ✕
            </button>
          </div>

          {/* Book Info */}
          <div className="bg-gray-50 rounded-lg p-4 mb-6">
            <h3 className="font-semibold text-gray-900">{book.title}</h3>
            <p className="text-sm text-gray-600">
              by {book.authors?.map(a => `${a.firstName} ${a.lastName}`).join(', ') || 'Unknown Author'}
            </p>
            <p className="text-sm text-gray-500">ISBN: {book.isbn}</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Borrower Name */}
            <div>
              <label htmlFor="borrowerName" className="block text-sm font-medium text-gray-700 mb-1">
                Borrower Name *
              </label>
              <input
                type="text"
                id="borrowerName"
                value={formData.borrowerName}
                onChange={(e) => handleInputChange('borrowerName', e.target.value)}
                className={`w-full border rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.borrowerName ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Enter full name"
                disabled={loading}
              />
              {errors.borrowerName && (
                <p className="text-red-500 text-sm mt-1">{errors.borrowerName}</p>
              )}
            </div>

            {/* Borrower Email */}
            <div>
              <label htmlFor="borrowerEmail" className="block text-sm font-medium text-gray-700 mb-1">
                Email Address *
              </label>
              <input
                type="email"
                id="borrowerEmail"
                value={formData.borrowerEmail}
                onChange={(e) => handleInputChange('borrowerEmail', e.target.value)}
                className={`w-full border rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.borrowerEmail ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Enter email address"
                disabled={loading}
              />
              {errors.borrowerEmail && (
                <p className="text-red-500 text-sm mt-1">{errors.borrowerEmail}</p>
              )}
            </div>

            {/* Borrower ID (Optional) */}
            <div>
              <label htmlFor="borrowerId" className="block text-sm font-medium text-gray-700 mb-1">
                Borrower ID (Optional)
              </label>
              <input
                type="text"
                id="borrowerId"
                value={formData.borrowerId}
                onChange={(e) => handleInputChange('borrowerId', e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Student ID, Employee ID, etc."
                disabled={loading}
              />
            </div>

            {/* Notes (Optional) */}
            <div>
              <label htmlFor="notes" className="block text-sm font-medium text-gray-700 mb-1">
                Notes (Optional)
              </label>
              <textarea
                id="notes"
                value={formData.notes}
                onChange={(e) => handleInputChange('notes', e.target.value)}
                rows={3}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Any additional notes..."
                disabled={loading}
              />
            </div>

            {/* Action Buttons */}
            <div className="flex space-x-3 pt-4">
              <button
                type="submit"
                disabled={loading}
                className="flex-1 bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {loading ? 'Borrowing...' : 'Confirm Borrow'}
              </button>
              <button
                type="button"
                onClick={onClose}
                disabled={loading}
                className="flex-1 bg-gray-500 text-white py-2 px-4 rounded-md hover:bg-gray-600 disabled:opacity-50 transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>

          {/* Loan Terms */}
          <div className="mt-4 p-3 bg-blue-50 rounded-lg">
            <p className="text-sm text-blue-800">
              📋 <strong>Loan Terms:</strong> Books are typically due in 14 days. 
              Late returns may incur fees. Please return books in good condition.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BorrowBookModal;