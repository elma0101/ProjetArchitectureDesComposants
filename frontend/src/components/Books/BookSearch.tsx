import React, { useState } from 'react';
import { SearchFilters } from '../../types';

interface BookSearchProps {
  onSearch: (filters: SearchFilters) => void;
}

const BookSearch: React.FC<BookSearchProps> = ({ onSearch }) => {
  const [filters, setFilters] = useState<SearchFilters>({
    title: '',
    author: '',
    genre: '',
    publicationYear: undefined,
    availableOnly: false,
    size: 20,
    sortBy: 'title',
    sortDir: 'asc'
  });

  const [isAdvancedOpen, setIsAdvancedOpen] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch(filters);
  };

  const handleReset = () => {
    const resetFilters: SearchFilters = {
      title: '',
      author: '',
      genre: '',
      publicationYear: undefined,
      availableOnly: false,
      size: 20,
      sortBy: 'title',
      sortDir: 'asc'
    };
    setFilters(resetFilters);
    onSearch(resetFilters);
  };

  const handleInputChange = (field: keyof SearchFilters, value: any) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Basic Search */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
              Title
            </label>
            <input
              type="text"
              id="title"
              value={filters.title || ''}
              onChange={(e) => handleInputChange('title', e.target.value)}
              placeholder="Search by title..."
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label htmlFor="author" className="block text-sm font-medium text-gray-700 mb-1">
              Author
            </label>
            <input
              type="text"
              id="author"
              value={filters.author || ''}
              onChange={(e) => handleInputChange('author', e.target.value)}
              placeholder="Search by author..."
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label htmlFor="genre" className="block text-sm font-medium text-gray-700 mb-1">
              Genre
            </label>
            <input
              type="text"
              id="genre"
              value={filters.genre || ''}
              onChange={(e) => handleInputChange('genre', e.target.value)}
              placeholder="Search by genre..."
              className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
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
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-gray-200">
            <div>
              <label htmlFor="publicationYear" className="block text-sm font-medium text-gray-700 mb-1">
                Publication Year
              </label>
              <input
                type="number"
                id="publicationYear"
                value={filters.publicationYear || ''}
                onChange={(e) => handleInputChange('publicationYear', e.target.value ? parseInt(e.target.value) : undefined)}
                placeholder="e.g., 2020"
                min="1000"
                max="2100"
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
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

            <div className="flex items-center">
              <input
                type="checkbox"
                id="availableOnly"
                checked={filters.availableOnly || false}
                onChange={(e) => handleInputChange('availableOnly', e.target.checked)}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="availableOnly" className="ml-2 block text-sm text-gray-700">
                Available books only
              </label>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex space-x-3 pt-4">
          <button
            type="submit"
            className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition-colors"
          >
            🔍 Search
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

export default BookSearch;