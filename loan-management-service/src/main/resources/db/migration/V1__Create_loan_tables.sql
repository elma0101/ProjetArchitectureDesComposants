-- Create loans table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    loan_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_loan_status CHECK (status IN ('ACTIVE', 'RETURNED', 'OVERDUE', 'CANCELLED'))
);

-- Create loan_tracking table
CREATE TABLE loan_tracking (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),
    changed_by VARCHAR(255),
    CONSTRAINT chk_tracking_status CHECK (status IN ('ACTIVE', 'RETURNED', 'OVERDUE', 'CANCELLED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_loans_book_id ON loans(book_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_due_date ON loans(due_date);
CREATE INDEX idx_loans_user_status ON loans(user_id, status);
CREATE INDEX idx_loan_tracking_loan_id ON loan_tracking(loan_id);
CREATE INDEX idx_loan_tracking_timestamp ON loan_tracking(timestamp);

-- Add comments for documentation
COMMENT ON TABLE loans IS 'Stores book loan records';
COMMENT ON TABLE loan_tracking IS 'Tracks loan status changes and history';
COMMENT ON COLUMN loans.user_id IS 'Reference to user in user-management-service';
COMMENT ON COLUMN loans.book_id IS 'Reference to book in book-catalog-service';
COMMENT ON COLUMN loans.status IS 'Current status of the loan';
COMMENT ON COLUMN loan_tracking.loan_id IS 'Reference to the loan being tracked';
