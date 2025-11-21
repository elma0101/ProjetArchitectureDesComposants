import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Author, Book } from '../types';
import { authorsAPI } from '../services/api';
import LoadingSpinner from '../components/Common/LoadingSpinner';
import BookCard from '../components/Books/BookCard';

const AuthorDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [author, setAuthor] = useState<Author | null>(null);
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchAuthorDetails(parseInt(id));
    }
  }, [id]);

  const fetchAuthorDetails = async (authorId: number) => {
    try {
      setLoading(true);
      setError(null);
      
      const [authorResponse, booksResponse] = await Promise.all([
        authorsAPI.getById(authorId),
        authorsAPI.getBooks(authorId)
      ]);
      
      setAuthor(authorResponse.data);
      setBooks(booksResponse.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch author details');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner size="large" message="Loading author details..." />;
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <div className="text-red-600 mb-4">❌ {error}</div>
        <Link
          to="/authors"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Back to Authors
        </Link>
      </div>
    );
  }

  if (!author) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-600 mb-4">Author not found</div>
        <Link
          to="/authors"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Back to Authors
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto">
      {/* Breadcrumb */}
      <nav className="mb-6">
        <Link to="/authors" className="text-blue-600 hover:text-blue-800">
          ← Back to Authors
        </Link>
      </nav>

      {/* Author Info */}
      <div className="bg-white rounded-lg shadow-lg p-8 mb-8">
        <div className="flex items-start space-x-6">
          <div className="w-32 h-32 bg-gray-200 rounded-full flex items-center justify-center text-4xl">
            👤
          </div>
          
          <div className="flex-1">
            <h1 className="text-3xl font-bold text-gray-900 mb-4">
              {author.firstName} {author.lastName}
            </h1>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              {author.nationality && (
                <div>
                  <span className="font-semibold text-gray-700">Nationality:</span>
                  <span className="ml-2 text-gray-600">{author.nationality}</span>
                </div>
              )}
              
              {author.birthDate && (
                <div>
                  <span className="font-semibold text-gray-700">Birth Date:</span>
                  <span className="ml-2 text-gray-600">
                    {new Date(author.birthDate).toLocaleDateString()}
                  </span>
                </div>
              )}
              
              <div>
                <span className="font-semibold text-gray-700">Books:</span>
                <span className="ml-2 text-gray-600">{books.length}</span>
              </div>
            </div>
            
            {author.biography && (
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">Biography</h3>
                <p className="text-gray-600 leading-relaxed">{author.biography}</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Books by Author */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-6">
          Books by {author.firstName} {author.lastName}
        </h2>
        
        {books.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg shadow-md">
            <div className="text-gray-500 text-lg mb-4">📚 No books found</div>
            <p className="text-gray-400">This author doesn't have any books in our system yet.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AuthorDetailPage;