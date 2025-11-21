import React, { useState, useEffect } from 'react';
import { Book, SearchFilters, PagedResponse } from '../../types';
import { booksAPI } from '../../services/api';
import LoadingSpinner from '../Common/LoadingSpinner';
import Pagination from '../Common/Pagination';
import BookCard from './BookCard';
import BookSearch from './BookSearch';

const BookList: React.FC = () => {
  const [books, setBooks] = useState<PagedResponse<Book> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<SearchFilters>({
    page: 0,
    size: 20,
    sortBy: 'title',
    sortDir: 'asc'
  });

  const fetchBooks = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;
      if (filters.title || filters.author || filters.genre || filters.publicationYear || filters.availableOnly) {
        response = await booksAPI.advancedSearch(filters);
      } else {
        response = await booksAPI.getAll(filters.page, filters.size, filters.sortBy);
      }
      
      setBooks(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch books');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBooks();
  }, [filters]);

  const handleSearch = (searchFilters: SearchFilters) => {
    setFilters({ ...searchFilters, page: 0 });
  };

  const handlePageChange = (page: number) => {
    setFilters({ ...filters, page });
  };

  const handleSortChange = (sortBy: string, sortDir: 'asc' | 'desc') => {
    setFilters({ ...filters, sortBy, sortDir, page: 0 });
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading books..." />;
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">❌ {error}</div>
        <button
          onClick={fetchBooks}
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
        <h1 className="text-3xl font-bold text-gray-900">Books</h1>
        <div className="flex space-x-2">
          <select
            value={`${filters.sortBy}-${filters.sortDir}`}
            onChange={(e) => {
              const [sortBy, sortDir] = e.target.value.split('-');
              handleSortChange(sortBy, sortDir as 'asc' | 'desc');
            }}
            className="border border-gray-300 rounded-md px-3 py-2"
          >
            <option value="title-asc">Title A-Z</option>
            <option value="title-desc">Title Z-A</option>
            <option value="publicationYear-desc">Newest First</option>
            <option value="publicationYear-asc">Oldest First</option>
            <option value="availableCopies-desc">Most Available</option>
          </select>
        </div>
      </div>

      <BookSearch onSearch={handleSearch} />

      {books && (
        <>
          <div className="text-sm text-gray-600 mb-4">
            Found {books.totalElements} books
          </div>

          {books.content.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-gray-500 text-lg mb-4">📚 No books found</div>
              <p className="text-gray-400">Try adjusting your search criteria</p>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {books.content.map((book) => (
                  <BookCard key={book.id} book={book} />
                ))}
              </div>

              <Pagination
                currentPage={books.number}
                totalPages={books.totalPages}
                totalElements={books.totalElements}
                onPageChange={handlePageChange}
              />
            </>
          )}
        </>
      )}
    </div>
  );
};

export default BookList;