import axios from 'axios';

const API_URL = 'http://localhost:8081/api/v1';

export interface ProjectDto {
  id?: number;
  name: string;
  description?: string;
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
  withCredentials: true,
});

const projectService = {
  getProjects: async (page = 0, size = 100, sort = 'name,asc'): Promise<PageResponse<ProjectDto>> => {
    const response = await apiClient.get(`/projects?page=${page}&size=${size}&sort=${sort}`);
    return response.data;
  },

  getProject: async (id: number): Promise<ProjectDto> => {
    const response = await apiClient.get(`/projects/${id}`);
    return response.data;
  },

  createProject: async (project: ProjectDto): Promise<ProjectDto> => {
    const response = await apiClient.post('/projects', project);
    return response.data;
  },

  updateProject: async (id: number, project: ProjectDto): Promise<ProjectDto> => {
    const response = await apiClient.put(`/projects/${id}`, project);
    return response.data;
  },

  deleteProject: async (id: number): Promise<void> => {
    await apiClient.delete(`/projects/${id}`);
  },

  searchProjects: async (query: string): Promise<ProjectDto[]> => {
    const response = await apiClient.get(`/projects/search?query=${encodeURIComponent(query)}`);
    return response.data;
  }
};

export default projectService; 