import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { loanService } from '../services/loanService';
import { bookService } from '../services/bookService';
import { Loan, Book } from '../types/api.types';
import { Link } from 'react-router-dom';

const MyLoans: React.FC = () => {
  const { user } = useAuth();
  const [loans, setLoans] = useState<Loan[]>([]);
  const [books, setBooks] = useState<Map<number, Book>>(new Map());
  const [loading, setLoading] = useState(true);
  const [returning, setReturning] = useState<number | null>(null);

  useEffect(() => {
    loadLoans();
  }, [user]);

  const loadLoans = async () => {
    if (!user) return;
    try {
      const response = await loanService.getMyLoans(0, 50);
      setLoans(response.content);
      
      // Load book details for each loan
      const bookIds = [...new Set(response.content.map(loan => loan.bookId))];
      const bookMap = new Map<number, Book>();
      
      await Promise.all(
        bookIds.map(async (bookId) => {
          try {
            const book = await bookService.getBookById(bookId);
            bookMap.set(bookId, book);
          } catch (error) {
            console.error(`Error loading book ${bookId}:`, error);
          }
        })
      );
      
      setBooks(bookMap);
    } catch (error) {
      console.error('Error loading loans:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleReturn = async (loanId: number) => {
    setReturning(loanId);
    try {
      await loanService.returnBook(loanId);
      loadLoans(); // Reload loans
    } catch (error) {
      console.error('Error returning book:', error);
      alert('Failed to return book');
    } finally {
      setReturning(null);
    }
  };

  const isOverdue = (dueDate: string) => {
    return new Date(dueDate) < new Date();
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  const activeLoans = loans.filter(loan => loan.status === 'ACTIVE');
  const returnedLoans = loans.filter(loan => loan.status === 'RETURNED');

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <h1 className="text-4xl font-bold text-gray-900 mb-8">My Loans</h1>

        {/* Active Loans */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Active Loans</h2>
          {activeLoans.length === 0 ? (
            <div className="bg-white rounded-lg shadow-md p-8 text-center">
              <p className="text-gray-600 mb-4">You don't have any active loans</p>
              <Link to="/books" className="text-blue-600 hover:text-blue-700 font-medium">
                Browse books to borrow →
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {activeLoans.map((loan) => {
                const book = books.get(loan.bookId);
                const overdue = isOverdue(loan.dueDate);
                
                return (
                  <div key={loan.id} className="bg-white rounded-lg shadow-md p-6">
                    <div className="flex flex-col md:flex-row gap-6">
                      <div className="md:w-32 h-40 bg-gray-200 rounded flex-shrink-0 flex items-center justify-center">
                        {book?.imageUrl ? (
                          <img src={book.imageUrl} alt={book.title} className="h-full w-full object-cover rounded" />
                        ) : (
                          <span className="text-gray-400">📚</span>
                        )}
                      </div>
                      
                      <div className="flex-1">
                        <h3 className="text-xl font-semibold mb-2">
                          {book ? (
                            <Link to={`/books/${book.id}`} className="hover:text-blue-600">
                              {book.title}
                            </Link>
                          ) : (
                            'Loading...'
                          )}
                        </h3>
                        {book && (
                          <p className="text-gray-600 mb-3">
                            by {book.authors.map(a => `${a.firstName} ${a.lastName}`).join(', ')}
                          </p>
                        )}
                        
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-4">
                          <div>
                            <p className="text-sm text-gray-600">Borrowed</p>
                            <p className="font-medium">{formatDate(loan.loanDate)}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-600">Due Date</p>
                            <p className={`font-medium ${overdue ? 'text-red-600' : ''}`}>
                              {formatDate(loan.dueDate)}
                              {overdue && ' (Overdue)'}
                            </p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-600">Status</p>
                            <span className={`inline-block px-2 py-1 rounded text-sm font-medium ${
                              overdue ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
                            }`}>
                              {overdue ? 'Overdue' : 'Active'}
                            </span>
                          </div>
                        </div>
                        
                        <button
                          onClick={() => handleReturn(loan.id)}
                          disabled={returning === loan.id}
                          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition"
                        >
                          {returning === loan.id ? 'Returning...' : 'Return Book'}
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        {/* Loan History */}
        <section>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Loan History</h2>
          {returnedLoans.length === 0 ? (
            <div className="bg-white rounded-lg shadow-md p-8 text-center">
              <p className="text-gray-600">No loan history yet</p>
            </div>
          ) : (
            <div className="bg-white rounded-lg shadow-md overflow-hidden">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Book
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Borrowed
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Returned
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {returnedLoans.map((loan) => {
                    const book = books.get(loan.bookId);
                    return (
                      <tr key={loan.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {book ? (
                            <Link to={`/books/${book.id}`} className="text-blue-600 hover:text-blue-700">
                              {book.title}
                            </Link>
                          ) : (
                            'Loading...'
                          )}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                          {formatDate(loan.loanDate)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                          {loan.returnDate ? formatDate(loan.returnDate) : '-'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                            Returned
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default MyLoans;
