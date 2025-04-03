import axios from 'axios';

const API_URL = 'http://localhost:8081/api/v1';

export interface TaskDto {
  id?: number;
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: Priority;
  dueDate?: string;
  estimatedHours?: number;
  actualHours?: number;
  projectId?: number | null;
  categoryId?: number | null;
  creatorId?: number | null;
  ownerId?: number | null;
  assigneeId?: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE',
  BLOCKED = 'BLOCKED',
  REVIEW = 'REVIEW'
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
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
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

const taskService = {
  getTasks: async (page = 0, size = 10, sort = 'createdAt,desc'): Promise<PageResponse<TaskDto>> => {
    const response = await apiClient.get(`/tasks?page=${page}&size=${size}&sort=${sort}`);
    return response.data;
  },

  getTask: async (id: number): Promise<TaskDto> => {
    const response = await apiClient.get(`/tasks/${id}`);
    return response.data;
  },

  createTask: async (task: TaskDto): Promise<TaskDto> => {
    console.log('Creating task with data:', JSON.stringify(task, null, 2));
    try {
      const taskToSend = {
        ...task,
        dueDate: task.dueDate ? new Date(task.dueDate).toISOString() : null
      };
      
      const payload = JSON.stringify(taskToSend);
      console.log('Final JSON payload:', payload);
      
      const response = await apiClient.post('/tasks', taskToSend);
      return response.data;
    } catch (error: any) {
      console.error('Error creating task:', error.message);
      if (error.response) {
        console.error('Server responded with status:', error.response.status);
        console.error('Response data:', error.response.data);
        
        if (error.response.data && error.response.data.errors) {
          console.error('Validation errors:', error.response.data.errors);
        }
      }
      throw error;
    }
  },

  updateTask: async (id: number, task: TaskDto): Promise<TaskDto> => {
    try {
      const taskToSend = {
        ...task,
        dueDate: task.dueDate ? new Date(task.dueDate).toISOString() : null
      };
      const response = await apiClient.put(`/tasks/${id}`, taskToSend);
      return response.data;
    } catch (error: any) {
      console.error('Error updating task:', error.message);
      if (error.response) {
        console.error('Server responded with status:', error.response.status);
        console.error('Response data:', error.response.data);
      }
      throw error;
    }
  },

  deleteTask: async (id: number): Promise<void> => {
    await apiClient.delete(`/tasks/${id}`);
  },

  searchTasks: async (query: string): Promise<TaskDto[]> => {
    const response = await apiClient.get(`/tasks/search?query=${encodeURIComponent(query)}`);
    return response.data;
  },

  getTasksByProject: async (projectId: number, page = 0, size = 10, sort = 'createdAt,desc'): Promise<PageResponse<TaskDto>> => {
    const response = await apiClient.get(`/tasks/project/${projectId}?page=${page}&size=${size}&sort=${sort}`);
    return response.data;
  },

  getTasksByCategory: async (categoryId: number, page = 0, size = 10, sort = 'createdAt,desc'): Promise<PageResponse<TaskDto>> => {
    const response = await apiClient.get(`/tasks/category/${categoryId}?page=${page}&size=${size}&sort=${sort}`);
    return response.data;
  },

  getTasksByStatus: async (status: TaskStatus, page = 0, size = 10): Promise<PageResponse<TaskDto>> => {
    const response = await apiClient.get(`/tasks/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },

  getTasksByPriority: async (priority: Priority, page = 0, size = 10): Promise<PageResponse<TaskDto>> => {
    const response = await apiClient.get(`/tasks/priority/${priority}?page=${page}&size=${size}`);
    return response.data;
  },

  getOverdueTasks: async (page = 0, size = 10): Promise<PageResponse<TaskDto>> => {
    const response = await apiClient.get(`/tasks/overdue?page=${page}&size=${size}`);
    return response.data;
  }
};

export default taskService; 