// API Types for Microservices

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export enum Role {
  USER = 'USER',
  LIBRARIAN = 'LIBRARIAN',
  ADMIN = 'ADMIN'
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface JwtResponse {
  token: string;
  refreshToken: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: Role[];
}

export interface Author {
  id: number;
  firstName: string;
  lastName: string;
  biography?: string;
  birthDate?: string;
  nationality?: string;
  bookCount?: number;
  createdAt: string;
  updatedAt: string;
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

export interface Loan {
  id: number;
  userId: number;
  bookId: number;
  loanDate: string;
  dueDate: string;
  returnDate?: string;
  status: LoanStatus;
  createdAt: string;
  updatedAt: string;
  book?: Book;
  user?: User;
}

export enum LoanStatus {
  ACTIVE = 'ACTIVE',
  RETURNED = 'RETURNED',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED'
}

export interface Recommendation {
  id: string;
  userId: number;
  bookId: number;
  type: RecommendationType;
  score: number;
  reason?: string;
  createdAt: string;
  book?: Book;
}

export enum RecommendationType {
  POPULAR = 'POPULAR',
  TRENDING = 'TRENDING',
  CONTENT_BASED = 'CONTENT_BASED',
  COLLABORATIVE = 'COLLABORATIVE'
}

export interface Notification {
  id: number;
  userId: number;
  type: NotificationType;
  recipient: string;
  subject: string;
  content: string;
  status: NotificationStatus;
  createdAt: string;
  sentAt?: string;
}

export enum NotificationType {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  PUSH = 'PUSH',
  IN_APP = 'IN_APP'
}

export enum NotificationStatus {
  PENDING = 'PENDING',
  SENT = 'SENT',
  FAILED = 'FAILED',
  RETRYING = 'RETRYING'
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
