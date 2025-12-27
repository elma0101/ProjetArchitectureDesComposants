import { apiClient } from './apiClient';
import { Loan, PageResponse } from '../types/api.types';

export const loanService = {
  async createLoan(bookId: number): Promise<Loan> {
    const response = await apiClient.post<Loan>('/api/loans', { bookId });
    return response.data;
  },

  async getUserLoans(userId: number, page: number = 0, size: number = 20): Promise<PageResponse<Loan>> {
    const response = await apiClient.get<PageResponse<Loan>>(`/api/loans/user/${userId}`, {
      params: { page, size }
    });
    return response.data;
  },

  async getMyLoans(page: number = 0, size: number = 20): Promise<PageResponse<Loan>> {
    const response = await apiClient.get<PageResponse<Loan>>('/api/loans/my-loans', {
      params: { page, size }
    });
    return response.data;
  },

  async getLoanById(id: number): Promise<Loan> {
    const response = await apiClient.get<Loan>(`/api/loans/${id}`);
    return response.data;
  },

  async returnBook(loanId: number): Promise<Loan> {
    const response = await apiClient.put<Loan>(`/api/loans/${loanId}/return`);
    return response.data;
  },

  async getOverdueLoans(page: number = 0, size: number = 20): Promise<PageResponse<Loan>> {
    const response = await apiClient.get<PageResponse<Loan>>('/api/loans/overdue', {
      params: { page, size }
    });
    return response.data;
  },

  async renewLoan(loanId: number): Promise<Loan> {
    const response = await apiClient.put<Loan>(`/api/loans/${loanId}/renew`);
    return response.data;
  }
};
