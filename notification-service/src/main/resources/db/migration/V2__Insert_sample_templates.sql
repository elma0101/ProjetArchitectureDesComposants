-- Insert sample notification templates
INSERT INTO notification_templates (name, type, subject, content, description, active, created_at)
VALUES 
    ('loan_created', 'EMAIL', 'Loan Confirmation - {{bookTitle}}', 
     'Dear {{userName}},\n\nYour loan request has been confirmed.\n\nBook: {{bookTitle}}\nLoan Date: {{loanDate}}\nDue Date: {{dueDate}}\n\nPlease return the book by the due date to avoid late fees.\n\nThank you for using our library service!',
     'Template for loan creation confirmation', true, NOW()),
    
    ('loan_returned', 'EMAIL', 'Loan Return Confirmation - {{bookTitle}}',
     'Dear {{userName}},\n\nThank you for returning the book.\n\nBook: {{bookTitle}}\nReturn Date: {{returnDate}}\n\nWe hope you enjoyed reading it!\n\nThank you for using our library service!',
     'Template for loan return confirmation', true, NOW()),
    
    ('loan_overdue', 'EMAIL', 'Overdue Loan Notice - {{bookTitle}}',
     'Dear {{userName}},\n\nThis is a reminder that your loan is overdue.\n\nBook: {{bookTitle}}\nDue Date: {{dueDate}}\n\nPlease return the book as soon as possible to avoid additional late fees.\n\nIf you have already returned the book, please disregard this notice.',
     'Template for overdue loan notification', true, NOW()),
    
    ('loan_due_soon', 'EMAIL', 'Loan Due Soon Reminder - {{bookTitle}}',
     'Dear {{userName}},\n\nThis is a friendly reminder that your loan is due soon.\n\nBook: {{bookTitle}}\nDue Date: {{dueDate}}\n\nPlease return the book by the due date to avoid late fees.\n\nThank you for using our library service!',
     'Template for loan due soon reminder', true, NOW());
