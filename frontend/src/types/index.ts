// Common types for the application
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: Role[];
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER',
  LIBRARIAN = 'LIBRARIAN'
}

export interface Book {
  id: number;
  title: string;
  isbn: string;
  description?: string;
  publicationYear?: number;
  genre?: string;
  availableCopies: number;
  totalCopies: number;
  imageUrl?: string;
  authors: Author[];
  createdAt: string;
  updatedAt: string;
}

export interface Author {
  id: number;
  firstName: string;
  lastName: string;
  biography?: string;
  birthDate?: string;
  nationality?: string;
  books?: Book[];
  createdAt: string;
  updatedAt: string;
}

export interface Loan {
  id: number;
  book: Book;
  borrowerName: string;
  borrowerEmail: string;
  borrowerId?: string;
  loanDate: string;
  dueDate: string;
  returnDate?: string;
  status: LoanStatus;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum LoanStatus {
  ACTIVE = 'ACTIVE',
  RETURNED = 'RETURNED',
  OVERDUE = 'OVERDUE'
}

export interface Recommendation {
  id: number;
  userId: string;
  book: Book;
  score: number;
  reason: string;
  type: RecommendationType;
  createdAt: string;
}

export enum RecommendationType {
  COLLABORATIVE = 'COLLABORATIVE',
  CONTENT_BASED = 'CONTENT_BASED',
  POPULAR = 'POPULAR',
  TRENDING = 'TRENDING'
}

// Request DTOs
export interface BookCreateRequest {
  title: string;
  isbn: string;
  description?: string;
  publicationYear?: number;
  genre?: string;
  availableCopies: number;
  totalCopies: number;
  imageUrl?: string;
  authorIds?: number[];
}

export interface BookUpdateRequest {
  title?: string;
  description?: string;
  publicationYear?: number;
  genre?: string;
  availableCopies?: number;
  totalCopies?: number;
  imageUrl?: string;
}

export interface AuthorCreateRequest {
  firstName: string;
  lastName: string;
  biography?: string;
  birthDate?: string;
  nationality?: string;
}

export interface AuthorUpdateRequest {
  firstName?: string;
  lastName?: string;
  biography?: string;
  birthDate?: string;
  nationality?: string;
}

export interface BorrowBookRequest {
  bookId: number;
  borrowerName: string;
  borrowerEmail: string;
  borrowerId?: string;
  notes?: string;
}

export interface ReturnBookRequest {
  notes?: string;
}

export interface ExtendLoanRequest {
  additionalDays: number;
}

export interface SearchFilters {
  title?: string;
  author?: string;
  genre?: string;
  publicationYear?: number;
  availableOnly?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export interface LoanSearchFilters {
  borrowerEmail?: string;
  borrowerName?: string;
  status?: LoanStatus;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export interface AuthorSearchRequest {
  firstName?: string;
  lastName?: string;
  nationality?: string;
  birthYear?: number;
}

export interface BookStatistics {
  totalBooks: number;
  availableBooks: number;
  loanedBooks: number;
  totalAuthors: number;
  totalGenres: number;
  mostPopularGenre: string;
}

export interface LoanStatistics {
  totalLoans: number;
  activeLoans: number;
  overdueLoans: number;
  returnedLoans: number;
  averageLoanDuration: number;
}

export interface AuthorStatistics {
  totalBooks: number;
  totalLoans: number;
  averageRating: number;
  mostPopularBook: string;
}

// Authentication types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  username: string;
  email: string;
  roles: string[];
}