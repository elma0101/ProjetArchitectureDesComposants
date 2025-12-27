-- Insert sample loan data
-- Note: These reference user_id and book_id from other services
-- In production, these would be created through the API after validating the references

INSERT INTO loans (user_id, book_id, loan_date, due_date, return_date, status, created_at, updated_at) VALUES
(1, 1, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '4 days', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '9 days', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE - INTERVAL '5 days', 'RETURNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 4, CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE - INTERVAL '11 days', NULL, 'OVERDUE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 5, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '11 days', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert loan tracking records
INSERT INTO loan_tracking (loan_id, status, timestamp, notes, changed_by) VALUES
(1, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '10 days', 'Loan created', 'system'),
(2, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '5 days', 'Loan created', 'system'),
(3, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '20 days', 'Loan created', 'system'),
(3, 'RETURNED', CURRENT_TIMESTAMP - INTERVAL '5 days', 'Book returned on time', 'librarian'),
(4, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '25 days', 'Loan created', 'system'),
(4, 'OVERDUE', CURRENT_TIMESTAMP - INTERVAL '11 days', 'Loan marked as overdue', 'system'),
(5, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3 days', 'Loan created', 'system');
