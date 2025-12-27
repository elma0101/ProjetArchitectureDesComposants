-- Insert sample authors
INSERT INTO authors (first_name, last_name, biography, nationality, birth_date) VALUES
('George', 'Orwell', 'English novelist and essayist, journalist and critic', 'British', '1903-06-25'),
('Jane', 'Austen', 'English novelist known primarily for her six major novels', 'British', '1775-12-16'),
('Mark', 'Twain', 'American writer, humorist, entrepreneur, publisher, and lecturer', 'American', '1835-11-30'),
('F. Scott', 'Fitzgerald', 'American novelist and short story writer', 'American', '1896-09-24'),
('Harper', 'Lee', 'American novelist best known for To Kill a Mockingbird', 'American', '1926-04-28');

-- Insert sample books
INSERT INTO books (title, isbn, description, publication_year, genre, total_copies, available_copies, image_url) VALUES
('1984', '9780452284234', 'A dystopian social science fiction novel and cautionary tale about the dangers of totalitarianism', 1949, 'Science Fiction', 5, 5, NULL),
('Animal Farm', '9780452284241', 'An allegorical novella about Soviet totalitarianism and the corruption of socialist ideals', 1945, 'Political Fiction', 3, 3, NULL),
('Pride and Prejudice', '9780141439518', 'A romantic novel of manners that critiques the British landed gentry at the end of the 18th century', 1813, 'Romance', 4, 4, NULL),
('The Adventures of Tom Sawyer', '9780143039563', 'A novel about a boy growing up along the Mississippi River in the 1840s', 1876, 'Adventure', 3, 3, NULL),
('The Great Gatsby', '9780743273565', 'A novel about the American Dream, wealth, and the Jazz Age', 1925, 'Literary Fiction', 6, 6, NULL),
('To Kill a Mockingbird', '9780061120084', 'A novel about racial injustice and the loss of innocence in the Deep South', 1960, 'Literary Fiction', 5, 5, NULL);

-- Link books to authors
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), -- 1984 by George Orwell
(2, 1), -- Animal Farm by George Orwell
(3, 2), -- Pride and Prejudice by Jane Austen
(4, 3), -- Tom Sawyer by Mark Twain
(5, 4), -- The Great Gatsby by F. Scott Fitzgerald
(6, 5); -- To Kill a Mockingbird by Harper Lee

