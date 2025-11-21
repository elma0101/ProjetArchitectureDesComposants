-- Sample data for the bookstore application
-- This file will be executed on application startup to populate the database with test data

-- Insert sample authors
INSERT INTO authors (first_name, last_name, biography, birth_date, nationality, created_at, updated_at) VALUES
('George', 'Orwell', 'English novelist and essayist, journalist and critic, whose work is characterised by lucid prose, biting social criticism, opposition to totalitarianism, and outspoken support of democratic socialism.', '1903-06-25', 'British', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jane', 'Austen', 'English novelist known primarily for her six major novels, which interpret, critique and comment upon the British landed gentry at the end of the 18th century.', '1775-12-16', 'British', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Harper', 'Lee', 'American novelist widely known for To Kill a Mockingbird, published in 1960. Immediately successful, it won the 1961 Pulitzer Prize and has become a classic of modern American literature.', '1926-04-28', 'American', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('F. Scott', 'Fitzgerald', 'American novelist, essayist, screenwriter, and short-story writer, although he is best known for his novels depicting the flamboyance and excess of the Jazz Age.', '1896-09-24', 'American', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('J.K.', 'Rowling', 'British author, philanthropist, film producer, television producer, and screenwriter. She is best known for writing the Harry Potter fantasy series.', '1965-07-31', 'British', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Agatha', 'Christie', 'English writer known for her sixty-six detective novels and fourteen short story collections, particularly those revolving around fictional detectives Hercule Poirot and Miss Marple.', '1890-09-15', 'British', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Stephen', 'King', 'American author of horror, supernatural fiction, suspense, crime, science-fiction, and fantasy novels. His books have sold more than 350 million copies.', '1947-09-21', 'American', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Gabriel', 'García Márquez', 'Colombian novelist, short-story writer, screenwriter, and journalist, known affectionately as Gabo or Gabito throughout Latin America.', '1927-03-06', 'Colombian', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample books
INSERT INTO books (title, isbn, description, publication_year, genre, available_copies, total_copies, created_at, updated_at) VALUES
('1984', '978-0-452-28423-4', 'A dystopian social science fiction novel and cautionary tale about the dangers of totalitarianism.', 1949, 'Dystopian Fiction', 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Animal Farm', '978-0-452-28424-1', 'An allegorical novella about a group of farm animals who rebel against their human farmer.', 1945, 'Political Satire', 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pride and Prejudice', '978-0-14-143951-8', 'A romantic novel of manners written by Jane Austen in 1813.', 1813, 'Romance', 4, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Emma', '978-0-14-143952-5', 'A novel about youthful hubris and romantic misunderstandings.', 1815, 'Romance', 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('To Kill a Mockingbird', '978-0-06-112008-4', 'A novel about racial injustice and the destruction of innocence.', 1960, 'Literary Fiction', 5, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Great Gatsby', '978-0-7432-7356-5', 'A classic American novel about the Jazz Age and the American Dream.', 1925, 'Literary Fiction', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Harry Potter and the Philosopher''s Stone', '978-0-7475-3269-9', 'The first novel in the Harry Potter series about a young wizard''s adventures.', 1997, 'Fantasy', 0, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Harry Potter and the Chamber of Secrets', '978-0-7475-3849-3', 'The second novel in the Harry Potter series.', 1998, 'Fantasy', 6, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Murder on the Orient Express', '978-0-00-711926-0', 'A detective novel featuring the Belgian detective Hercule Poirot.', 1934, 'Mystery', 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('And Then There Were None', '978-0-00-711925-3', 'A mystery novel about ten strangers invited to an island where they are murdered one by one.', 1939, 'Mystery', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Shining', '978-0-385-12167-5', 'A horror novel about a family that becomes caretakers of an isolated hotel.', 1977, 'Horror', 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('It', '978-0-670-81302-4', 'A horror novel about a group of children terrorized by a supernatural entity.', 1986, 'Horror', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('One Hundred Years of Solitude', '978-0-06-088328-7', 'A landmark novel that tells the multi-generational story of the Buendía family.', 1967, 'Magical Realism', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create book-author relationships
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), -- 1984 by George Orwell
(2, 1), -- Animal Farm by George Orwell
(3, 2), -- Pride and Prejudice by Jane Austen
(4, 2), -- Emma by Jane Austen
(5, 3), -- To Kill a Mockingbird by Harper Lee
(6, 4), -- The Great Gatsby by F. Scott Fitzgerald
(7, 5), -- Harry Potter 1 by J.K. Rowling
(8, 5), -- Harry Potter 2 by J.K. Rowling
(9, 6), -- Murder on the Orient Express by Agatha Christie
(10, 6), -- And Then There Were None by Agatha Christie
(11, 7), -- The Shining by Stephen King
(12, 7), -- It by Stephen King
(13, 8); -- One Hundred Years of Solitude by Gabriel García Márquez

-- Insert sample loans
INSERT INTO loans (book_id, borrower_name, borrower_email, borrower_id, loan_date, due_date, return_date, status, notes, created_at, updated_at) VALUES
(1, 'John Smith', 'john.smith@email.com', 'USER001', '2024-01-15', '2024-02-15', '2024-02-10', 'RETURNED', 'Excellent condition upon return', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Emily Johnson', 'emily.johnson@email.com', 'USER002', '2024-01-20', '2024-02-20', NULL, 'ACTIVE', 'First-time borrower', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Michael Brown', 'michael.brown@email.com', 'USER003', '2024-01-10', '2024-02-10', NULL, 'OVERDUE', 'Reminder sent', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Sarah Davis', 'sarah.davis@email.com', 'USER004', '2024-02-01', '2024-03-01', NULL, 'ACTIVE', 'Regular borrower', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'David Wilson', 'david.wilson@email.com', 'USER005', '2024-01-25', '2024-02-25', '2024-02-20', 'RETURNED', 'Loved the book!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Lisa Anderson', 'lisa.anderson@email.com', 'USER006', '2024-02-05', '2024-03-05', NULL, 'ACTIVE', 'Horror fan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample recommendations
INSERT INTO recommendations (user_id, book_id, recommendation_score, recommendation_reason, type, created_at) VALUES
('USER001', 2, 0.95, 'Based on your interest in dystopian fiction', 'CONTENT_BASED', CURRENT_TIMESTAMP),
('USER001', 11, 0.75, 'Popular among users with similar reading preferences', 'COLLABORATIVE', CURRENT_TIMESTAMP),
('USER002', 4, 0.90, 'Another classic by Jane Austen', 'CONTENT_BASED', CURRENT_TIMESTAMP),
('USER002', 6, 0.80, 'Highly rated literary fiction', 'POPULAR', CURRENT_TIMESTAMP),
('USER003', 13, 0.85, 'Based on your interest in literary fiction', 'CONTENT_BASED', CURRENT_TIMESTAMP),
('USER004', 5, 0.92, 'Trending among literature enthusiasts', 'TRENDING', CURRENT_TIMESTAMP),
('USER005', 10, 0.88, 'Another mystery by Agatha Christie', 'CONTENT_BASED', CURRENT_TIMESTAMP),
('USER006', 12, 0.93, 'Another horror novel by Stephen King', 'CONTENT_BASED', CURRENT_TIMESTAMP),
('USER001', 7, 0.70, 'Popular fantasy series', 'POPULAR', CURRENT_TIMESTAMP),
('USER002', 8, 0.65, 'Trending fantasy novel', 'TRENDING', CURRENT_TIMESTAMP);-
- Create loan_tracking table
CREATE TABLE IF NOT EXISTS loan_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_description VARCHAR(500),
    event_timestamp TIMESTAMP NOT NULL,
    additional_data VARCHAR(1000),
    INDEX idx_loan_tracking_loan_id (loan_id),
    INDEX idx_loan_tracking_event_type (event_type),
    INDEX idx_loan_tracking_timestamp (event_timestamp)
);

-- Insert sample loan tracking data
INSERT INTO loan_tracking (loan_id, event_type, event_description, event_timestamp) VALUES
(1, 'LOAN_CREATED', 'Loan created for borrower john.smith@email.com, book ID 1', '2024-01-15 10:00:00'),
(1, 'NOTIFICATION_SENT', 'LOAN_CONFIRMATION', '2024-01-15 10:01:00'),
(2, 'LOAN_CREATED', 'Loan created for borrower emily.johnson@email.com, book ID 3', '2024-01-20 14:30:00'),
(2, 'NOTIFICATION_SENT', 'LOAN_CONFIRMATION', '2024-01-20 14:31:00'),
(3, 'LOAN_CREATED', 'Loan created for borrower michael.brown@email.com, book ID 5', '2024-01-10 09:15:00'),
(3, 'NOTIFICATION_SENT', 'LOAN_CONFIRMATION', '2024-01-10 09:16:00'),
(3, 'STATUS_CHANGE', 'Status changed from ACTIVE to OVERDUE', '2024-02-11 09:00:00'),
(3, 'NOTIFICATION_SENT', 'OVERDUE_NOTIFICATION', '2024-02-11 09:01:00'),
(1, 'STATUS_CHANGE', 'Status changed from ACTIVE to RETURNED', '2024-02-10 16:45:00'),
(1, 'LOAN_RETURNED', 'Book returned on time', '2024-02-10 16:45:00'),
(1, 'NOTIFICATION_SENT', 'RETURN_CONFIRMATION', '2024-02-10 16:46:00'),
(4, 'LOAN_CREATED', 'Loan created for borrower sarah.davis@email.com, book ID 6', '2024-02-01 11:20:00'),
(4, 'NOTIFICATION_SENT', 'LOAN_CONFIRMATION', '2024-02-01 11:21:00'),
(5, 'LOAN_CREATED', 'Loan created for borrower david.wilson@email.com, book ID 9', '2024-01-25 13:45:00'),
(5, 'NOTIFICATION_SENT', 'LOAN_CONFIRMATION', '2024-01-25 13:46:00'),
(5, 'STATUS_CHANGE', 'Status changed from ACTIVE to RETURNED', '2024-02-20 15:30:00'),
(5, 'LOAN_RETURNED', 'Book returned on time', '2024-02-20 15:30:00'),
(5, 'NOTIFICATION_SENT', 'RETURN_CONFIRMATION', '2024-02-20 15:31:00'),
(6, 'LOAN_CREATED', 'Loan created for borrower lisa.anderson@email.com, book ID 11', '2024-02-05 10:15:00'),
(6, 'NOTIFICATION_SENT', 'LOAN_CONFIRMATION', '2024-02-05 10:16:00');

-- Insert default users with encrypted passwords
-- Password for all users is 'password123' (BCrypt encrypted)
INSERT INTO users (username, password, email, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDu', 'admin@bookstore.com', 'Admin', 'User', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('librarian', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDu', 'librarian@bookstore.com', 'Library', 'Staff', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDu', 'user1@bookstore.com', 'John', 'Smith', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDu', 'user2@bookstore.com', 'Emily', 'Johnson', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert user roles
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'),
(2, 'LIBRARIAN'),
(3, 'USER'),
(4, 'USER');