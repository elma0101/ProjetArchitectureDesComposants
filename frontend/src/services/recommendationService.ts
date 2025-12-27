import { apiClient } from './apiClient';
import { Recommendation, RecommendationType } from '../types/api.types';

export const recommendationService = {
  async getRecommendations(userId: number): Promise<Recommendation[]> {
    const response = await apiClient.get<Recommendation[]>(`/api/recommendations/user/${userId}`);
    return response.data;
  },

  async getRecommendationsByType(userId: number, type: RecommendationType): Promise<Recommendation[]> {
    const response = await apiClient.get<Recommendation[]>(`/api/recommendations/user/${userId}/type/${type}`);
    return response.data;
  },

  async refreshRecommendations(userId: number): Promise<Recommendation[]> {
    const response = await apiClient.post<Recommendation[]>(`/api/recommendations/user/${userId}/refresh`);
    return response.data;
  },

  async submitFeedback(bookId: number, rating: number): Promise<void> {
    await apiClient.post('/api/preferences/feedback', { bookId, rating });
  },

  async addFavoriteAuthor(userId: number, authorId: number): Promise<void> {
    await apiClient.post(`/api/preferences/user/${userId}/favorite-author/${authorId}`);
  },

  async addFavoriteCategory(userId: number, category: string): Promise<void> {
    await apiClient.post(`/api/preferences/user/${userId}/favorite-category`, null, {
      params: { category }
    });
  }
};
