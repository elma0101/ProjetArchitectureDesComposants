import React from 'react';
import { Link } from 'react-router-dom';
import { Book } from '../../types';

interface BookCardProps {
  book: Book;
  showActions?: boolean;
  onBorrow?: (book: Book) => void;
}

const BookCard: React.FC<BookCardProps> = ({ book, showActions = true, onBorrow }) => {
  const formatAuthors = (authors: any[]) => {
    if (!authors || authors.length === 0) return 'Unknown Author';
    return authors.map(author => `${author.firstName} ${author.lastName}`).join(', ');
  };

  const getAvailabilityStatus = () => {
    if (book.availableCopies > 0) {
      return {
        text: `${book.availableCopies} available`,
        color: 'text-green-600',
        bgColor: 'bg-green-100'
      };
    } else {
      return {
        text: 'Not available',
        color: 'text-red-600',
        bgColor: 'bg-red-100'
      };
    }
  };

  const availability = getAvailabilityStatus();

  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 overflow-hidden">
      {/* Book Image */}
      <div className="h-48 bg-gray-200 flex items-center justify-center">
        {book.imageUrl ? (
          <img
            src={book.imageUrl}
            alt={book.title}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="text-gray-400 text-4xl">📖</div>
        )}
      </div>

      {/* Book Details */}
      <div className="p-4">
        <div className="mb-2">
          <Link
            to={`/books/${book.id}`}
            className="text-lg font-semibold text-gray-900 hover:text-blue-600 transition-colors line-clamp-2"
          >
            {book.title}
          </Link>
        </div>

        <div className="text-sm text-gray-600 mb-2">
          by {formatAuthors(book.authors)}
        </div>

        {book.genre && (
          <div className="text-xs text-gray-500 mb-2">
            📚 {book.genre}
          </div>
        )}

        {book.publicationYear && (
          <div className="text-xs text-gray-500 mb-3">
            📅 {book.publicationYear}
          </div>
        )}

        {/* Availability Status */}
        <div className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${availability.bgColor} ${availability.color} mb-3`}>
          {availability.text}
        </div>

        {/* Description */}
        {book.description && (
          <p className="text-sm text-gray-600 line-clamp-3 mb-3">
            {book.description}
          </p>
        )}

        {/* Actions */}
        {showActions && (
          <div className="flex space-x-2">
            <Link
              to={`/books/${book.id}`}
              className="flex-1 bg-blue-600 text-white text-center py-2 px-3 rounded text-sm hover:bg-blue-700 transition-colors"
            >
              View Details
            </Link>
            {book.availableCopies > 0 && onBorrow && (
              <button
                onClick={() => onBorrow(book)}
                className="bg-green-600 text-white py-2 px-3 rounded text-sm hover:bg-green-700 transition-colors"
              >
                Borrow
              </button>
            )}
          </div>
        )}

        {/* Book Info */}
        <div className="mt-3 pt-3 border-t border-gray-200 text-xs text-gray-500">
          <div className="flex justify-between">
            <span>ISBN: {book.isbn}</span>
            <span>{book.totalCopies} total copies</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookCard;