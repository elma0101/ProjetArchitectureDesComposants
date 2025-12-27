import { apiClient } from './apiClient';
import { LoginRequest, RegisterRequest, JwtResponse, User } from '../types/api.types';

export const authService = {
  async login(credentials: LoginRequest): Promise<JwtResponse> {
    const response = await apiClient.post<JwtResponse>('/api/auth/login', credentials);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  async register(data: RegisterRequest): Promise<JwtResponse> {
    const response = await apiClient.post<JwtResponse>('/api/auth/register', data);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  async refreshToken(refreshToken: string): Promise<JwtResponse> {
    const response = await apiClient.post<JwtResponse>('/api/auth/refresh', { refreshToken });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser(): JwtResponse | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  },

  getToken(): string | null {
    return localStorage.getItem('token');
  }
};
