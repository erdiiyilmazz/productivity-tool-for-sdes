import React, { useState, useEffect } from 'react';
import { 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  FormHelperText, 
  CircularProgress,
  SelectChangeEvent,
  Avatar,
  ListItemAvatar,
  ListItemText
} from '@mui/material';
import userService from '../services/userService';
import { UserResponse } from '../services/authService';

interface UserSelectProps {
  value: number | '';
  onChange: (userId: number | '') => void;
  required?: boolean;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
  label?: string;
  fullWidth?: boolean;
  variant?: 'standard' | 'outlined' | 'filled';
  size?: 'small' | 'medium';
}

const UserSelect: React.FC<UserSelectProps> = ({
  value,
  onChange,
  required = false,
  error = false,
  helperText,
  disabled = false,
  label = 'Assignee',
  fullWidth = true,
  variant = 'outlined',
  size = 'medium'
}) => {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        // Try to get all users with the new endpoint
        try {
          const allUsers = await userService.getAllUsers();
          setUsers(allUsers);
          
          // If selected user doesn't exist in the list, reset selection
          if (value !== '' && !allUsers.some(user => user.id === value)) {
            onChange('');
          }
          
          setLoadError(null);
        } catch (err) {
          console.log('Failed to get all users, falling back to current user only');
          const currentUser = await userService.getCurrentUser();
          const usersList = currentUser ? [currentUser] : [];
          setUsers(usersList);
          
          // If selected user doesn't exist in the list, reset selection
          if (value !== '' && !usersList.some(user => user.id === value)) {
            onChange('');
          }
          
          setLoadError(null);
        }
      } catch (err) {
        console.error('Error loading users:', err);
        setLoadError('Failed to load users');
        setUsers([]);
        
        // Reset selection if we have no users
        if (value !== '') {
          onChange('');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, [onChange]);

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
      disabled={disabled || loading}
      size={size}
    >
      <InputLabel id="user-select-label">{label}</InputLabel>
      <Select
        labelId="user-select-label"
        value={users.length > 0 ? value : ''}
        label={label}
        onChange={handleChange}
        startAdornment={loading ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
        sx={{ 
          '& .MuiListItemText-root': { 
            display: 'flex', 
            alignItems: 'center' 
          } 
        }}
      >
        <MenuItem value="">
          <em>None</em>
        </MenuItem>
        {users.map((user) => (
          <MenuItem key={user.id} value={user.id || ''} sx={{ display: 'flex', alignItems: 'center' }}>
            <ListItemAvatar>
              <Avatar sx={{ width: 24, height: 24, fontSize: '0.75rem', bgcolor: 'primary.main' }}>
                {user.username.charAt(0).toUpperCase()}
              </Avatar>
            </ListItemAvatar>
            <ListItemText primary={user.username} secondary={user.fullName} />
          </MenuItem>
        ))}
      </Select>
      {(helperText || loadError) && (
        <FormHelperText>{loadError || helperText}</FormHelperText>
      )}
    </FormControl>
  );
};

export default UserSelect; 