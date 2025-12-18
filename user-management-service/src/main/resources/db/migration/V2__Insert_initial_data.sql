-- Insert default admin user
-- Password is 'admin123' (should be bcrypt encoded in production)
INSERT INTO users (username, password, email, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES ('admin', '$2a$10$xqxqxqxqxqxqxqxqxqxqxOK8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8', 'admin@bookstore.com', 'Admin', 'User', true, true, true, true);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role)
VALUES (1, 'ADMIN');

-- Insert default librarian user
-- Password is 'librarian123'
INSERT INTO users (username, password, email, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES ('librarian', '$2a$10$xqxqxqxqxqxqxqxqxqxqxOK8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8', 'librarian@bookstore.com', 'Librarian', 'User', true, true, true, true);

-- Assign LIBRARIAN role to librarian user
INSERT INTO user_roles (user_id, role)
VALUES (2, 'LIBRARIAN');

-- Insert default regular user
-- Password is 'user123'
INSERT INTO users (username, password, email, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES ('user', '$2a$10$xqxqxqxqxqxqxqxqxqxqxOK8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8', 'user@bookstore.com', 'Regular', 'User', true, true, true, true);

-- Assign USER role to regular user
INSERT INTO user_roles (user_id, role)
VALUES (3, 'USER');
