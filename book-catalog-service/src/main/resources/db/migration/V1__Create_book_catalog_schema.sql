-- Create authors table
CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    biography VARCHAR(1000),
    birth_date DATE,
    nationality VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create books table
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    description VARCHAR(2000),
    publication_year INTEGER,
    genre VARCHAR(100),
    total_copies INTEGER NOT NULL DEFAULT 0,
    available_copies INTEGER NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_copies CHECK (available_copies >= 0 AND available_copies <= total_copies),
    CONSTRAINT chk_publication_year CHECK (publication_year >= 1000 AND publication_year <= 2100)
);

-- Create book_authors junction table
CREATE TABLE book_authors (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_book_isbn ON books(isbn);
CREATE INDEX idx_book_title ON books(title);
CREATE INDEX idx_book_genre ON books(genre);
CREATE INDEX idx_book_publication_year ON books(publication_year);
CREATE INDEX idx_book_available_copies ON books(available_copies);
CREATE INDEX idx_book_created_at ON books(created_at);

CREATE INDEX idx_author_first_name ON authors(first_name);
CREATE INDEX idx_author_last_name ON authors(last_name);
CREATE INDEX idx_author_full_name ON authors(first_name, last_name);
CREATE INDEX idx_author_nationality ON authors(nationality);
CREATE INDEX idx_author_birth_date ON authors(birth_date);
CREATE INDEX idx_author_created_at ON authors(created_at);

CREATE INDEX idx_book_authors_book_id ON book_authors(book_id);
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);

