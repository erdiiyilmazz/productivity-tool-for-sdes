import React, { useState, useEffect } from 'react';
import { 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  FormHelperText, 
  CircularProgress,
  SelectChangeEvent
} from '@mui/material';
import projectService, { ProjectDto } from '../services/projectService';

interface ProjectSelectProps {
  value: number | '';
  onChange: (projectId: number | '') => void;
  required?: boolean;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
  label?: string;
  fullWidth?: boolean;
  variant?: 'standard' | 'outlined' | 'filled';
  size?: 'small' | 'medium';
}

const ProjectSelect: React.FC<ProjectSelectProps> = ({
  value,
  onChange,
  required = false,
  error = false,
  helperText,
  disabled = false,
  label = 'Project',
  fullWidth = true,
  variant = 'outlined',
  size = 'medium'
}) => {
  const [projects, setProjects] = useState<ProjectDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        setLoading(true);
        const response = await projectService.getProjects();
        setProjects(response.content);
        setLoadError(null);
      } catch (err) {
        console.error('Error loading projects:', err);
        setLoadError('Failed to load projects');
        setProjects([]);
      } finally {
        setLoading(false);
      }
    };

    fetchProjects();
  }, []);

  const handleChange = (event: SelectChangeEvent<number | ''>) => {
    const newValue = event.target.value;
    if (newValue !== value) {
      onChange(newValue === '' ? '' : Number(newValue));
    }
  };

  return (
    <FormControl 
      fullWidth={fullWidth} 
      variant={variant} 
      required={required}
      error={error || !!loadError}
      disabled={disabled || loading}
      size={size}
    >
      <InputLabel id="project-select-label">{label}</InputLabel>
      <Select
        labelId="project-select-label"
        value={value}
        label={label}
        onChange={handleChange}
        startAdornment={loading ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
      >
        <MenuItem value="">
          <em>None</em>
        </MenuItem>
        {projects.map((project) => (
          <MenuItem key={project.id} value={project.id}>
            {project.name}
          </MenuItem>
        ))}
      </Select>
      {(helperText || loadError) && (
        <FormHelperText>{loadError || helperText}</FormHelperText>
      )}
    </FormControl>
  );
};

export default ProjectSelect; 