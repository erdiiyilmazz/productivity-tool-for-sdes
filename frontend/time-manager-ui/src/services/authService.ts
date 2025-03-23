import axios from 'axios';

// Define API base URL
const API_URL = 'http://localhost:8081/api/v1';

// Define types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
}

// Configure axios
const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true, // Include cookies for session-based auth
});

// Authentication service functions
const authService = {
  // Login function
  login: async (credentials: LoginRequest): Promise<UserResponse> => {
    const response = await apiClient.post('/auth/login', credentials);
    return response.data;
  },

  // Register function
  register: async (userData: RegisterRequest): Promise<UserResponse> => {
    const response = await apiClient.post('/auth/register', userData);
    return response.data;
  },

  // Logout function
  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },

  // Get current user function
  getCurrentUser: async (): Promise<UserResponse | null> => {
    try {
      const response = await apiClient.get('/auth/me');
      return response.data;
    } catch (error) {
      // If 401 Unauthorized, user is not logged in
      return null;
    }
  },

  // Check if user is authenticated
  isAuthenticated: async (): Promise<boolean> => {
    try {
      const response = await apiClient.get('/auth/me');
      return !!response.data;
    } catch (error) {
      return false;
    }
  }
};

export default authService; 