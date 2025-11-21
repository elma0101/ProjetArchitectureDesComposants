import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Book, BorrowBookRequest } from '../../types';
import { booksAPI, loansAPI } from '../../services/api';
import LoadingSpinner from '../Common/LoadingSpinner';
import BorrowBookModal from '../Loans/BorrowBookModal';

const BookDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [book, setBook] = useState<Book | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showBorrowModal, setShowBorrowModal] = useState(false);
  const [borrowing, setBorrowing] = useState(false);

  useEffect(() => {
    if (id) {
      fetchBook(parseInt(id));
    }
  }, [id]);

  const fetchBook = async (bookId: number) => {
    try {
      setLoading(true);
      setError(null);
      const response = await booksAPI.getById(bookId);
      setBook(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch book details');
    } finally {
      setLoading(false);
    }
  };

  const handleBorrow = async (borrowRequest: BorrowBookRequest) => {
    if (!book) return;

    try {
      setBorrowing(true);
      await loansAPI.borrow(borrowRequest);
      setShowBorrowModal(false);
      // Refresh book data to update available copies
      await fetchBook(book.id);
      alert('Book borrowed successfully!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to borrow book');
    } finally {
      setBorrowing(false);
    }
  };

  const formatAuthors = (authors: any[]) => {
    if (!authors || authors.length === 0) return 'Unknown Author';
    return authors.map(author => `${author.firstName} ${author.lastName}`).join(', ');
  };

  const getAvailabilityStatus = () => {
    if (!book) return { text: '', color: '', bgColor: '' };
    
    if (book.availableCopies > 0) {
      return {
        text: `${book.availableCopies} of ${book.totalCopies} available`,
        color: 'text-green-600',
        bgColor: 'bg-green-100'
      };
    } else {
      return {
        text: 'Currently unavailable',
        color: 'text-red-600',
        bgColor: 'bg-red-100'
      };
    }
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading book details..." />;
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">❌ {error}</div>
        <Link
          to="/books"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Back to Books
        </Link>
      </div>
    );
  }

  if (!book) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-600 mb-4">Book not found</div>
        <Link
          to="/books"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Back to Books
        </Link>
      </div>
    );
  }

  const availability = getAvailabilityStatus();

  return (
    <div className="max-w-4xl mx-auto">
      {/* Breadcrumb */}
      <nav className="mb-6">
        <Link to="/books" className="text-blue-600 hover:text-blue-800">
          ← Back to Books
        </Link>
      </nav>

      <div className="bg-white rounded-lg shadow-lg overflow-hidden">
        <div className="md:flex">
          {/* Book Image */}
          <div className="md:w-1/3">
            <div className="h-96 bg-gray-200 flex items-center justify-center">
              {book.imageUrl ? (
                <img
                  src={book.imageUrl}
                  alt={book.title}
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="text-gray-400 text-6xl">📖</div>
              )}
            </div>
          </div>

          {/* Book Details */}
          <div className="md:w-2/3 p-8">
            <div className="mb-4">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{book.title}</h1>
              <p className="text-xl text-gray-600">by {formatAuthors(book.authors)}</p>
            </div>

            {/* Availability Status */}
            <div className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${availability.bgColor} ${availability.color} mb-4`}>
              {availability.text}
            </div>

            {/* Book Metadata */}
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div>
                <span className="font-semibold text-gray-700">ISBN:</span>
                <span className="ml-2 text-gray-600">{book.isbn}</span>
              </div>
              {book.genre && (
                <div>
                  <span className="font-semibold text-gray-700">Genre:</span>
                  <span className="ml-2 text-gray-600">{book.genre}</span>
                </div>
              )}
              {book.publicationYear && (
                <div>
                  <span className="font-semibold text-gray-700">Published:</span>
                  <span className="ml-2 text-gray-600">{book.publicationYear}</span>
                </div>
              )}
              <div>
                <span className="font-semibold text-gray-700">Total Copies:</span>
                <span className="ml-2 text-gray-600">{book.totalCopies}</span>
              </div>
            </div>

            {/* Description */}
            {book.description && (
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-2">Description</h3>
                <p className="text-gray-600 leading-relaxed">{book.description}</p>
              </div>
            )}

            {/* Authors Details */}
            {book.authors && book.authors.length > 0 && (
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-2">Authors</h3>
                <div className="space-y-2">
                  {book.authors.map((author) => (
                    <div key={author.id} className="flex items-center space-x-2">
                      <Link
                        to={`/authors/${author.id}`}
                        className="text-blue-600 hover:text-blue-800 font-medium"
                      >
                        {author.firstName} {author.lastName}
                      </Link>
                      {author.nationality && (
                        <span className="text-sm text-gray-500">({author.nationality})</span>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex space-x-4">
              {book.availableCopies > 0 ? (
                <button
                  onClick={() => setShowBorrowModal(true)}
                  className="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition-colors font-medium"
                >
                  📚 Borrow This Book
                </button>
              ) : (
                <button
                  disabled
                  className="bg-gray-400 text-white px-6 py-3 rounded-lg cursor-not-allowed font-medium"
                >
                  Currently Unavailable
                </button>
              )}
              
              <Link
                to={`/books/${book.id}/loans`}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors font-medium"
              >
                📋 View Loan History
              </Link>
            </div>

            {/* Timestamps */}
            <div className="mt-6 pt-6 border-t border-gray-200 text-sm text-gray-500">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <span className="font-medium">Added:</span>
                  <span className="ml-2">{new Date(book.createdAt).toLocaleDateString()}</span>
                </div>
                <div>
                  <span className="font-medium">Updated:</span>
                  <span className="ml-2">{new Date(book.updatedAt).toLocaleDateString()}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Borrow Modal */}
      {showBorrowModal && (
        <BorrowBookModal
          book={book}
          onBorrow={handleBorrow}
          onClose={() => setShowBorrowModal(false)}
          loading={borrowing}
        />
      )}
    </div>
  );
};

export default BookDetail;