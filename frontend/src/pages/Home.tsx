import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { bookService } from '../services/bookService';
import { recommendationService } from '../services/recommendationService';
import { Book, Recommendation } from '../types/api.types';

const Home: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const [popularBooks, setPopularBooks] = useState<Book[]>([]);
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [user]);

  const loadData = async () => {
    try {
      const booksResponse = await bookService.getBooks(0, 8);
      setPopularBooks(booksResponse.content);

      if (isAuthenticated && user) {
        const recs = await recommendationService.getRecommendations(user.id);
        setRecommendations(recs.slice(0, 4));
      }
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="bg-blue-600 text-white py-20">
        <div className="container mx-auto px-4">
          <h1 className="text-5xl font-bold mb-4">Welcome to the Library</h1>
          <p className="text-xl mb-8">
            Discover, borrow, and enjoy thousands of books
          </p>
          <div className="flex gap-4">
            <Link
              to="/books"
              className="bg-white text-blue-600 px-6 py-3 rounded-lg font-semibold hover:bg-gray-100 transition"
            >
              Browse Books
            </Link>
            {!isAuthenticated && (
              <Link
                to="/register"
                className="bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-800 transition"
              >
                Get Started
              </Link>
            )}
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-12">
        {/* Recommendations Section */}
        {isAuthenticated && recommendations.length > 0 && (
          <section className="mb-12">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-3xl font-bold text-gray-900">Recommended for You</h2>
              <Link to="/recommendations" className="text-blue-600 hover:text-blue-700">
                View All →
              </Link>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {recommendations.map((rec) => (
                <div key={rec.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition">
                  <div className="h-48 bg-gray-200 flex items-center justify-center">
                    <span className="text-gray-400">Book Cover</span>
                  </div>
                  <div className="p-4">
                    <h3 className="font-semibold text-lg mb-2 truncate">
                      {rec.book?.title || 'Book Title'}
                    </h3>
                    <p className="text-sm text-gray-600 mb-2">{rec.type}</p>
                    <Link
                      to={`/books/${rec.bookId}`}
                      className="text-blue-600 hover:text-blue-700 text-sm font-medium"
                    >
                      View Details →
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Popular Books Section */}
        <section>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-3xl font-bold text-gray-900">Popular Books</h2>
            <Link to="/books" className="text-blue-600 hover:text-blue-700">
              View All →
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {popularBooks.map((book) => (
              <div key={book.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition">
                <div className="h-48 bg-gray-200 flex items-center justify-center">
                  {book.imageUrl ? (
                    <img src={book.imageUrl} alt={book.title} className="h-full w-full object-cover" />
                  ) : (
                    <span className="text-gray-400">Book Cover</span>
                  )}
                </div>
                <div className="p-4">
                  <h3 className="font-semibold text-lg mb-2 truncate">{book.title}</h3>
                  <p className="text-sm text-gray-600 mb-2">
                    {book.authors.map(a => `${a.firstName} ${a.lastName}`).join(', ')}
                  </p>
                  <div className="flex justify-between items-center">
                    <span className={`text-sm ${book.availableCopies > 0 ? 'text-green-600' : 'text-red-600'}`}>
                      {book.availableCopies > 0 ? `${book.availableCopies} available` : 'Not available'}
                    </span>
                    <Link
                      to={`/books/${book.id}`}
                      className="text-blue-600 hover:text-blue-700 text-sm font-medium"
                    >
                      View →
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Features Section */}
        <section className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="text-center">
            <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-2xl">📚</span>
            </div>
            <h3 className="text-xl font-semibold mb-2">Vast Collection</h3>
            <p className="text-gray-600">Access thousands of books across all genres</p>
          </div>
          <div className="text-center">
            <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-2xl">🎯</span>
            </div>
            <h3 className="text-xl font-semibold mb-2">Personalized</h3>
            <p className="text-gray-600">Get recommendations based on your preferences</p>
          </div>
          <div className="text-center">
            <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-2xl">⚡</span>
            </div>
            <h3 className="text-xl font-semibold mb-2">Easy Borrowing</h3>
            <p className="text-gray-600">Borrow and return books with just a click</p>
          </div>
        </section>
      </div>
    </div>
  );
};

export default Home;
