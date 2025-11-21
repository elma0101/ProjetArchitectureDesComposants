import axios, { AxiosResponse } from 'axios';
import {
  Book,
  Author,
  Loan,
  Recommendation,
  PagedResponse,
  BookCreateRequest,
  BookUpdateRequest,
  AuthorCreateRequest,
  AuthorUpdateRequest,
  BorrowBookRequest,
  ReturnBookRequest,
  ExtendLoanRequest,
  SearchFilters,
  LoanSearchFilters,
  AuthorSearchRequest,
  BookStatistics,
  LoanStatistics,
  AuthorStatistics,
  LoginRequest,
  RegisterRequest,
  JwtResponse,
  RecommendationType,
  LoanStatus
} from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Authentication API
export const authAPI = {
  login: (credentials: LoginRequest): Promise<AxiosResponse<JwtResponse>> =>
    api.post('/auth/login', credentials),
  
  register: (userData: RegisterRequest): Promise<AxiosResponse<any>> =>
    api.post('/auth/register', userData),
  
  logout: (): Promise<AxiosResponse<any>> =>
    api.post('/auth/logout'),
  
  getProfile: (): Promise<AxiosResponse<any>> =>
    api.get('/auth/profile'),
};

// Books API
export const booksAPI = {
  // Basic CRUD operations (Spring Data REST endpoints)
  getAll: (page = 0, size = 20, sort = 'title'): Promise<AxiosResponse<PagedResponse<Book>>> =>
    api.get(`/books?page=${page}&size=${size}&sort=${sort}`),
  
  getById: (id: number): Promise<AxiosResponse<Book>> =>
    api.get(`/books/${id}`),
  
  create: (book: BookCreateRequest): Promise<AxiosResponse<Book>> =>
    api.post('/books', book),
  
  update: (id: number, book: BookUpdateRequest): Promise<AxiosResponse<Book>> =>
    api.put(`/books/${id}`, book),
  
  delete: (id: number): Promise<AxiosResponse<void>> =>
    api.delete(`/books/${id}`),
  
  // Search operations
  searchByTitle: (title: string, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Book>>> =>
    api.get(`/books/search/findByTitle?title=${encodeURIComponent(title)}&page=${page}&size=${size}`),
  
  searchByAuthor: (author: string, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Book>>> =>
    api.get(`/books/search/findByAuthor?author=${encodeURIComponent(author)}&page=${page}&size=${size}`),
  
  searchByIsbn: (isbn: string): Promise<AxiosResponse<Book>> =>
    api.get(`/books/search/findByIsbn?isbn=${encodeURIComponent(isbn)}`),
  
  searchByGenre: (genre: string, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Book>>> =>
    api.get(`/books/search/findByGenre?genre=${encodeURIComponent(genre)}&page=${page}&size=${size}`),
  
  advancedSearch: (filters: SearchFilters): Promise<AxiosResponse<PagedResponse<Book>>> => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value.toString());
      }
    });
    return api.get(`/books/search/advanced?${params.toString()}`);
  },
  
  getAvailable: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Book>>> =>
    api.get(`/books/search/available?page=${page}&size=${size}`),
  
  getPopular: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Book>>> =>
    api.get(`/books/search/popular?page=${page}&size=${size}`),
  
  getRecent: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Book>>> =>
    api.get(`/books/search/recent?page=${page}&size=${size}`),
  
  // Advanced book management
  bulkCreate: (books: BookCreateRequest[]): Promise<AxiosResponse<any>> =>
    api.post('/advanced-books/bulk', { books }),
  
  getStatistics: (): Promise<AxiosResponse<BookStatistics>> =>
    api.get('/advanced-books/statistics'),
  
  getAvailabilityStatus: (id: number): Promise<AxiosResponse<any>> =>
    api.get(`/advanced-books/${id}/availability`),
};

// Authors API
export const authorsAPI = {
  getAll: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Author>>> =>
    api.get(`/authors?page=${page}&size=${size}`),
  
  getById: (id: number): Promise<AxiosResponse<Author>> =>
    api.get(`/authors/${id}`),
  
  create: (author: AuthorCreateRequest): Promise<AxiosResponse<Author>> =>
    api.post('/authors', author),
  
  update: (id: number, author: AuthorUpdateRequest): Promise<AxiosResponse<Author>> =>
    api.put(`/authors/${id}`, author),
  
  delete: (id: number): Promise<AxiosResponse<void>> =>
    api.delete(`/authors/${id}`),
  
  getBooks: (id: number): Promise<AxiosResponse<Book[]>> =>
    api.get(`/authors/${id}/books`),
  
  addBooks: (id: number, bookIds: number[]): Promise<AxiosResponse<Author>> =>
    api.post(`/authors/${id}/books`, { bookIds }),
  
  removeBooks: (id: number, bookIds: number[]): Promise<AxiosResponse<Author>> =>
    api.delete(`/authors/${id}/books`, { data: { bookIds } }),
  
  search: (searchRequest: AuthorSearchRequest, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Author>>> =>
    api.post(`/authors/search?page=${page}&size=${size}`, searchRequest),
  
  getStatistics: (id: number): Promise<AxiosResponse<AuthorStatistics>> =>
    api.get(`/authors/${id}/statistics`),
  
  getMostProlific: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Author>>> =>
    api.get(`/authors/prolific?page=${page}&size=${size}`),
  
  getByNationality: (nationality: string, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Author>>> =>
    api.get(`/authors/nationality/${encodeURIComponent(nationality)}?page=${page}&size=${size}`),
};

// Loans API
export const loansAPI = {
  // Basic loan operations
  borrow: (request: BorrowBookRequest): Promise<AxiosResponse<Loan>> =>
    api.post('/loan-management/borrow', request),
  
  return: (loanId: number, request?: ReturnBookRequest): Promise<AxiosResponse<Loan>> =>
    api.put(`/loan-management/${loanId}/return`, request || {}),
  
  extend: (loanId: number, request: ExtendLoanRequest): Promise<AxiosResponse<Loan>> =>
    api.put(`/loan-management/${loanId}/extend`, request),
  
  getById: (loanId: number): Promise<AxiosResponse<Loan>> =>
    api.get(`/loan-management/${loanId}`),
  
  // Loan queries
  getActive: (page = 0, size = 20, sortBy = 'dueDate', sortDir = 'asc'): Promise<AxiosResponse<PagedResponse<Loan>>> =>
    api.get(`/loan-management/active?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),
  
  getOverdue: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Loan>>> =>
    api.get(`/loan-management/overdue?page=${page}&size=${size}`),
  
  getByBorrowerEmail: (email: string, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Loan>>> =>
    api.get(`/loan-management/borrower/${encodeURIComponent(email)}?page=${page}&size=${size}`),
  
  getLoanHistory: (email: string, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Loan>>> =>
    api.get(`/loan-management/history/${encodeURIComponent(email)}?page=${page}&size=${size}`),
  
  search: (filters: LoanSearchFilters): Promise<AxiosResponse<PagedResponse<Loan>>> => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value.toString());
      }
    });
    return api.get(`/loan-management/search?${params.toString()}`);
  },
  
  getDueToday: (): Promise<AxiosResponse<Loan[]>> =>
    api.get('/loan-management/due-today'),
  
  getDueWithin: (days: number): Promise<AxiosResponse<Loan[]>> =>
    api.get(`/loan-management/due-within?days=${days}`),
  
  getBookStatus: (bookId: number): Promise<AxiosResponse<any>> =>
    api.get(`/loan-management/book/${bookId}/status`),
  
  // Statistics and analytics
  getStatistics: (): Promise<AxiosResponse<LoanStatistics>> =>
    api.get('/loan-management/statistics'),
  
  getMostBorrowedBooks: (page = 0, size = 10): Promise<AxiosResponse<PagedResponse<any>>> =>
    api.get(`/loan-management/statistics/most-borrowed-books?page=${page}&size=${size}`),
  
  getMostActiveBorrowers: (page = 0, size = 10): Promise<AxiosResponse<PagedResponse<any>>> =>
    api.get(`/loan-management/statistics/most-active-borrowers?page=${page}&size=${size}`),
  
  updateOverdue: (): Promise<AxiosResponse<any>> =>
    api.post('/loan-management/update-overdue'),
};

// Recommendations API
export const recommendationsAPI = {
  getForUser: (userId: string, limit = 10): Promise<AxiosResponse<Recommendation[]>> =>
    api.get(`/recommendations/${userId}?limit=${limit}`),
  
  generateForUser: (userId: string, limit = 10): Promise<AxiosResponse<Recommendation[]>> =>
    api.post(`/recommendations/${userId}/generate?limit=${limit}`),
  
  getSavedForUser: (userId: string, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Recommendation>>> =>
    api.get(`/recommendations/${userId}/saved?page=${page}&size=${size}`),
  
  getPopular: (limit = 10): Promise<AxiosResponse<Recommendation[]>> =>
    api.get(`/recommendations/popular?limit=${limit}`),
  
  getTrending: (limit = 10): Promise<AxiosResponse<Recommendation[]>> =>
    api.get(`/recommendations/trending?limit=${limit}`),
  
  getCollaborative: (userId: string, limit = 10): Promise<AxiosResponse<Recommendation[]>> =>
    api.get(`/recommendations/collaborative/${userId}?limit=${limit}`),
  
  getContentBased: (userId: string, limit = 10): Promise<AxiosResponse<Recommendation[]>> =>
    api.get(`/recommendations/content-based/${userId}?limit=${limit}`),
  
  getByType: (type: RecommendationType, page = 0, size = 20): Promise<AxiosResponse<PagedResponse<Recommendation>>> =>
    api.get(`/recommendations/type/${type}?page=${page}&size=${size}`),
  
  cleanup: (userId: string, daysToKeep = 30): Promise<AxiosResponse<void>> =>
    api.delete(`/recommendations/${userId}/cleanup?daysToKeep=${daysToKeep}`),
};

// Admin API
export const adminAPI = {
  getUsers: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<any>>> =>
    api.get(`/admin/users?page=${page}&size=${size}`),
  
  updateUserRole: (userId: number, roles: string[]): Promise<AxiosResponse<any>> =>
    api.put(`/admin/users/${userId}/roles`, { roles }),
  
  getSystemHealth: (): Promise<AxiosResponse<any>> =>
    api.get('/admin/health'),
  
  getAuditLogs: (page = 0, size = 20): Promise<AxiosResponse<PagedResponse<any>>> =>
    api.get(`/admin/audit-logs?page=${page}&size=${size}`),
};

export default api;