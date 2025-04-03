# Productivity Tool For Software Enthusiasts

A comprehensive task and time management application with integrated authentication, task management, time tracking, and project organization capabilities for software developers/project managers/scrum masters.


## 📋 Features

### Authentication & User Management
- User registration with secure password encryption
- Role-based authentication
- Session management with Redis
- Profile management

### Task Management
- Create, update, delete, and view tasks
- Task categorization by projects and categories
- Task filtering by status, priority, and more
- Task assignment to users
- Task searching and sorting

### Project Management
- Project creation and organization
- Category management within projects
- Project members management

### Time Tracking
- Track time spent on tasks
- Start/stop timer functionality
- Generate time reports
- Analyze productivity

## 🛠️ Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.x**: Core framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **Spring Session**: Session management
- **Spring Validation**: Input validation
- **PostgreSQL**: Primary database
- **Redis**: Session storage
- **Liquibase**: Database schema management
- **Swagger/OpenAPI**: API documentation

### Frontend
- **React 18.x**: UI library
- **TypeScript**: Type-safe JavaScript
- **Material-UI**: Component library
- **Axios**: HTTP client
- **React Router**: Navigation

### DevOps & Infrastructure
- **Docker**: Containerization
- **Docker Compose**: Multi-container deployment
- **Maven**: Build automation
- **GitHub**: Version control

## 🏗️ Architecture

The application follows a modular architecture with clear separation of concerns:

1. **Auth Manager**: Handles user authentication, registration, and session management
2. **Task Manager**: Manages task creation, updates, and queries
3. **Time Tracker**: Tracks time entries for tasks
4. **Common**: Shared utilities and components
5. **Scheduler**: Background jobs and scheduling

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- Docker and Docker Compose
- PostgreSQL 14+ (or use provided Docker setup)
- Redis (or use provided Docker setup)

### Setup

#### 1. Clone the repository
```bash
git clone https://github.com/your-username/time-manager.git
cd time-manager
```

#### 2. Start the database services
```bash
docker-compose up -d postgres redis
```

#### 3. Build and run the backend
```bash
./mvnw clean install
./mvnw spring-boot:run
```

#### 4. Set up and run the frontend
```bash
cd frontend/time-manager-ui
npm install
npm start
```

The application will be accessible at:
- Backend: http://localhost:8081
- Frontend: http://localhost:3000
- API Documentation: http://localhost:8081/swagger-ui-custom.html

## 📝 API Documentation

The application provides comprehensive API documentation using Swagger/OpenAPI, available at `/swagger-ui-custom.html`. The documentation includes:

- Detailed endpoint descriptions
- Request/response examples
- Authentication requirements
- Schema definitions

## 💾 Database Schema

The application uses PostgreSQL with the following main entities:
- Users
- Roles
- Projects
- Categories
- Tasks
- Time Entries
- Task Attachments

Database migrations are managed through Liquibase, ensuring consistent schema evolution.

## 🔒 Security

- Passwords are encrypted using BCrypt
- Session-based authentication
- CSRF protection
- Input validation
- Role-based access control

## 🧪 Testing

The application includes unit and integration tests. Run them with:

```bash
./mvnw test
```

## 🛣️ Roadmap

Upcoming features and improvements:

- Team collaboration features
- Calendar integration
- Notifications system
- Mobile application
- Advanced reporting and analytics
- Dark/light theme toggle
- Export data (CSV, PDF)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributors

- [Erdi Yılmaz](https://github.com/erdiiyilmazz) - Initial work and ongoing development

## 🙏 Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [Material-UI](https://mui.com/)
- [PostgreSQL](https://www.postgresql.org/)
- [Redis](https://redis.io/) 
