import React, { useState, useContext, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import {
  AppBar,
  Box,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Tabs,
  Tab
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import authService from '../services/authService';
import { AuthContext } from '../App';

const Header: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, setUser, isAuthenticated } = useContext(AuthContext);
  const [loading, setLoading] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [tabValue, setTabValue] = useState<number>(0);

  useEffect(() => {
    if (location.pathname === '/') {
      setTabValue(0);
    } else if (location.pathname.startsWith('/tasks')) {
      setTabValue(1);
    } else if (location.pathname.startsWith('/projects')) {
      setTabValue(2);
    } else if (location.pathname.startsWith('/categories')) {
      setTabValue(3);
    }
  }, [location.pathname]);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    try {
      setLoading(true);
      // Try to logout on the server
      await authService.logout();
    } catch (error) {
      console.error('Error logging out:', error);
      // Continue with local logout even if server logout fails
    } finally {
      // Always clear the local user state
      setUser(null);
      handleMenuClose();
      setLoading(false);
      navigate('/login');
    }
  };

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    switch (newValue) {
      case 0: navigate('/'); break;
      case 1: navigate('/tasks'); break;
      case 2: navigate('/projects'); break;
      case 3: navigate('/categories'); break;
      default: navigate('/');
    }
  };

  return (
    <AppBar position="static">
      <Toolbar>
        <IconButton
          size="large"
          edge="start"
          color="inherit"
          aria-label="menu"
          sx={{ mr: 2, display: { md: 'none' } }}
          onClick={handleMenuOpen}
        >
          <MenuIcon />
        </IconButton>
        <Menu
          id="menu-appbar"
          anchorEl={anchorEl}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left',
          }}
          keepMounted
          transformOrigin={{
            vertical: 'top',
            horizontal: 'left',
          }}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
        >
          <MenuItem onClick={() => { handleMenuClose(); navigate('/'); }}>Home</MenuItem>
          {isAuthenticated && (
            <MenuItem onClick={() => { handleMenuClose(); navigate('/tasks'); }}>Tasks</MenuItem>
          )}
          {isAuthenticated && (
            <MenuItem onClick={() => { handleMenuClose(); navigate('/projects'); }}>Projects</MenuItem>
          )}
          {isAuthenticated && (
            <MenuItem onClick={() => { handleMenuClose(); navigate('/categories'); }}>Categories</MenuItem>
          )}
        </Menu>
        
        <Typography variant="h6" component="div" sx={{ display: { xs: 'block', md: 'none' }, flexGrow: 1 }}>
          <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
            Time Manager
          </Link>
        </Typography>
        
        {/* Desktop Navigation */}
        <Box sx={{ display: { xs: 'none', md: 'flex' }, flexGrow: 1, alignItems: 'center' }}>
          <Typography variant="h6" component="div" sx={{ mr: 4 }}>
            <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
              Time Manager
            </Link>
          </Typography>
          
          {isAuthenticated && (
            <Tabs 
              value={tabValue} 
              onChange={handleTabChange}
              textColor="inherit"
              indicatorColor="secondary"
            >
              <Tab label="Home" />
              <Tab label="Tasks" />
              <Tab label="Projects" />
              <Tab label="Categories" />
            </Tabs>
          )}
        </Box>
        
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          {isAuthenticated ? (
            <>
              <Box sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }} onClick={handleMenuOpen}>
                <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', mr: 1 }}>
                  {user?.username.charAt(0).toUpperCase()}
                </Avatar>
                <Typography variant="body1" sx={{ mr: 2 }}>
                  {user?.username}
                </Typography>
              </Box>
              <Button 
                color="inherit" 
                onClick={handleLogout}
                disabled={loading}
              >
                {loading ? 'Logging out...' : 'Logout'}
              </Button>
            </>
          ) : (
            <>
              <Button 
                color="inherit" 
                onClick={() => navigate('/login')}
              >
                Login
              </Button>
              <Button 
                color="inherit" 
                onClick={() => navigate('/register')}
              >
                Register
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header; 