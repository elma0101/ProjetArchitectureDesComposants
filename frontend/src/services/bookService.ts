import { apiClient } from './apiClient';
import { Book, Author, PageResponse } from '../types/api.types';

export const bookService = {
  async getBooks(page: number = 0, size: number = 20, search?: string, genre?: string): Promise<PageResponse<Book>> {
    const params: any = { page, size };
    if (search) params.search = search;
    if (genre) params.genre = genre;
    
    const response = await apiClient.get<PageResponse<Book>>('/api/books', { params });
    return response.data;
  },

  async getBookById(id: number): Promise<Book> {
    const response = await apiClient.get<Book>(`/api/books/${id}`);
    return response.data;
  },

  async searchBooks(query: string, page: number = 0, size: number = 20): Promise<PageResponse<Book>> {
    const response = await apiClient.get<PageResponse<Book>>('/api/books/search', {
      params: { q: query, page, size }
    });
    return response.data;
  },

  async getBooksByGenre(genre: string, page: number = 0, size: number = 20): Promise<PageResponse<Book>> {
    const response = await apiClient.get<PageResponse<Book>>('/api/books', {
      params: { genre, page, size }
    });
    return response.data;
  },

  async getBooksByAuthor(authorId: number, page: number = 0, size: number = 20): Promise<PageResponse<Book>> {
    const response = await apiClient.get<PageResponse<Book>>(`/api/authors/${authorId}/books`, {
      params: { page, size }
    });
    return response.data;
  },

  async getAuthors(page: number = 0, size: number = 20): Promise<PageResponse<Author>> {
    const response = await apiClient.get<PageResponse<Author>>('/api/authors', {
      params: { page, size }
    });
    return response.data;
  },

  async getAuthorById(id: number): Promise<Author> {
    const response = await apiClient.get<Author>(`/api/authors/${id}`);
    return response.data;
  },

  async searchAuthors(query: string, page: number = 0, size: number = 20): Promise<PageResponse<Author>> {
    const response = await apiClient.get<PageResponse<Author>>('/api/authors/search', {
      params: { q: query, page, size }
    });
    return response.data;
  }
};
