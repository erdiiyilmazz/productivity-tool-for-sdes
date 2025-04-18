import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Container, 
  Typography, 
  Button, 
  Paper, 
  TextField, 
  Grid,
  MenuItem,
  Snackbar,
  Alert,
  CircularProgress,
  InputAdornment,
  Divider
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import taskService, { TaskDto, TaskStatus, Priority } from '../services/taskService';
import ProjectSelect from '../components/ProjectSelect';
import CategorySelect from '../components/CategorySelect';
import UserSelect from '../components/UserSelect';

interface TaskFormProps {
  isEditing?: boolean;
}

const TaskForm: React.FC<TaskFormProps> = ({ isEditing = false }) => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [task, setTask] = useState<TaskDto>({
    title: '',
    description: '',
    status: TaskStatus.TODO,
    priority: Priority.MEDIUM,
    dueDate: '',
    estimatedHours: 0,
    actualHours: 0
  });

  useEffect(() => {
    if (isEditing && id) {
      fetchTask(parseInt(id));
    }
  }, [isEditing, id]);

  const fetchTask = async (taskId: number) => {
    setLoading(true);
    setError(null);
    try {
      const fetchedTask = await taskService.getTask(taskId);
      setTask(fetchedTask);
    } catch (err) {
      console.error('Error fetching task:', err);
      setError('Failed to load task. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    
    if (name === 'estimatedHours' || name === 'actualHours') {
      const numValue = parseFloat(value);
      if (!isNaN(numValue) && numValue >= 0) {
        setTask({ ...task, [name]: numValue });
      }
    } else {
      setTask({ ...task, [name]: value });
    }
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setTask({ ...task, dueDate: e.target.value });
  };

  const handleProjectChange = (projectId: number | '') => {
    setTask({ 
      ...task, 
      projectId: projectId === '' ? undefined : projectId,
      // Reset category when project changes
      categoryId: undefined 
    });
  };

  const handleCategoryChange = (categoryId: number | '') => {
    setTask({ 
      ...task, 
      categoryId: categoryId === '' ? undefined : categoryId 
    });
  };

  const handleAssigneeChange = (assigneeId: number | '') => {
    setTask({ 
      ...task, 
      assigneeId: assigneeId === '' ? undefined : assigneeId 
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const errors: string[] = [];
    
    if (!task.title.trim()) {
      errors.push('Title is required');
    }
    
    if (!task.projectId) {
      errors.push('Project is required');
      setError('Project is required');
      return;
    }
    
    if (errors.length > 0) {
      setError(errors.join(', '));
      return;
    }
    
    const taskToSubmit: {
      title: string;
      description: string;
      status: TaskStatus;
      priority: Priority;
      projectId: number;
      categoryId?: number;
      assigneeId?: number;
      dueDate?: string;
    } = {
      title: task.title,
      description: task.description || "",
      status: task.status || TaskStatus.TODO,
      priority: task.priority || Priority.MEDIUM,
      projectId: Number(task.projectId)
    };
    
    // Only add optional fields if they have valid values
    if (task.categoryId) {
      taskToSubmit.categoryId = Number(task.categoryId);
    }
    
    if (task.assigneeId) {
      taskToSubmit.assigneeId = Number(task.assigneeId);
    }
    
    if (task.dueDate) {
      taskToSubmit.dueDate = task.dueDate;
    }
    
    console.log('Submitting task data:', JSON.stringify(taskToSubmit, null, 2));
    
    setLoading(true);
    setError(null);
    
    try {
      if (isEditing && id) {
        await taskService.updateTask(parseInt(id), taskToSubmit as TaskDto);
        setSuccess('Task updated successfully!');
      } else {
        await taskService.createTask(taskToSubmit as TaskDto);
        setSuccess('Task created successfully!');
        if (!isEditing) {
          setTask({
            title: '',
            description: '',
            status: TaskStatus.TODO,
            priority: Priority.MEDIUM,
            dueDate: '',
            estimatedHours: 0,
            actualHours: 0
          });
        }
      }
      
      setTimeout(() => {
        handleClose();
      }, 1500);
    } catch (err: any) {
      console.error('Error saving task:', err);
      let errorMessage = 'Failed to save task. Please try again.';
      
      if (err.response) {
        console.log('Error response status:', err.response.status);
        console.log('Error response data:', JSON.stringify(err.response.data, null, 2));
        
        if (err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.response.data && err.response.data.error) {
          errorMessage = err.response.data.error;
        } else if (err.response.data && err.response.data.errors && Array.isArray(err.response.data.errors)) {
          errorMessage = err.response.data.errors.map((e: any) => e.message || e.defaultMessage).join(', ');
        } else if (typeof err.response.data === 'string') {
          errorMessage = err.response.data;
        }
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    navigate('/tasks');
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom component="div">
          {isEditing ? 'Edit Task' : 'Create New Task'}
        </Typography>

        <Paper sx={{ p: 3 }}>
          {loading && !isEditing ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <form onSubmit={handleSubmit}>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    required
                    label="Title"
                    name="title"
                    value={task.title}
                    onChange={handleChange}
                    variant="outlined"
                    disabled={loading}
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Description"
                    name="description"
                    value={task.description || ''}
                    onChange={handleChange}
                    variant="outlined"
                    multiline
                    rows={4}
                    disabled={loading}
                  />
                </Grid>
                
                <Grid item xs={12}>
                  <Divider sx={{ mt: 1, mb: 3 }} />
                  <Typography variant="subtitle1" gutterBottom>
                    Task Details
                  </Typography>
                </Grid>
                
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Status"
                    name="status"
                    select
                    value={task.status || TaskStatus.TODO}
                    onChange={handleChange}
                    variant="outlined"
                    disabled={loading}
                  >
                    {Object.values(TaskStatus).map((status) => (
                      <MenuItem key={status} value={status}>
                        {status.replace('_', ' ')}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Priority"
                    name="priority"
                    select
                    value={task.priority || Priority.MEDIUM}
                    onChange={handleChange}
                    variant="outlined"
                    disabled={loading}
                  >
                    {Object.values(Priority).map((priority) => (
                      <MenuItem key={priority} value={priority}>
                        {priority}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Due Date"
                    type="datetime-local"
                    name="dueDate"
                    value={task.dueDate ? task.dueDate.substring(0, 16) : ''}
                    onChange={handleDateChange}
                    InputLabelProps={{
                      shrink: true,
                    }}
                    variant="outlined"
                    disabled={loading}
                  />
                </Grid>
                
                <Grid item xs={12}>
                  <Divider sx={{ mt: 1, mb: 3 }} />
                  <Typography variant="subtitle1" gutterBottom>
                    Organization
                  </Typography>
                </Grid>
                
                <Grid item xs={12} sm={6}>
                  <ProjectSelect
                    value={task.projectId || ''}
                    onChange={handleProjectChange}
                    disabled={loading}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <CategorySelect
                    projectId={task.projectId || null}
                    value={task.categoryId || ''}
                    onChange={handleCategoryChange}
                    disabled={loading}
                    required={false}
                  />
                </Grid>
                
                <Grid item xs={12}>
                  <Divider sx={{ mt: 1, mb: 3 }} />
                  <Typography variant="subtitle1" gutterBottom>
                    Assignment & Time Tracking
                  </Typography>
                </Grid>
                
                <Grid item xs={12} sm={6}>
                  <UserSelect
                    value={task.assigneeId || ''}
                    onChange={handleAssigneeChange}
                    disabled={loading}
                    required={false}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Estimated Hours"
                    name="estimatedHours"
                    type="number"
                    InputProps={{
                      endAdornment: <InputAdornment position="end">hrs</InputAdornment>,
                    }}
                    value={task.estimatedHours || ''}
                    onChange={handleChange}
                    variant="outlined"
                    disabled={loading}
                  />
                </Grid>
                {isEditing && (
                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="Actual Hours"
                      name="actualHours"
                      type="number"
                      InputProps={{
                        endAdornment: <InputAdornment position="end">hrs</InputAdornment>,
                      }}
                      value={task.actualHours || ''}
                      onChange={handleChange}
                      variant="outlined"
                      disabled={loading}
                    />
                  </Grid>
                )}
                <Grid item xs={12} sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
                  <Button
                    variant="outlined"
                    onClick={handleClose}
                    disabled={loading}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    disabled={loading}
                  >
                    {loading ? (
                      <CircularProgress size={24} />
                    ) : isEditing ? (
                      'Update Task'
                    ) : (
                      'Create Task'
                    )}
                  </Button>
                </Grid>
              </Grid>
            </form>
          )}
        </Paper>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </Box>

      <Snackbar 
        open={!!success} 
        autoHideDuration={5000} 
        onClose={() => setSuccess(null)}
      >
        <Alert onClose={() => setSuccess(null)} severity="success">
          {success}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default TaskForm; 