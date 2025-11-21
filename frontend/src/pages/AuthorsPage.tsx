import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Author, PagedResponse } from '../types';
import { authorsAPI } from '../services/api';
import LoadingSpinner from '../components/Common/LoadingSpinner';
import Pagination from '../components/Common/Pagination';

const AuthorsPage: React.FC = () => {
  const [authors, setAuthors] = useState<PagedResponse<Author> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchAuthors();
  }, [page]);

  const fetchAuthors = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await authorsAPI.getAll(page, 20);
      setAuthors(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch authors');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchTerm.trim()) {
      fetchAuthors();
      return;
    }

    try {
      setLoading(true);
      const response = await authorsAPI.search({
        firstName: searchTerm,
        lastName: searchTerm
      }, 0, 20);
      setAuthors(response.data);
      setPage(0);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Search failed');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading authors..." />;
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">❌ {error}</div>
        <button
          onClick={fetchAuthors}
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
        <h1 className="text-3xl font-bold text-gray-900">Authors</h1>
      </div>

      {/* Search */}
      <form onSubmit={handleSearch} className="bg-white rounded-lg shadow-md p-6">
        <div className="flex space-x-4">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search authors by name..."
            className="flex-1 border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700"
          >
            Search
          </button>
        </div>
      </form>

      {authors && (
        <>
          <div className="text-sm text-gray-600 mb-4">
            Found {authors.totalElements} authors
          </div>

          {authors.content.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-gray-500 text-lg mb-4">👥 No authors found</div>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {authors.content.map((author) => (
                  <div key={author.id} className="bg-white rounded-lg shadow-md p-6">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">
                      <Link
                        to={`/authors/${author.id}`}
                        className="hover:text-blue-600 transition-colors"
                      >
                        {author.firstName} {author.lastName}
                      </Link>
                    </h3>
                    
                    {author.nationality && (
                      <p className="text-sm text-gray-600 mb-2">
                        🌍 {author.nationality}
                      </p>
                    )}
                    
                    {author.birthDate && (
                      <p className="text-sm text-gray-600 mb-2">
                        📅 Born: {new Date(author.birthDate).toLocaleDateString()}
                      </p>
                    )}
                    
                    {author.biography && (
                      <p className="text-sm text-gray-600 line-clamp-3 mb-3">
                        {author.biography}
                      </p>
                    )}
                    
                    <Link
                      to={`/authors/${author.id}`}
                      className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                    >
                      View Details →
                    </Link>
                  </div>
                ))}
              </div>

              <Pagination
                currentPage={authors.number}
                totalPages={authors.totalPages}
                totalElements={authors.totalElements}
                onPageChange={setPage}
              />
            </>
          )}
        </>
      )}
    </div>
  );
};

export default AuthorsPage;