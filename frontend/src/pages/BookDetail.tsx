import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { bookService } from '../services/bookService';
import { loanService } from '../services/loanService';
import { useAuth } from '../context/AuthContext';
import { Book } from '../types/api.types';

const BookDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const [book, setBook] = useState<Book | null>(null);
  const [loading, setLoading] = useState(true);
  const [borrowing, setBorrowing] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadBook();
  }, [id]);

  const loadBook = async () => {
    if (!id) return;
    try {
      const data = await bookService.getBookById(parseInt(id));
      setBook(data);
    } catch (error) {
      console.error('Error loading book:', error);
      setError('Failed to load book details');
    } finally {
      setLoading(false);
    }
  };

  const handleBorrow = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!book) return;

    setBorrowing(true);
    setError('');
    setSuccess('');

    try {
      await loanService.createLoan(book.id);
      setSuccess('Book borrowed successfully!');
      loadBook(); // Reload to update available copies
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to borrow book');
    } finally {
      setBorrowing(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!book) {
    return (
      <div className="container mx-auto px-4 py-8">
        <p className="text-center text-xl text-gray-600">Book not found</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <button
          onClick={() => navigate(-1)}
          className="mb-6 text-blue-600 hover:text-blue-700 flex items-center gap-2"
        >
          ← Back
        </button>

        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          <div className="md:flex">
            <div className="md:w-1/3 bg-gray-200 flex items-center justify-center p-8">
              {book.imageUrl ? (
                <img src={book.imageUrl} alt={book.title} className="max-h-96 object-contain" />
              ) : (
                <div className="text-gray-400 text-center">
                  <div className="text-6xl mb-4">📚</div>
                  <p>No cover image</p>
                </div>
              )}
            </div>

            <div className="md:w-2/3 p-8">
              <h1 className="text-4xl font-bold text-gray-900 mb-4">{book.title}</h1>
              
              <div className="mb-6">
                <h2 className="text-xl font-semibold text-gray-700 mb-2">Authors</h2>
                <div className="flex flex-wrap gap-2">
                  {book.authors.map((author) => (
                    <Link
                      key={author.id}
                      to={`/authors/${author.id}`}
                      className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full hover:bg-blue-200 transition"
                    >
                      {author.firstName} {author.lastName}
                    </Link>
                  ))}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 mb-6">
                <div>
                  <p className="text-gray-600">ISBN</p>
                  <p className="font-semibold">{book.isbn}</p>
                </div>
                {book.publicationYear && (
                  <div>
                    <p className="text-gray-600">Publication Year</p>
                    <p className="font-semibold">{book.publicationYear}</p>
                  </div>
                )}
                {book.genre && (
                  <div>
                    <p className="text-gray-600">Genre</p>
                    <p className="font-semibold">{book.genre}</p>
                  </div>
                )}
                <div>
                  <p className="text-gray-600">Availability</p>
                  <p className={`font-semibold ${book.availableCopies > 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {book.availableCopies} of {book.totalCopies} available
                  </p>
                </div>
              </div>

              {book.description && (
                <div className="mb-6">
                  <h2 className="text-xl font-semibold text-gray-700 mb-2">Description</h2>
                  <p className="text-gray-700 leading-relaxed">{book.description}</p>
                </div>
              )}

              {error && (
                <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-red-800">{error}</p>
                </div>
              )}

              {success && (
                <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                  <p className="text-green-800">{success}</p>
                </div>
              )}

              <div className="flex gap-4">
                <button
                  onClick={handleBorrow}
                  disabled={book.availableCopies === 0 || borrowing || !isAuthenticated}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                >
                  {borrowing ? 'Borrowing...' : book.availableCopies === 0 ? 'Not Available' : 'Borrow Book'}
                </button>
                {!isAuthenticated && (
                  <Link
                    to="/login"
                    className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
                  >
                    Login to Borrow
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookDetail;
