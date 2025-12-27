import { apiClient } from './apiClient';
import { Notification, PageResponse } from '../types/api.types';

export const notificationService = {
  async getUserNotifications(userId: number, page: number = 0, size: number = 20): Promise<PageResponse<Notification>> {
    const response = await apiClient.get<PageResponse<Notification>>(`/api/notifications/user/${userId}`, {
      params: { page, size }
    });
    return response.data;
  },

  async getNotificationById(id: number): Promise<Notification> {
    const response = await apiClient.get<Notification>(`/api/notifications/${id}`);
    return response.data;
  },

  async markAsRead(id: number): Promise<void> {
    await apiClient.put(`/api/notifications/${id}/read`);
  }
};
