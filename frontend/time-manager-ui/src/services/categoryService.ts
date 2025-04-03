import axios from 'axios';

const API_URL = 'http://localhost:8081/api/v1';

export interface CategoryDto {
  id?: number;
  name: string;
  description?: string;
  projectId: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    }
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  numberOfElements: number;
  empty: boolean;
}

const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true, // Include cookies for session-based auth
});

const categoryService = {
  getCategoriesByProject: async (projectId: number, page = 0, size = 100, sort = 'name,asc'): Promise<PageResponse<CategoryDto>> => {
    const response = await apiClient.get(`/projects/${projectId}/categories?page=${page}&size=${size}&sort=${sort}`);
    return response.data;
  },

  getCategory: async (projectId: number, categoryId: number): Promise<CategoryDto> => {
    const response = await apiClient.get(`/projects/${projectId}/categories/${categoryId}`);
    return response.data;
  },

  createCategory: async (projectId: number, category: CategoryDto): Promise<CategoryDto> => {
    const response = await apiClient.post(`/projects/${projectId}/categories`, category);
    return response.data;
  },

  updateCategory: async (projectId: number, categoryId: number, category: CategoryDto): Promise<CategoryDto> => {
    const response = await apiClient.put(`/projects/${projectId}/categories/${categoryId}`, category);
    return response.data;
  },

  deleteCategory: async (projectId: number, categoryId: number): Promise<void> => {
    await apiClient.delete(`/projects/${projectId}/categories/${categoryId}`);
  },

  searchCategories: async (projectId: number, query: string): Promise<CategoryDto[]> => {
    const response = await apiClient.get(`/projects/${projectId}/categories/search?query=${encodeURIComponent(query)}`);
    return response.data;
  },

  getAllCategories: async (): Promise<CategoryDto[]> => {
    return [];
  }
};

export default categoryService; 