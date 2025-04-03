import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Container, 
  Typography, 
  Button, 
  Paper, 
  List,
  ListItem,
  ListItemText,
  IconButton,
  Tooltip,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Fab,
  Grid,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider
} from '@mui/material';
import { 
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Refresh as RefreshIcon,
  ExpandMore as ExpandMoreIcon
} from '@mui/icons-material';
import projectService, { ProjectDto } from '../services/projectService';
import categoryService, { CategoryDto } from '../services/categoryService';
import ProjectSelect from '../components/ProjectSelect';

interface CategoryWithProject extends CategoryDto {
  projectName?: string;
}

interface ProjectWithCategories {
  project: ProjectDto;
  categories: CategoryDto[];
}

const Categories: React.FC = () => {
  const [projects, setProjects] = useState<ProjectDto[]>([]);
  const [projectsWithCategories, setProjectsWithCategories] = useState<ProjectWithCategories[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingCategory, setEditingCategory] = useState<CategoryDto | null>(null);
  const [newCategory, setNewCategory] = useState<CategoryDto>({
    name: '',
    description: '',
    projectId: 0
  });
  const [dialogLoading, setDialogLoading] = useState(false);
  const [dialogError, setDialogError] = useState<string | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const projectsResponse = await projectService.getProjects(0, 100);
      const projects = projectsResponse.content;
      setProjects(projects);
      
      // For each project, fetch its categories
      const projectsWithCategoriesData: ProjectWithCategories[] = [];
      
      for (const project of projects) {
        try {
          const categoriesResponse = await categoryService.getCategoriesByProject(project.id!);
          projectsWithCategoriesData.push({
            project,
            categories: categoriesResponse.content
          });
        } catch (err) {
          console.error(`Error fetching categories for project ${project.id}:`, err);
        }
      }
      
      setProjectsWithCategories(projectsWithCategoriesData);
    } catch (err) {
      console.error('Error fetching data:', err);
      setError('Failed to load data. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const openCreateDialog = () => {
    setEditingCategory(null);
    setNewCategory({
      name: '',
      description: '',
      projectId: projects.length > 0 ? projects[0].id! : 0
    });
    setDialogError(null);
    setOpenDialog(true);
  };

  const openEditDialog = (category: CategoryDto) => {
    setEditingCategory(category);
    setNewCategory({
      name: category.name,
      description: category.description || '',
      projectId: category.projectId
    });
    setDialogError(null);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setNewCategory({
      ...newCategory,
      [name]: value
    });
  };

  const handleProjectChange = (projectId: number | '') => {
    setNewCategory({
      ...newCategory,
      projectId: projectId === '' ? 0 : projectId
    });
  };

  const handleSaveCategory = async () => {
    // Validate
    if (!newCategory.name.trim()) {
      setDialogError('Category name is required');
      return;
    }

    if (!newCategory.projectId) {
      setDialogError('Project is required');
      return;
    }

    setDialogLoading(true);
    setDialogError(null);

    try {
      if (editingCategory) {
        await categoryService.updateCategory(
          newCategory.projectId,
          editingCategory.id!,
          newCategory
        );
      } else {
        await categoryService.createCategory(newCategory.projectId, newCategory);
      }
      
      setOpenDialog(false);
      fetchData(); // Refresh the data
    } catch (err) {
      console.error('Error saving category:', err);
      setDialogError('Failed to save category. Please try again.');
    } finally {
      setDialogLoading(false);
    }
  };

  const handleDeleteCategory = async (projectId: number, categoryId: number) => {
    if (window.confirm('Are you sure you want to delete this category? All associated tasks will be uncategorized.')) {
      try {
        await categoryService.deleteCategory(projectId, categoryId);
        fetchData();
      } catch (err) {
        console.error('Error deleting category:', err);
        setError('Failed to delete category. Please try again.');
      }
    }
  };

  return (
    <Container maxWidth="xl">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom component="div">
          Category Management
        </Typography>

        <Paper sx={{ p: 2, mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={6}>
              <Typography variant="h6">
                Categories by Project
              </Typography>
            </Grid>
            <Grid item xs={12} md={6} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button 
                variant="outlined" 
                startIcon={<RefreshIcon />}
                onClick={fetchData}
              >
                Refresh
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {error && (
          <Typography color="error" sx={{ mt: 2, mb: 2 }}>
            {error}
          </Typography>
        )}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
            <CircularProgress />
          </Box>
        ) : projectsWithCategories.length === 0 ? (
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="body1">
              No projects or categories found. Create your first project and category to get started.
            </Typography>
          </Paper>
        ) : (
          projectsWithCategories.map(({ project, categories }) => (
            <Accordion key={project.id} defaultExpanded={true} sx={{ mb: 2 }}>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">{project.name}</Typography>
              </AccordionSummary>
              <AccordionDetails>
                {categories.length === 0 ? (
                  <Typography variant="body2" color="text.secondary">
                    No categories in this project
                  </Typography>
                ) : (
                  <List>
                    {categories.map((category) => (
                      <React.Fragment key={category.id}>
                        <ListItem
                          secondaryAction={
                            <Box>
                              <Tooltip title="Edit">
                                <IconButton
                                  edge="end"
                                  size="small"
                                  color="primary"
                                  onClick={() => openEditDialog(category)}
                                >
                                  <EditIcon />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Delete">
                                <IconButton
                                  edge="end"
                                  size="small"
                                  color="error"
                                  onClick={() => handleDeleteCategory(project.id!, category.id!)}
                                >
                                  <DeleteIcon />
                                </IconButton>
                              </Tooltip>
                            </Box>
                          }
                        >
                          <ListItemText
                            primary={category.name}
                            secondary={category.description || 'No description'}
                          />
                        </ListItem>
                        <Divider component="li" />
                      </React.Fragment>
                    ))}
                  </List>
                )}
                <Box sx={{ mt: 2 }}>
                  <Button
                    variant="outlined"
                    startIcon={<AddIcon />}
                    onClick={() => {
                      setNewCategory({
                        name: '',
                        description: '',
                        projectId: project.id!
                      });
                      setEditingCategory(null);
                      setDialogError(null);
                      setOpenDialog(true);
                    }}
                  >
                    Add Category to {project.name}
                  </Button>
                </Box>
              </AccordionDetails>
            </Accordion>
          ))
        )}
      </Box>
      
      <Fab 
        color="primary" 
        sx={{ position: 'fixed', bottom: 20, right: 20 }}
        onClick={openCreateDialog}
      >
        <AddIcon />
      </Fab>

      {/* Dialog for creating/editing categories */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingCategory ? 'Edit Category' : 'Create New Category'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <ProjectSelect
              value={newCategory.projectId || ''}
              onChange={handleProjectChange}
              required
              fullWidth
              disabled={!!editingCategory}
              label="Project"
            />
            <TextField
              fullWidth
              label="Category Name"
              name="name"
              required
              value={newCategory.name}
              onChange={handleInputChange}
              margin="normal"
              error={dialogError?.includes('name')}
            />
            <TextField
              fullWidth
              label="Description"
              name="description"
              value={newCategory.description || ''}
              onChange={handleInputChange}
              margin="normal"
              multiline
              rows={3}
            />
            {dialogError && (
              <Typography color="error" variant="body2" sx={{ mt: 2 }}>
                {dialogError}
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog} disabled={dialogLoading}>
            Cancel
          </Button>
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleSaveCategory}
            disabled={dialogLoading}
          >
            {dialogLoading ? <CircularProgress size={24} /> : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Categories; 