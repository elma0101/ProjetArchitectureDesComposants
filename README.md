# Bookstore Web Application

A full-stack bookstore web application built with Spring Boot (backend) and React with TypeScript (frontend).

## Project Structure

```
bookstore-web-app/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/               # React TypeScript frontend
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── tsconfig.json
└── README.md
```

## Prerequisites

- Java 17 or higher
- Node.js 16 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## Setup Instructions

### Database Setup

1. Install PostgreSQL
2. Create a database named `bookstore`
3. Create a user with appropriate permissions:
   ```sql
   CREATE DATABASE bookstore;
   CREATE USER bookstore_user WITH PASSWORD 'bookstore_password';
   GRANT ALL PRIVILEGES ON DATABASE bookstore TO bookstore_user;
   ```

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Install dependencies and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

   The frontend will start on `http://localhost:3000`

## Environment Variables

### Backend
- `DB_USERNAME`: Database username (default: bookstore_user)
- `DB_PASSWORD`: Database password (default: bookstore_password)
- `JWT_SECRET`: JWT secret key for token signing

### Frontend
The frontend uses a proxy configuration to connect to the backend during development.

## API Endpoints

- Health Check: `GET /api/health`

## Development

- Backend runs on port 8080
- Frontend runs on port 3000
- CORS is configured to allow communication between frontend and backend
- JWT authentication is set up for secure API access

## Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```