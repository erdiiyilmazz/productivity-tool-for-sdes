import React, { useState } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Grid,
  Paper,
  Link,
  InputAdornment,
  IconButton,
  Alert,
  CircularProgress
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import AppRegistrationIcon from '@mui/icons-material/AppRegistration';
import Avatar from '@mui/material/Avatar';
import authService from '../services/authService';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    fullName: '',
    password: '',
    confirmPassword: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleClickShowPassword = () => {
    setShowPassword(!showPassword);
  };

  const validatePassword = (password: string): string | null => {
    if (!password) return "Password is required";
    if (password.length < 8) return "Password must be at least 8 characters long";
    
    // Check for at least one digit
    if (!/(?=.*[0-9])/.test(password)) {
      return "Password must contain at least one digit";
    }
    
    // Check for at least one lowercase letter
    if (!/(?=.*[a-z])/.test(password)) {
      return "Password must contain at least one lowercase letter";
    }
    
    // Check for at least one uppercase letter
    if (!/(?=.*[A-Z])/.test(password)) {
      return "Password must contain at least one uppercase letter";
    }
    
    // Check for at least one special character
    if (!/(?=.*[@#$%^&+=!])/.test(password)) {
      return "Password must contain at least one special character (@#$%^&+=!)";
    }
    
    return null;
  };
  
  const validateUsername = (username: string): string | null => {
    if (!username) return "Username is required";
    if (username.length < 3) return "Username must be at least 3 characters long";
    if (username.length > 50) return "Username cannot exceed 50 characters";
    
    // Check for valid characters
    if (!/^[a-zA-Z0-9._-]+$/.test(username)) {
      return "Username can only contain letters, numbers, dots, underscores and hyphens";
    }
    
    return null;
  };
  
  const validateEmail = (email: string): string | null => {
    if (!email) return "Email is required";
    
    // Simple email validation
    if (!/\S+@\S+\.\S+/.test(email)) {
      return "Invalid email format";
    }
    
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    
    // Validate username
    const usernameError = validateUsername(formData.username);
    if (usernameError) {
      setError(usernameError);
      return;
    }
    
    // Validate email
    const emailError = validateEmail(formData.email);
    if (emailError) {
      setError(emailError);
      return;
    }
    
    // Validate full name
    if (!formData.fullName) {
      setError("Full name is required");
      return;
    }
    
    if (formData.fullName.length > 100) {
      setError("Full name cannot exceed 100 characters");
      return;
    }
    
    // Validate password
    const passwordError = validatePassword(formData.password);
    if (passwordError) {
      setError(passwordError);
      return;
    }
    
    // Check if passwords match
    if (formData.password !== formData.confirmPassword) {
      setError("Passwords don't match");
      return;
    }
    
    setLoading(true);
    
    try {
      // Prepare registration data (excluding confirmPassword)
      const registrationData = {
        username: formData.username,
        email: formData.email,
        fullName: formData.fullName,
        password: formData.password
      };
      
      // Log only non-sensitive data
      console.log('Attempting to register user:', formData.username);
      
      // Call the registration API
      await authService.register(registrationData);
      
      // Redirect to login page after successful registration
      navigate('/login');
    } catch (err: any) {
      console.error('Registration error:', err);
      
      // Enhanced error handling
      if (err.response) {
        // Don't log response data as it might contain sensitive information
        console.log('Error response status:', err.response.status);
        
        // Handle different error status codes
        if (err.response.status === 400) {
          // Check for validation errors in different formats
          if (err.response.data.message) {
            setError(err.response.data.message);
          } else if (err.response.data.errors && Array.isArray(err.response.data.errors)) {
            // Extract first error message from array of validation errors
            setError(err.response.data.errors[0].message || 'Invalid input data');
          } else if (typeof err.response.data === 'string') {
            // Sometimes error might come as plain text
            setError(err.response.data);
          } else {
            // Default validation error
            setError('Please check your input data');
          }
        } else if (err.response.status === 409) {
          // Conflict - username or email already exists
          setError(err.response.data.message || 'Username or email already exists');
        } else {
          setError('Registration failed. Please try again.');
        }
      } else if (err.request) {
        setError('Network error. Please check your connection.');
      } else {
        setError('An unexpected error occurred');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          marginBottom: 4
        }}
      >
        <Paper 
          elevation={6} 
          sx={{ 
            padding: 4, 
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            width: '100%',
            borderRadius: 2
          }}
        >
          <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
            <AppRegistrationIcon />
          </Avatar>
          <Typography component="h1" variant="h5" mb={3}>
            Create your account
          </Typography>
          
          {error && (
            <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
              {error}
            </Alert>
          )}
          
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1, width: '100%' }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={formData.username}
              onChange={handleChange}
              disabled={loading}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="fullName"
              label="Full Name"
              name="fullName"
              autoComplete="name"
              value={formData.fullName}
              onChange={handleChange}
              disabled={loading}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email Address"
              name="email"
              autoComplete="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              disabled={loading}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type={showPassword ? 'text' : 'password'}
              id="password"
              autoComplete="new-password"
              value={formData.password}
              onChange={handleChange}
              disabled={loading}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={handleClickShowPassword}
                      edge="end"
                      disabled={loading}
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                )
              }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="confirmPassword"
              label="Confirm Password"
              type={showPassword ? 'text' : 'password'}
              id="confirmPassword"
              autoComplete="new-password"
              value={formData.confirmPassword}
              onChange={handleChange}
              disabled={loading}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={loading}
              sx={{ mt: 3, mb: 2, py: 1.2 }}
            >
              {loading ? <CircularProgress size={24} /> : 'Sign Up'}
            </Button>
            <Grid container justifyContent="flex-end">
              <Grid item>
                <Link component={RouterLink} to="/login" variant="body2">
                  Already have an account? Sign in
                </Link>
              </Grid>
            </Grid>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Register; 