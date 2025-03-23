import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions
} from '@mui/material';
import { styled } from '@mui/material/styles';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import AssignmentIcon from '@mui/icons-material/Assignment';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';

const FeatureCard = styled(Card)(({ theme }) => ({
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  transition: 'transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out',
  '&:hover': {
    transform: 'translateY(-5px)',
    boxShadow: theme.shadows[8],
  },
}));

const IconWrapper = styled(Box)(({ theme }) => ({
  marginBottom: theme.spacing(2),
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  '& > svg': {
    fontSize: '3rem',
    color: theme.palette.primary.main,
  },
}));

const Home: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Hero Section */}
      <Box
        sx={{
          bgcolor: 'background.paper',
          pt: 8,
          pb: 6,
          background: 'linear-gradient(45deg, #1a237e 30%, #283593 90%)',
        }}
      >
        <Container maxWidth="md">
          <Typography
            component="h1"
            variant="h2"
            align="center"
            color="white"
            gutterBottom
          >
            Time Manager
          </Typography>
          <Typography variant="h5" align="center" color="white" paragraph>
            Streamline your productivity with our powerful time management and task scheduling system.
            Track time, manage projects, and stay on top of your deadlines.
          </Typography>
          <Box
            sx={{
              mt: 4,
              display: 'flex',
              justifyContent: 'center',
              gap: 2,
            }}
          >
            <Button 
              variant="contained" 
              color="secondary" 
              size="large"
              onClick={() => navigate('/register')}
            >
              Get Started
            </Button>
            <Button 
              variant="outlined" 
              sx={{ color: 'white', borderColor: 'white' }}
              size="large"
              onClick={() => navigate('/login')}
            >
              Sign In
            </Button>
          </Box>
        </Container>
      </Box>

      {/* Features Section */}
      <Container sx={{ py: 8 }} maxWidth="md">
        <Typography
          component="h2"
          variant="h3"
          align="center"
          color="textPrimary"
          gutterBottom
        >
          Features
        </Typography>
        <Typography
          variant="h6"
          align="center"
          color="textSecondary"
          paragraph
          sx={{ mb: 6 }}
        >
          Discover what makes Time Manager the perfect tool for your productivity
        </Typography>
        <Grid container spacing={4}>
          <Grid item xs={12} sm={6} md={3}>
            <FeatureCard>
              <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
                <IconWrapper>
                  <AccessTimeIcon />
                </IconWrapper>
                <Typography gutterBottom variant="h5" component="h3">
                  Time Tracking
                </Typography>
                <Typography>
                  Track the time you spend on tasks and generate detailed reports.
                </Typography>
              </CardContent>
              <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                <Button size="small" color="primary">
                  Learn More
                </Button>
              </CardActions>
            </FeatureCard>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <FeatureCard>
              <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
                <IconWrapper>
                  <TaskAltIcon />
                </IconWrapper>
                <Typography gutterBottom variant="h5" component="h3">
                  Task Management
                </Typography>
                <Typography>
                  Create, organize, and prioritize your tasks efficiently.
                </Typography>
              </CardContent>
              <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                <Button size="small" color="primary">
                  Learn More
                </Button>
              </CardActions>
            </FeatureCard>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <FeatureCard>
              <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
                <IconWrapper>
                  <AssignmentIcon />
                </IconWrapper>
                <Typography gutterBottom variant="h5" component="h3">
                  Project Planning
                </Typography>
                <Typography>
                  Group tasks into projects and track progress easily.
                </Typography>
              </CardContent>
              <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                <Button size="small" color="primary">
                  Learn More
                </Button>
              </CardActions>
            </FeatureCard>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <FeatureCard>
              <CardContent sx={{ flexGrow: 1, textAlign: 'center' }}>
                <IconWrapper>
                  <CalendarMonthIcon />
                </IconWrapper>
                <Typography gutterBottom variant="h5" component="h3">
                  Scheduling
                </Typography>
                <Typography>
                  Set reminders and schedule your tasks for better time management.
                </Typography>
              </CardContent>
              <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                <Button size="small" color="primary">
                  Learn More
                </Button>
              </CardActions>
            </FeatureCard>
          </Grid>
        </Grid>
      </Container>

      {/* Footer */}
      <Box
        component="footer"
        sx={{
          py: 3,
          px: 2,
          mt: 'auto',
          backgroundColor: (theme) => theme.palette.grey[900],
        }}
      >
        <Container maxWidth="sm">
          <Typography variant="body1" align="center" color="white">
            Time Manager - Streamline your productivity
          </Typography>
          <Typography variant="body2" align="center" color="white" sx={{ mt: 1 }}>
            © {new Date().getFullYear()} Time Manager. All rights reserved.
          </Typography>
        </Container>
      </Box>
    </Box>
  );
};

export default Home; 