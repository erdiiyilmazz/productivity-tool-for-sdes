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
import categoryService, { CategoryDto } from '../services/categoryService';

interface CategorySelectProps {
  projectId?: number | null;
  value: number | '';
  onChange: (categoryId: number | '') => void;
  required?: boolean;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
  label?: string;
  fullWidth?: boolean;
  variant?: 'standard' | 'outlined' | 'filled';
  size?: 'small' | 'medium';
}

const CategorySelect: React.FC<CategorySelectProps> = ({
  projectId,
  value,
  onChange,
  required = false,
  error = false,
  helperText,
  disabled = false,
  label = 'Category',
  fullWidth = true,
  variant = 'outlined',
  size = 'medium'
}) => {
  const [categories, setCategories] = useState<CategoryDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  // Fetch categories when project changes
  useEffect(() => {
    const fetchCategories = async () => {
      if (!projectId) {
        setCategories([]);
        if (value !== '') {
          onChange('');
        }
        return;
      }

      try {
        setLoading(true);
        const response = await categoryService.getCategoriesByProject(projectId);
        setCategories(response.content);
        
        if (value !== '' && !response.content.some(cat => cat.id === value)) {
          onChange('');
        }
        
        setLoadError(null);
      } catch (err) {
        console.error('Error loading categories:', err);
        setLoadError('Failed to load categories');
        setCategories([]);
        if (value !== '') {
          onChange('');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, [projectId, onChange]);

  const handleChange = (event: SelectChangeEvent<number | ''>) => {
    const newValue = event.target.value;
    onChange(newValue === '' ? '' : Number(newValue));
  };

  return (
    <FormControl 
      fullWidth={fullWidth} 
      variant={variant} 
      required={required}
      error={error || !!loadError}
      disabled={disabled || loading || !projectId}
      size={size}
    >
      <InputLabel id="category-select-label">{label}</InputLabel>
      <Select
        labelId="category-select-label"
        value={categories.length > 0 ? value : ''}
        label={label}
        onChange={handleChange}
        startAdornment={loading ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
      >
        <MenuItem value="">
          <em>None</em>
        </MenuItem>
        {categories.map((category) => (
          <MenuItem key={category.id} value={category.id || ''}>
            {category.name}
          </MenuItem>
        ))}
      </Select>
      {!projectId && !disabled ? (
        <FormHelperText>Select a project first</FormHelperText>
      ) : (
        (helperText || loadError) && (
          <FormHelperText>{loadError || helperText}</FormHelperText>
        )
      )}
    </FormControl>
  );
};

export default CategorySelect; 