# Vega User Service

## Overview
Vega User Service, Vega VCS ekosisteminde kullanıcı kimlik doğrulama ve yetkilendirme işlemlerini yöneten mikroservistir.

## Features
- User registration and authentication
- JWT token management
- Password encryption (BCrypt)
- User profile management
- Role-based access control
- Session management

## Technology Stack
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (jjwt)
- Maven

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/{id}` - Get user by ID
- `DELETE /api/users/{id}` - Delete user

### Health Check
- `GET /actuator/health` - Service health status

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(20) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Configuration
- Port: 8081
- Database: PostgreSQL
- JWT Secret: Configurable via environment variables
- JWT Expiration: 24 hours (configurable)

## Security
- Password hashing with BCrypt
- JWT token authentication
- CORS configuration
- Input validation
- SQL injection prevention

