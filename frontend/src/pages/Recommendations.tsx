import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { recommendationService } from '../services/recommendationService';
import { bookService } from '../services/bookService';
import { Recommendation, Book, RecommendationType } from '../types/api.types';

const Recommendations: React.FC = () => {
  const { user } = useAuth();
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [books, setBooks] = useState<Map<number, Book>>(new Map());
  const [loading, setLoading] = useState(true);
  const [selectedType, setSelectedType] = useState<RecommendationType | 'ALL'>('ALL');
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadRecommendations();
  }, [user, selectedType]);

  const loadRecommendations = async () => {
    if (!user) return;
    setLoading(true);
    try {
      let recs: Recommendation[];
      if (selectedType === 'ALL') {
        recs = await recommendationService.getRecommendations(user.id);
      } else {
        recs = await recommendationService.getRecommendationsByType(user.id, selectedType);
      }
      setRecommendations(recs);

      // Load book details
      const bookIds = [...new Set(recs.map(rec => rec.bookId))];
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
      console.error('Error loading recommendations:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    if (!user) return;
    setRefreshing(true);
    try {
      await recommendationService.refreshRecommendations(user.id);
      await loadRecommendations();
    } catch (error) {
      console.error('Error refreshing recommendations:', error);
    } finally {
      setRefreshing(false);
    }
  };

  const getTypeLabel = (type: RecommendationType) => {
    switch (type) {
      case RecommendationType.POPULAR:
        return 'Popular';
      case RecommendationType.TRENDING:
        return 'Trending';
      case RecommendationType.CONTENT_BASED:
        return 'Based on Your Preferences';
      case RecommendationType.COLLABORATIVE:
        return 'Users Like You Enjoyed';
      default:
        return type;
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
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900">Recommendations for You</h1>
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition"
          >
            {refreshing ? 'Refreshing...' : '🔄 Refresh'}
          </button>
        </div>

        {/* Filter by Type */}
        <div className="bg-white rounded-lg shadow-md p-4 mb-8">
          <div className="flex gap-2 flex-wrap">
            <button
              onClick={() => setSelectedType('ALL')}
              className={`px-4 py-2 rounded-lg transition ${
                selectedType === 'ALL' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              All
            </button>
            {Object.values(RecommendationType).map((type) => (
              <button
                key={type}
                onClick={() => setSelectedType(type)}
                className={`px-4 py-2 rounded-lg transition ${
                  selectedType === type ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                {getTypeLabel(type)}
              </button>
            ))}
          </div>
        </div>

        {/* Recommendations Grid */}
        {recommendations.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-12 text-center">
            <p className="text-xl text-gray-600 mb-4">No recommendations available yet</p>
            <p className="text-gray-500 mb-6">
              Start borrowing books to get personalized recommendations!
            </p>
            <Link
              to="/books"
              className="inline-block px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
              Browse Books
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {recommendations.map((rec) => {
              const book = books.get(rec.bookId);
              
              return (
                <div key={rec.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition">
                  <div className="h-64 bg-gray-200 flex items-center justify-center">
                    {book?.imageUrl ? (
                      <img src={book.imageUrl} alt={book.title} className="h-full w-full object-cover" />
                    ) : (
                      <span className="text-gray-400 text-4xl">📚</span>
                    )}
                  </div>
                  <div className="p-4">
                    <div className="mb-2">
                      <span className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded">
                        {getTypeLabel(rec.type)}
                      </span>
                    </div>
                    <h3 className="font-semibold text-lg mb-2 line-clamp-2">
                      {book?.title || 'Loading...'}
                    </h3>
                    {book && (
                      <p className="text-sm text-gray-600 mb-2">
                        {book.authors.map(a => `${a.firstName} ${a.lastName}`).join(', ')}
                      </p>
                    )}
                    {rec.reason && (
                      <p className="text-xs text-gray-500 mb-3 line-clamp-2">{rec.reason}</p>
                    )}
                    <div className="flex justify-between items-center">
                      {book && (
                        <span className={`text-sm ${book.availableCopies > 0 ? 'text-green-600' : 'text-red-600'}`}>
                          {book.availableCopies > 0 ? 'Available' : 'Not available'}
                        </span>
                      )}
                      <Link
                        to={`/books/${rec.bookId}`}
                        className="text-blue-600 hover:text-blue-700 text-sm font-medium"
                      >
                        View Details →
                      </Link>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default Recommendations;
