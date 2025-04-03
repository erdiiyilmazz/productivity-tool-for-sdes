import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Container, 
  Typography, 
  Button, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  TablePagination,
  IconButton,
  Chip,
  Tooltip,
  TextField,
  MenuItem,
  Grid,
  CircularProgress,
  Fab
} from '@mui/material';
import { 
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  Refresh as RefreshIcon,
  FilterList as FilterIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import taskService, { TaskDto, TaskStatus, Priority, PageResponse } from '../services/taskService';

const Tasks: React.FC = () => {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortField, setSortField] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState('desc');
  const [statusFilter, setStatusFilter] = useState<TaskStatus | ''>('');
  const [priorityFilter, setPriorityFilter] = useState<Priority | ''>('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchTasks();
  }, [page, rowsPerPage, sortField, sortDirection, statusFilter, priorityFilter]);

  const fetchTasks = async () => {
    setLoading(true);
    setError(null);
    try {
      let response: PageResponse<TaskDto>;
      
      if (statusFilter) {
        response = await taskService.getTasksByStatus(statusFilter as TaskStatus, page, rowsPerPage);
      } else if (priorityFilter) {
        response = await taskService.getTasksByPriority(priorityFilter as Priority, page, rowsPerPage);
      } else {
        response = await taskService.getTasks(
          page, 
          rowsPerPage, 
          `${sortField},${sortDirection}`
        );
      }
      
      setTasks(response.content);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error('Error fetching tasks:', err);
      setError('Failed to load tasks. Please try again later.');
      setTasks([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      fetchTasks();
      return;
    }
    
    setLoading(true);
    try {
      const results = await taskService.searchTasks(searchQuery);
      setTasks(results);
      setTotalElements(results.length);
    } catch (err) {
      console.error('Error searching tasks:', err);
      setError('Failed to search tasks. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSort = (field: string) => {
    if (field === sortField) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleResetFilters = () => {
    setStatusFilter('');
    setPriorityFilter('');
    setSearchQuery('');
    setSortField('createdAt');
    setSortDirection('desc');
    setPage(0);
  };

  const navigateToCreateTask = () => {
    navigate('/tasks/create');
  };

  const navigateToEditTask = (id: number) => {
    navigate(`/tasks/edit/${id}`);
  };

  const handleDeleteTask = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await taskService.deleteTask(id);
        fetchTasks();
      } catch (err) {
        console.error('Error deleting task:', err);
        setError('Failed to delete task. Please try again.');
      }
    }
  };

  const getStatusColor = (status: TaskStatus) => {
    switch (status) {
      case TaskStatus.TODO:
        return 'default';
      case TaskStatus.IN_PROGRESS:
        return 'primary';
      case TaskStatus.DONE:
        return 'success';
      case TaskStatus.BLOCKED:
        return 'error';
      case TaskStatus.REVIEW:
        return 'warning';
      default:
        return 'default';
    }
  };

  const getPriorityColor = (priority: Priority) => {
    switch (priority) {
      case Priority.LOW:
        return 'success';
      case Priority.MEDIUM:
        return 'info';
      case Priority.HIGH:
        return 'warning';
      case Priority.URGENT:
        return 'error';
      default:
        return 'default';
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  return (
    <Container maxWidth="xl">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom component="div">
          Task Management
        </Typography>

        <Paper sx={{ p: 2, mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={6}>
              <form onSubmit={handleSearchSubmit}>
                <Box sx={{ display: 'flex' }}>
                  <TextField
                    fullWidth
                    variant="outlined"
                    size="small"
                    placeholder="Search tasks by title..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    sx={{ mr: 1 }}
                  />
                  <Button 
                    type="submit" 
                    variant="contained" 
                    startIcon={<SearchIcon />}
                  >
                    Search
                  </Button>
                </Box>
              </form>
            </Grid>
            <Grid item xs={6} md={2}>
              <TextField
                select
                fullWidth
                size="small"
                label="Status"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as TaskStatus)}
              >
                <MenuItem value="">All Statuses</MenuItem>
                {Object.values(TaskStatus).map((status) => (
                  <MenuItem key={status} value={status}>
                    {status.replace('_', ' ')}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={6} md={2}>
              <TextField
                select
                fullWidth
                size="small"
                label="Priority"
                value={priorityFilter}
                onChange={(e) => setPriorityFilter(e.target.value as Priority)}
              >
                <MenuItem value="">All Priorities</MenuItem>
                {Object.values(Priority).map((priority) => (
                  <MenuItem key={priority} value={priority}>
                    {priority}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12} md={2} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button 
                variant="outlined" 
                startIcon={<RefreshIcon />}
                onClick={handleResetFilters}
              >
                Reset
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {error && (
          <Typography color="error" sx={{ mt: 2, mb: 2 }}>
            {error}
          </Typography>
        )}

        <Paper sx={{ width: '100%', overflow: 'hidden' }}>
          <TableContainer>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell 
                    onClick={() => handleSort('title')}
                    sx={{ cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Title {sortField === 'title' && (sortDirection === 'asc' ? '↑' : '↓')}
                  </TableCell>
                  <TableCell 
                    onClick={() => handleSort('status')}
                    sx={{ cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Status {sortField === 'status' && (sortDirection === 'asc' ? '↑' : '↓')}
                  </TableCell>
                  <TableCell 
                    onClick={() => handleSort('priority')}
                    sx={{ cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Priority {sortField === 'priority' && (sortDirection === 'asc' ? '↑' : '↓')}
                  </TableCell>
                  <TableCell 
                    onClick={() => handleSort('dueDate')}
                    sx={{ cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Due Date {sortField === 'dueDate' && (sortDirection === 'asc' ? '↑' : '↓')}
                  </TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                      <CircularProgress />
                    </TableCell>
                  </TableRow>
                ) : tasks.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                      No tasks found
                    </TableCell>
                  </TableRow>
                ) : (
                  tasks.map((task) => (
                    <TableRow key={task.id} hover sx={{ cursor: 'pointer' }}>
                      <TableCell 
                        onClick={() => navigateToEditTask(task.id!)}
                        sx={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                      >
                        <Tooltip title={task.description || ''} placement="top-start">
                          <Typography variant="body2">{task.title}</Typography>
                        </Tooltip>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={task.status?.replace('_', ' ') || 'TODO'} 
                          color={getStatusColor(task.status || TaskStatus.TODO) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={task.priority || 'MEDIUM'} 
                          color={getPriorityColor(task.priority || Priority.MEDIUM) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{formatDate(task.dueDate)}</TableCell>
                      <TableCell>
                        <Tooltip title="Edit">
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={(e) => {
                              e.stopPropagation();
                              navigateToEditTask(task.id!);
                            }}
                          >
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeleteTask(task.id!);
                            }}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={totalElements}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </Paper>
      </Box>
      
      <Fab 
        color="primary" 
        sx={{ position: 'fixed', bottom: 20, right: 20 }}
        onClick={navigateToCreateTask}
      >
        <AddIcon />
      </Fab>
    </Container>
  );
};

export default Tasks; 