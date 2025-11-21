# Library Management System - Frontend

A comprehensive React frontend for the Library Management System built with TypeScript, Tailwind CSS, and modern React patterns.

## 🚀 Features

### 📚 Book Management
- **Book Catalog**: Browse, search, and filter books by title, author, genre, publication year
- **Book Details**: Detailed book information with author details and availability status
- **Advanced Search**: Multi-criteria search with pagination and sorting
- **Book Recommendations**: Personalized, popular, and trending book recommendations

### 👥 Author Management
- **Author Profiles**: Detailed author information with biography and book listings
- **Author Search**: Search authors by name, nationality, and other criteria
- **Author-Book Relationships**: View all books by specific authors

### 📋 Loan Management
- **Borrow Books**: Interactive borrowing process with form validation
- **Loan Tracking**: View active, overdue, and returned loans
- **Loan Actions**: Return books, extend loan periods
- **Loan History**: Complete borrowing history for users
- **Overdue Alerts**: Visual indicators for overdue books

### 🎯 Recommendation System
- **Personalized Recommendations**: Based on user borrowing history
- **Popular Books**: Most borrowed books in the system
- **Trending Books**: Recently popular books
- **Multiple Algorithms**: Collaborative filtering, content-based, and popularity-based

### 👤 User Management
- **Authentication**: Login/Register with JWT tokens
- **User Profiles**: View and manage user information
- **Role-Based Access**: Different interfaces for users and administrators
- **My Loans**: Personal loan management interface

### 🔧 Admin Features
- **Dashboard**: System statistics and health monitoring
- **User Management**: View and manage user accounts
- **System Controls**: Update overdue loans, system maintenance
- **Analytics**: Book and loan statistics

## 🏗️ Architecture

### Component Structure
```
src/
├── components/
│   ├── Layout/
│   │   ├── Header.tsx          # Navigation header
│   │   └── Layout.tsx          # Main layout wrapper
│   ├── Common/
│   │   ├── LoadingSpinner.tsx  # Loading indicator
│   │   └── Pagination.tsx      # Pagination component
│   ├── Books/
│   │   ├── BookList.tsx        # Book catalog listing
│   │   ├── BookCard.tsx        # Individual book display
│   │   ├── BookSearch.tsx      # Advanced search form
│   │   └── BookDetail.tsx      # Detailed book view
│   └── Loans/
│       ├── LoanList.tsx        # Loan management interface
│       ├── LoanCard.tsx        # Individual loan display
│       ├── LoanSearch.tsx      # Loan search and filters
│       └── BorrowBookModal.tsx # Book borrowing modal
├── pages/
│   ├── HomePage.tsx            # Dashboard/landing page
│   ├── BooksPage.tsx           # Books catalog page
│   ├── BookDetailPage.tsx      # Individual book page
│   ├── AuthorsPage.tsx         # Authors listing page
│   ├── AuthorDetailPage.tsx    # Individual author page
│   ├── LoansPage.tsx           # Loan management page
│   ├── RecommendationsPage.tsx # Recommendations page
│   ├── LoginPage.tsx           # User login
│   ├── RegisterPage.tsx        # User registration
│   ├── ProfilePage.tsx         # User profile
│   ├── MyLoansPage.tsx         # Personal loans
│   └── AdminPage.tsx           # Admin dashboard
├── services/
│   └── api.ts                  # API service layer
├── types/
│   └── index.ts                # TypeScript type definitions
└── App.tsx                     # Main application component
```

### State Management
- **React Context**: Authentication state management
- **Local State**: Component-level state with React hooks
- **API Integration**: Axios-based HTTP client with interceptors

### Routing
- **React Router v6**: Client-side routing
- **Protected Routes**: Authentication-based route protection
- **Role-Based Routes**: Admin-only route access

## 🎨 UI/UX Design

### Design System
- **Tailwind CSS**: Utility-first CSS framework
- **Responsive Design**: Mobile-first responsive layouts
- **Consistent Styling**: Reusable component patterns
- **Accessibility**: WCAG-compliant interface elements

### User Experience
- **Loading States**: Comprehensive loading indicators
- **Error Handling**: User-friendly error messages
- **Form Validation**: Real-time form validation
- **Visual Feedback**: Success/error notifications
- **Intuitive Navigation**: Clear navigation patterns

## 🔌 API Integration

### Service Layer
The application uses a comprehensive API service layer that interfaces with the Spring Boot backend:

#### Authentication API
- Login/logout functionality
- User registration
- Profile management
- JWT token handling

#### Books API
- CRUD operations for books
- Advanced search and filtering
- Popularity and trending queries
- Availability status checks

#### Authors API
- Author management
- Book-author relationships
- Author search and statistics

#### Loans API
- Book borrowing and returning
- Loan tracking and history
- Overdue management
- Loan statistics

#### Recommendations API
- Personalized recommendations
- Popular and trending books
- Multiple recommendation algorithms

### Error Handling
- **Global Error Interceptor**: Centralized error handling
- **Authentication Errors**: Automatic token refresh/logout
- **Network Errors**: Retry mechanisms and offline detection
- **Validation Errors**: Form-specific error display

## 🚦 Getting Started

### Prerequisites
- Node.js 16+ and npm
- Backend API running on `http://localhost:8080`

### Installation
```bash
# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build
```

### Environment Configuration
Create a `.env` file in the frontend directory:
```env
REACT_APP_API_URL=http://localhost:8080/api
```

## 🧪 Testing

### Testing Strategy
- **Unit Tests**: Component testing with React Testing Library
- **Integration Tests**: API integration testing
- **E2E Tests**: User workflow testing
- **Accessibility Tests**: Automated accessibility testing

### Running Tests
```bash
# Run all tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm test -- --watch
```

## 📱 Responsive Design

### Breakpoints
- **Mobile**: 320px - 768px
- **Tablet**: 768px - 1024px
- **Desktop**: 1024px+

### Mobile Features
- Touch-friendly interface
- Optimized navigation
- Responsive grid layouts
- Mobile-specific interactions

## 🔒 Security

### Authentication
- JWT token-based authentication
- Automatic token refresh
- Secure token storage
- Role-based access control

### Data Protection
- Input sanitization
- XSS protection
- CSRF protection via backend
- Secure API communication

## 🎯 Key Features Implementation

### Book Borrowing Flow
1. User browses book catalog
2. Selects book and clicks "Borrow"
3. Fills out borrowing form with validation
4. System checks availability
5. Creates loan record
6. Updates book availability
7. Provides confirmation

### Search & Filtering
- Real-time search suggestions
- Multi-criteria filtering
- Pagination with page size options
- Sort by multiple fields
- Search result highlighting

### Recommendation Engine Integration
- Multiple recommendation types
- Real-time recommendation generation
- Personalized based on user history
- Fallback to popular books for new users

### Admin Dashboard
- System health monitoring
- Real-time statistics
- User management interface
- Bulk operations support
- System maintenance tools

## 🔄 State Management Patterns

### Authentication Context
```typescript
interface AuthContextType {
  user: User | null;
  login: (credentials) => Promise<void>;
  logout: () => void;
  loading: boolean;
}
```

### API State Management
- Loading states for all async operations
- Error state handling with user feedback
- Optimistic updates where appropriate
- Cache invalidation strategies

## 📊 Performance Optimization

### Code Splitting
- Route-based code splitting
- Lazy loading of components
- Dynamic imports for large libraries

### Caching Strategy
- API response caching
- Image lazy loading
- Browser caching optimization

### Bundle Optimization
- Tree shaking for unused code
- Minification and compression
- Asset optimization

## 🌐 Internationalization Ready

The application is structured to support internationalization:
- Externalized text strings
- Date/time formatting
- Number formatting
- RTL language support ready

## 🚀 Deployment

### Build Process
```bash
# Create production build
npm run build

# Serve static files
npm install -g serve
serve -s build
```

### Docker Support
```dockerfile
FROM node:16-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 🤝 Contributing

### Development Guidelines
- Follow TypeScript best practices
- Use functional components with hooks
- Implement proper error boundaries
- Write comprehensive tests
- Follow accessibility guidelines

### Code Style
- ESLint configuration for code quality
- Prettier for code formatting
- Consistent naming conventions
- Component documentation

## 📈 Future Enhancements

### Planned Features
- Real-time notifications
- Advanced analytics dashboard
- Mobile app version
- Offline support
- Multi-language support
- Advanced search with AI
- Social features (reviews, ratings)
- Integration with external book APIs

### Technical Improvements
- Service worker implementation
- Progressive Web App features
- Advanced caching strategies
- Performance monitoring
- A/B testing framework

This frontend provides a complete, production-ready interface for the Library Management System with modern React patterns, comprehensive error handling, and excellent user experience.