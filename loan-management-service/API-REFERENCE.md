# Loan Management Service - API Reference

## Base URL
```
http://localhost:8083/api/loans
```

When accessed through API Gateway:
```
http://localhost:8080/api/loans
```

## Endpoints

### 1. Create Loan
Create a new loan for a book.

**Endpoint:** `POST /api/loans`

**Request Body:**
```json
{
  "userId": 1,
  "bookId": 1,
  "notes": "Optional notes"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 1,
  "bookId": 1,
  "loanDate": "2024-12-27",
  "dueDate": "2025-01-10",
  "returnDate": null,
  "status": "ACTIVE",
  "createdAt": "2024-12-27T10:30:00",
  "updatedAt": "2024-12-27T10:30:00",
  "overdue": false
}
```

---

### 2. Return Loan
Return a borrowed book.

**Endpoint:** `PUT /api/loans/{id}/return`

**Request Body (Optional):**
```json
{
  "notes": "Returned in good condition"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "bookId": 1,
  "loanDate": "2024-12-27",
  "dueDate": "2025-01-10",
  "returnDate": "2024-12-30",
  "status": "RETURNED",
  "createdAt": "2024-12-27T10:30:00",
  "updatedAt": "2024-12-30T14:20:00",
  "overdue": false
}
```

---

### 3. Extend Loan
Extend the due date of a loan.

**Endpoint:** `PUT /api/loans/{id}/extend`

**Query Parameters:**
- `days` (optional, default: 7) - Number of days to extend

**Example:** `PUT /api/loans/1/extend?days=7`

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "bookId": 1,
  "loanDate": "2024-12-27",
  "dueDate": "2025-01-17",
  "returnDate": null,
  "status": "ACTIVE",
  "createdAt": "2024-12-27T10:30:00",
  "updatedAt": "2024-12-28T09:15:00",
  "overdue": false
}
```

---

### 4. Get Loan by ID
Retrieve a specific loan by its ID.

**Endpoint:** `GET /api/loans/{id}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "bookId": 1,
  "loanDate": "2024-12-27",
  "dueDate": "2025-01-10",
  "returnDate": null,
  "status": "ACTIVE",
  "createdAt": "2024-12-27T10:30:00",
  "updatedAt": "2024-12-27T10:30:00",
  "overdue": false
}
```

---

### 5. Get Loan History
Get the tracking history for a specific loan.

**Endpoint:** `GET /api/loans/{id}/history`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "loanId": 1,
    "status": "ACTIVE",
    "timestamp": "2024-12-27T10:30:00",
    "notes": "Loan created",
    "changedBy": "system"
  },
  {
    "id": 2,
    "loanId": 1,
    "status": "RETURNED",
    "timestamp": "2024-12-30T14:20:00",
    "notes": "Loan returned",
    "changedBy": "system"
  }
]
```

---

### 6. Get User Loans (Paginated)
Get all loans for a specific user with pagination.

**Endpoint:** `GET /api/loans/user/{userId}`

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size
- `sort` (optional) - Sort field and direction (e.g., `loanDate,desc`)

**Example:** `GET /api/loans/user/1?page=0&size=10`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "bookId": 1,
      "loanDate": "2024-12-27",
      "dueDate": "2025-01-10",
      "status": "ACTIVE",
      "overdue": false
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 7. Get Active User Loans
Get all active loans for a specific user.

**Endpoint:** `GET /api/loans/user/{userId}/active`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "bookId": 1,
    "loanDate": "2024-12-27",
    "dueDate": "2025-01-10",
    "status": "ACTIVE",
    "overdue": false
  }
]
```

---

### 8. Get Book Loans
Get all loans for a specific book.

**Endpoint:** `GET /api/loans/book/{bookId}`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "bookId": 1,
    "loanDate": "2024-12-27",
    "dueDate": "2025-01-10",
    "status": "ACTIVE",
    "overdue": false
  }
]
```

---

### 9. Search Loans
Search and filter loans with multiple criteria.

**Endpoint:** `GET /api/loans/search`

**Query Parameters:**
- `userId` (optional) - Filter by user ID
- `bookId` (optional) - Filter by book ID
- `status` (optional) - Filter by status (ACTIVE, RETURNED, OVERDUE, CANCELLED)
- `fromDate` (optional) - Filter by loan date from (ISO date format)
- `toDate` (optional) - Filter by loan date to (ISO date format)
- `overdue` (optional) - Filter by overdue status (true/false)

**Example:** `GET /api/loans/search?userId=1&status=ACTIVE&overdue=false`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "bookId": 1,
    "loanDate": "2024-12-27",
    "dueDate": "2025-01-10",
    "status": "ACTIVE",
    "overdue": false
  }
]
```

---

### 10. Get Overdue Loans
Get all loans that are currently overdue.

**Endpoint:** `GET /api/loans/overdue`

**Response:** `200 OK`
```json
[
  {
    "id": 2,
    "userId": 2,
    "bookId": 2,
    "loanDate": "2024-11-01",
    "dueDate": "2024-11-15",
    "status": "OVERDUE",
    "overdue": true
  }
]
```

---

### 11. Update Overdue Loans
Update the status of all overdue loans (admin operation).

**Endpoint:** `POST /api/loans/overdue/update`

**Response:** `200 OK`
```
5
```
(Returns the number of loans updated)

---

### 12. Get Loan Statistics
Get overall loan statistics.

**Endpoint:** `GET /api/loans/statistics`

**Response:** `200 OK`
```json
{
  "activeLoans": 10,
  "overdueLoans": 2,
  "returnedLoans": 50,
  "totalLoans": 62
}
```

---

### 13. Get User Loan Count
Get the count of active loans for a specific user.

**Endpoint:** `GET /api/loans/analytics/user/{userId}`

**Response:** `200 OK`
```
3
```

---

### 14. Get Book Loan Count
Get the count of active loans for a specific book.

**Endpoint:** `GET /api/loans/analytics/book/{bookId}`

**Response:** `200 OK`
```
2
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-12-27T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: User ID is required, Book ID is required"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-12-27T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Loan not found with ID: 123"
}
```

### 409 Conflict
```json
{
  "timestamp": "2024-12-27T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Book is not available for loan: The Great Gatsby"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-12-27T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

### 503 Service Unavailable
```json
{
  "timestamp": "2024-12-27T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Book Catalog Service is currently unavailable"
}
```

---

## Loan Status Values

- `ACTIVE` - Loan is currently active
- `RETURNED` - Book has been returned
- `OVERDUE` - Loan is past due date
- `CANCELLED` - Loan has been cancelled

---

## Notes

1. All dates are in ISO 8601 format (YYYY-MM-DD)
2. All timestamps are in ISO 8601 format with timezone
3. Default loan period is 14 days
4. Maximum extension period is 30 days
5. Loans are automatically marked as overdue when past due date
6. Book availability is validated before loan creation
7. Book availability is updated on loan creation and return
