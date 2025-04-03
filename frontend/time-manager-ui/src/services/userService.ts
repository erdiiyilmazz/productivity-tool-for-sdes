import axios from 'axios';
import { UserResponse } from './authService';

const API_URL = 'http://localhost:8081/api/v1';

const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true,
});

const userService = {
  getAllUsers: async (): Promise<UserResponse[]> => {
    const response = await apiClient.get('/auth/users');
    return response.data;
  },

  getUserById: async (id: number): Promise<UserResponse> => {
    const response = await apiClient.get(`/auth/users/${id}`);
    return response.data;
  },

  getCurrentUser: async (): Promise<UserResponse> => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  }
};

export default userService; 