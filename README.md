# Sportal - Student Portal System

A comprehensive student portal system built with Spring Boot that enables course enrollment, assignment submission, and academic management across three distinct user roles.

## 🚀 Features

### Student Features
- **Course Enrollment**: Browse and enroll in available courses
- **Assignment Management**: Submit assignments and track submission status
- **Grade Tracking**: View grades and academic progress
- **Course Materials**: Access course resources and materials

### Instructor Features
- **Course Management**: Create and manage courses
- **Material Upload**: Add course materials and resources
- **Assignment Grading**: Grade student submissions and provide feedback
- **Student Progress**: Monitor enrolled students and their performance

### Admin Features
- **User Management**: Manage students, instructors, and system users
- **Course Administration**: Oversee all courses and enrollments
- **System Configuration**: Configure system settings and permissions
- **Reports & Analytics**: Generate academic reports and statistics

## 🛠️ Technology Stack

- **Backend Framework**: Spring Boot 3.x
- **Security**: Spring Security with JWT Authentication
- **Database**: PostgreSQL
- **Code Simplification**: Lombok
- **Build Tool**: Maven/Gradle
- **Java Version**: 17+

## 📋 Prerequisites

Before running the application, ensure you have:

- Java 17 or higher
- PostgreSQL 12+
- Maven 3.6+ or Gradle 7+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## ⚙️ Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/sportal.git
cd sportal
```

### 2. Database Setup
Create a PostgreSQL database:
```sql
CREATE DATABASE sportal_db;
CREATE USER sportal_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE sportal_db TO sportal_user;
```

### 3. Configuration
Update `application.properties` or `application.yml`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/sportal_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Server Configuration
server.port=8080
```

## 🔐 Authentication & Authorization

The system uses JWT-based authentication with role-based access control:

### User Roles
- **ADMIN**: Full system access and administration
- **INSTRUCTOR**: Course and student management
- **STUDENT**: Course enrollment and assignment submission

### API Authentication
Include JWT token in request headers:
```
Authorization: <your-jwt-token>
```

## 🗄️ Database Schema

### Core Entities
- **Users**: User authentication and profile information
- **Courses**: Course details and metadata
- **Enrollments**: Student-course relationships
- **Assignments**: Assignment details and specifications
- **Submissions**: Student assignment submissions
- **Grades**: Assignment grades and feedback
- **Materials**: Course materials and resources

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/sportal/
│   │       ├── config/          # Security & JWT configuration
│   │       ├── controller/      # REST controllers
│   │       ├── models/         # for entities and Dtos
│   │       ├── repository/     # Data repositories
│   │       ├── service/        # Business logic
│   │       ├── security/       # Security components
│   │       └── mapper/          # mapping requests/responses with dtos
│   └── resources/
│       ├── application.properties
│       └── static/
└── test/
    └── java/
        └── com/sportal/        # Test classes
```
---

**Sportal** - Empowering education through technology 🎓