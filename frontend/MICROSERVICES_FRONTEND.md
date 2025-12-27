# Library System Frontend - Microservices Architecture

A modern React TypeScript frontend for the Library Management System microservices architecture.

## Architecture Overview

This frontend is designed to work with the following microservices:

- **API Gateway** (Port 8080) - Single entry point for all API requests
- **User Management Service** (Port 8081) - Authentication and user management
- **Book Catalog Service** (Port 8082) - Books and authors management
- **Loan Management Service** (Port 8083) - Book borrowing and returns
- **Recommendation Service** (Port 8084) - Personalized book recommendations
- **Notification Service** (Port 8086) - User notifications

## Features

### 1. Authentication & User Management
- User registration with email validation
- Secure login with JWT tokens
- Token refresh mechanism
- Role-based access control (USER, LIBRARIAN, ADMIN)
- Automatic token expiration handling

### 2. Book Catalog
- Browse books with pagination
- Search books by title, author, or ISBN
- Filter by genre
- View detailed book information
- Author profiles with book listings
- Real-time availability status

### 3. Loan Management
- Borrow books with one click
- View active loans
- Return books
- Loan history tracking
- Overdue loan indicators
- Due date reminders

### 4. Personalized Recommendations
- Multiple recommendation types:
  - Popular books
  - Trending books
  - Content-based (based on preferences)
  - Collaborative filtering
- Refresh recommendations on demand
- Filter by recommendation type
- Integration with user borrowing history

### 5. Notifications (Future Enhancement)
- In-app notifications
- Email notifications for due dates
- Overdue reminders
- New recommendation alerts

## Technology Stack

- **React 18.2** - UI framework
- **TypeScript 4.9** - Type safety
- **React Router 6** - Client-side routing
- **Axios** - HTTP client
- **Tailwind CSS 3.3** - Styling
- **Context API** - State management

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── Navbar.tsx              # Navigation bar
│   │   └── ProtectedRoute.tsx      # Route protection
│   ├── context/
│   │   └── AuthContext.tsx         # Authentication state
│   ├── pages/
│   │   ├── Home.tsx                # Landing page
│   │   ├── Login.tsx               # Login page
│   │   ├── Register.tsx            # Registration page
│   │   ├── Books.tsx               # Book listing
│   │   ├── BookDetail.tsx          # Book details
│   │   ├── MyLoans.tsx             # User loans
│   │   └── Recommendations.tsx     # Recommendations
│   ├── services/
│   │   ├── apiClient.ts            # Axios configuration
│   │   ├── authService.ts          # Authentication API
│   │   ├── bookService.ts          # Book catalog API
│   │   ├── loanService.ts          # Loan management API
│   │   ├── recommendationService.ts # Recommendations API
│   │   └── notificationService.ts  # Notifications API
│   ├── types/
│   │   └── api.types.ts            # TypeScript interfaces
│   ├── App.new.tsx                 # Main application
│   └── index.tsx                   # Entry point
├── public/
├── package.json
└── tailwind.config.js
```

## Setup Instructions

### Prerequisites

- Node.js 16+ and npm
- Running microservices (API Gateway, User Management, Book Catalog, Loan Management, Recommendation Service)

### Installation

1. Install dependencies:
```bash
cd frontend
npm install
```

2. Configure environment variables:
Create a `.env` file in the frontend directory:
```env
REACT_APP_API_GATEWAY_URL=http://localhost:8080
```

3. Start the development server:
```bash
npm start
```

The application will open at `http://localhost:3000`

### Build for Production

```bash
npm run build
```

This creates an optimized production build in the `build/` directory.

## API Integration

### Authentication Flow

1. User logs in via `/api/auth/login`
2. JWT token is stored in localStorage
3. Token is automatically added to all API requests via Axios interceptor
4. On 401 response, user is redirected to login

### Service Communication

All API calls go through the API Gateway (port 8080), which routes to appropriate microservices:

- `/api/auth/*` → User Management Service
- `/api/users/*` → User Management Service
- `/api/books/*` → Book Catalog Service
- `/api/authors/*` → Book Catalog Service
- `/api/loans/*` → Loan Management Service
- `/api/recommendations/*` → Recommendation Service
- `/api/notifications/*` → Notification Service

### Error Handling

- Network errors are caught and displayed to users
- 401 errors trigger automatic logout and redirect to login
- 403 errors show access denied messages
- 404 errors show not found pages
- 500 errors show server error messages

## Key Components

### AuthContext

Manages authentication state across the application:
- Current user information
- Login/logout functions
- Authentication status
- Loading state

### Protected Routes

Wraps routes that require authentication:
- Checks if user is logged in
- Redirects to login if not authenticated
- Shows loading spinner during auth check

### API Services

Encapsulate all API calls:
- Type-safe request/response handling
- Automatic token injection
- Error handling
- Response transformation

## User Flows

### New User Registration
1. Navigate to `/register`
2. Fill in registration form
3. Submit → Creates account and logs in
4. Redirected to home page

### Borrowing a Book
1. Browse books at `/books`
2. Click on a book to view details
3. Click "Borrow Book" button
4. Book is added to "My Loans"
5. Available copies decremented

### Returning a Book
1. Navigate to `/my-loans`
2. Find active loan
3. Click "Return Book" button
4. Loan status updated to RETURNED
5. Available copies incremented

### Getting Recommendations
1. Navigate to `/recommendations`
2. View personalized recommendations
3. Filter by recommendation type
4. Click "Refresh" to update recommendations
5. Click on book to view details and borrow

## Styling

The application uses Tailwind CSS for styling with a consistent design system:

- **Primary Color**: Blue (#2563EB)
- **Success Color**: Green (#059669)
- **Error Color**: Red (#DC2626)
- **Gray Scale**: Tailwind's default gray palette

### Responsive Design

- Mobile-first approach
- Breakpoints:
  - sm: 640px
  - md: 768px
  - lg: 1024px
  - xl: 1280px

## Testing

Run tests:
```bash
npm test
```

Run tests with coverage:
```bash
npm test -- --coverage
```

## Deployment

### Docker Deployment

The frontend includes a Dockerfile for containerized deployment:

```bash
docker build -t library-frontend .
docker run -p 3000:80 library-frontend
```

### Kubernetes Deployment

Deploy to Kubernetes using the provided manifests:

```bash
kubectl apply -f kubernetes/frontend-deployment.yaml
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REACT_APP_API_GATEWAY_URL` | API Gateway URL | `http://localhost:8080` |

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance Optimizations

- Code splitting with React.lazy
- Image lazy loading
- Pagination for large lists
- Debounced search inputs
- Memoized components
- Optimized bundle size

## Security Features

- JWT token-based authentication
- Automatic token refresh
- XSS protection via React
- CSRF protection
- Secure HTTP-only cookies (when configured)
- Input validation and sanitization

## Future Enhancements

1. **Real-time Updates**
   - WebSocket integration for live notifications
   - Real-time availability updates

2. **Advanced Search**
   - Faceted search
   - Advanced filters
   - Search history

3. **Social Features**
   - Book reviews and ratings
   - Reading lists
   - Share recommendations

4. **Accessibility**
   - ARIA labels
   - Keyboard navigation
   - Screen reader support

5. **PWA Features**
   - Offline support
   - Push notifications
   - Install prompt

## Troubleshooting

### API Connection Issues

If you can't connect to the API:
1. Verify API Gateway is running on port 8080
2. Check CORS configuration in API Gateway
3. Verify environment variables
4. Check browser console for errors

### Authentication Issues

If login fails:
1. Verify User Management Service is running
2. Check JWT secret configuration
3. Clear localStorage and try again
4. Check network tab for API responses

### Build Issues

If build fails:
1. Delete `node_modules` and `package-lock.json`
2. Run `npm install` again
3. Clear npm cache: `npm cache clean --force`
4. Check Node.js version compatibility

## Contributing

1. Follow the existing code structure
2. Use TypeScript for type safety
3. Follow React best practices
4. Write meaningful commit messages
5. Test thoroughly before submitting

## License

Copyright © 2024 Library Management System
