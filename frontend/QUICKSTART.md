# Quick Start Guide - Library System Frontend

Get the frontend up and running in 5 minutes!

## Prerequisites

✅ Node.js 16+ installed  
✅ npm or yarn installed  
✅ Backend microservices running (API Gateway on port 8080)

## Step 1: Install Dependencies

```bash
cd frontend
npm install
```

## Step 2: Configure Environment

Create a `.env` file in the `frontend` directory:

```env
REACT_APP_API_GATEWAY_URL=http://localhost:8080
```

## Step 3: Start Development Server

```bash
npm start
```

The app will open at `http://localhost:3000`

## Step 4: Test the Application

### Option A: Use Existing Test Users

The backend comes with pre-seeded users:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| librarian | librarian123 | LIBRARIAN |
| user | user123 | USER |

### Option B: Register a New Account

1. Click "Register" in the navigation
2. Fill in the registration form
3. Submit to create your account

## Quick Tour

### 1. Browse Books
- Navigate to "Books" in the menu
- Use search to find specific books
- Filter by genre
- Click on any book to see details

### 2. Borrow a Book
- Go to a book's detail page
- Click "Borrow Book" (must be logged in)
- Book appears in "My Loans"

### 3. View Your Loans
- Click "My Loans" in the menu
- See all active loans and history
- Click "Return Book" to return a borrowed book

### 4. Get Recommendations
- Click "Recommendations" in the menu
- View personalized book suggestions
- Filter by recommendation type
- Click "Refresh" to update recommendations

## File Structure Overview

```
frontend/
├── src/
│   ├── components/          # Reusable UI components
│   ├── context/            # React Context (Auth)
│   ├── pages/              # Page components
│   ├── services/           # API service layer
│   ├── types/              # TypeScript types
│   └── App.new.tsx         # Main app component
├── public/                 # Static assets
└── package.json           # Dependencies
```

## Key Files to Know

### Services (API Integration)
- `src/services/authService.ts` - Login, register, logout
- `src/services/bookService.ts` - Browse and search books
- `src/services/loanService.ts` - Borrow and return books
- `src/services/recommendationService.ts` - Get recommendations

### Pages
- `src/pages/Home.tsx` - Landing page
- `src/pages/Books.tsx` - Book catalog
- `src/pages/BookDetail.tsx` - Individual book page
- `src/pages/MyLoans.tsx` - User's loans
- `src/pages/Recommendations.tsx` - Personalized recommendations

### Context
- `src/context/AuthContext.tsx` - Authentication state management

## Common Tasks

### Add a New Page

1. Create page component in `src/pages/`
2. Add route in `src/App.new.tsx`
3. Add navigation link in `src/components/Navbar.tsx`

### Add a New API Service

1. Create service file in `src/services/`
2. Import `apiClient` from `./apiClient`
3. Export functions that call API endpoints

### Update Types

Edit `src/types/api.types.ts` to match backend DTOs

## Troubleshooting

### "Cannot connect to API"
- Ensure API Gateway is running on port 8080
- Check `.env` file has correct URL
- Verify CORS is enabled in API Gateway

### "Login failed"
- Verify User Management Service is running
- Check username/password
- Look at browser console for errors

### "Books not loading"
- Ensure Book Catalog Service is running
- Check API Gateway routing configuration
- Verify service is registered with Eureka

### Build errors
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

## Next Steps

1. **Customize Styling**: Edit Tailwind classes in components
2. **Add Features**: Implement additional pages or functionality
3. **Integrate More Services**: Connect to Notification Service
4. **Add Tests**: Write unit tests for components
5. **Deploy**: Build and deploy to production

## Useful Commands

```bash
# Start development server
npm start

# Run tests
npm test

# Build for production
npm run build

# Run linter
npm run lint

# Format code
npm run format
```

## Getting Help

- Check `MICROSERVICES_FRONTEND.md` for detailed documentation
- Review backend service READMEs for API details
- Check browser console for errors
- Review network tab for API responses

## Default Test Data

The backend includes sample books and authors. After starting the services, you'll see:
- Multiple books across different genres
- Various authors
- Sample loan data (if using test users)

Happy coding! 🚀
