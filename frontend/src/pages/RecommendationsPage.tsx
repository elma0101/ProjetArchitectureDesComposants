import React, { useState, useEffect, useContext } from 'react';
import { Recommendation, RecommendationType } from '../types';
import { recommendationsAPI } from '../services/api';
import { AuthContext } from '../App';
import LoadingSpinner from '../components/Common/LoadingSpinner';
import BookCard from '../components/Books/BookCard';

const RecommendationsPage: React.FC = () => {
  const { user } = useContext(AuthContext);
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'personal' | 'popular' | 'trending'>('personal');

  useEffect(() => {
    fetchRecommendations();
  }, [activeTab, user]);

  const fetchRecommendations = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;
      
      switch (activeTab) {
        case 'personal':
          if (user) {
            response = await recommendationsAPI.getForUser(user.id.toString(), 20);
          } else {
            response = await recommendationsAPI.getPopular(20);
          }
          break;
        case 'popular':
          response = await recommendationsAPI.getPopular(20);
          break;
        case 'trending':
          response = await recommendationsAPI.getTrending(20);
          break;
        default:
          response = await recommendationsAPI.getPopular(20);
      }
      
      setRecommendations(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch recommendations');
    } finally {
      setLoading(false);
    }
  };

  const generatePersonalRecommendations = async () => {
    if (!user) return;
    
    try {
      setLoading(true);
      await recommendationsAPI.generateForUser(user.id.toString(), 20);
      await fetchRecommendations();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate recommendations');
      setLoading(false);
    }
  };

  const getRecommendationTypeIcon = (type: RecommendationType) => {
    switch (type) {
      case RecommendationType.COLLABORATIVE:
        return '👥';
      case RecommendationType.CONTENT_BASED:
        return '🎯';
      case RecommendationType.POPULAR:
        return '🔥';
      case RecommendationType.TRENDING:
        return '📈';
      default:
        return '📚';
    }
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading recommendations..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Book Recommendations</h1>
        {user && activeTab === 'personal' && (
          <button
            onClick={generatePersonalRecommendations}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            🔄 Generate New Recommendations
          </button>
        )}
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {[
            { key: 'personal', label: user ? 'For You' : 'Popular', icon: user ? '🎯' : '🔥' },
            { key: 'popular', label: 'Popular', icon: '🔥' },
            { key: 'trending', label: 'Trending', icon: '📈' }
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as any)}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === tab.key
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab.icon} {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {!user && activeTab === 'personal' && (
        <div className="bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded">
          <p>Sign in to get personalized recommendations based on your reading history!</p>
        </div>
      )}

      {recommendations.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-gray-500 text-lg mb-4">📚 No recommendations available</div>
          <p className="text-gray-400 mb-4">
            {activeTab === 'personal' && user
              ? 'Start borrowing books to get personalized recommendations!'
              : 'Check back later for new recommendations.'}
          </p>
          {user && activeTab === 'personal' && (
            <button
              onClick={generatePersonalRecommendations}
              className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
            >
              Generate Recommendations
            </button>
          )}
        </div>
      ) : (
        <div className="space-y-8">
          {/* Recommendation explanation */}
          <div className="bg-gray-50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-900 mb-2">
              {activeTab === 'personal' && user ? '🎯 Personalized for You' : 
               activeTab === 'popular' ? '🔥 Popular Books' : 
               '📈 Trending Now'}
            </h3>
            <p className="text-sm text-gray-600">
              {activeTab === 'personal' && user ? 
                'Based on your reading history and preferences' :
                activeTab === 'popular' ? 
                'Most borrowed books in our library' :
                'Books gaining popularity recently'}
            </p>
          </div>

          {/* Recommendations Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {recommendations.map((recommendation) => (
              <div key={recommendation.id} className="relative">
                <BookCard book={recommendation.book} />
                
                {/* Recommendation Badge */}
                <div className="absolute top-2 right-2 bg-white rounded-full p-1 shadow-md">
                  <div className="text-xs font-medium text-gray-600 flex items-center space-x-1">
                    <span>{getRecommendationTypeIcon(recommendation.type)}</span>
                    <span>{Math.round(recommendation.score * 100)}%</span>
                  </div>
                </div>
                
                {/* Recommendation Reason */}
                {recommendation.reason && (
                  <div className="mt-2 p-2 bg-blue-50 rounded text-xs text-blue-800">
                    💡 {recommendation.reason}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default RecommendationsPage;