package com.bookstore.catalog.service;

import com.bookstore.catalog.dto.BookRequest;
import com.bookstore.catalog.dto.BookResponse;
import com.bookstore.catalog.entity.Author;
import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.event.*;
import com.bookstore.catalog.repository.AuthorRepository;
import com.bookstore.catalog.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceEventIntegrationTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookEventPublisher eventPublisher;

    @InjectMocks
    private BookService bookService;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        testAuthor.setBiography("Test author");
        testAuthor.setBirthDate(LocalDate.of(1970, 1, 1));
        testAuthor.setNationality("American");

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0-123456-78-9");
        testBook.setDescription("Test description");
        testBook.setPublicationYear(2024);
        testBook.setGenre("Fiction");
        testBook.setTotalCopies(10);
        testBook.setAvailableCopies(10);
        testBook.setAuthors(new HashSet<>(Set.of(testAuthor)));
    }

    @Test
    void createBook_shouldPublishBookCreatedEvent() {
        // Given
        BookRequest request = new BookRequest();
        request.setTitle("Test Book");
        request.setIsbn("978-0-123456-78-9");
        request.setDescription("Test description");
        request.setPublicationYear(2024);
        request.setGenre("Fiction");
        request.setTotalCopies(10);
        request.setAvailableCopies(10);
        request.setAuthorIds(Set.of(1L));

        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        BookResponse response = bookService.createBook(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Book");

        ArgumentCaptor<BookCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookCreatedEvent.class);
        verify(eventPublisher).publishBookCreated(eventCaptor.capture());

        BookCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getBookId()).isEqualTo(1L);
        assertThat(capturedEvent.getIsbn()).isEqualTo("978-0-123456-78-9");
        assertThat(capturedEvent.getTitle()).isEqualTo("Test Book");
        assertThat(capturedEvent.getEventType()).isEqualTo("BOOK_CREATED");
    }

    @Test
    void updateBook_shouldPublishBookUpdatedEvent() {
        // Given
        BookRequest request = new BookRequest();
        request.setTitle("Updated Book");
        request.setIsbn("978-0-123456-78-9");
        request.setDescription("Updated description");
        request.setPublicationYear(2024);
        request.setGenre("Fiction");
        request.setTotalCopies(10);
        request.setAvailableCopies(8);
        request.setAuthorIds(Set.of(1L));

        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Book");
        updatedBook.setIsbn("978-0-123456-78-9");
        updatedBook.setDescription("Updated description");
        updatedBook.setPublicationYear(2024);
        updatedBook.setGenre("Fiction");
        updatedBook.setTotalCopies(10);
        updatedBook.setAvailableCopies(8);
        updatedBook.setAuthors(new HashSet<>(Set.of(testAuthor)));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        // When
        BookResponse response = bookService.updateBook(1L, request);

        // Then
        assertThat(response).isNotNull();

        ArgumentCaptor<BookUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(BookUpdatedEvent.class);
        verify(eventPublisher).publishBookUpdated(eventCaptor.capture());

        BookUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getBookId()).isEqualTo(1L);
        assertThat(capturedEvent.getEventType()).isEqualTo("BOOK_UPDATED");
        assertThat(capturedEvent.getPreviousAvailableCopies()).isEqualTo(10);
    }

    @Test
    void updateBook_shouldPublishAvailabilityChangedEventWhenCopiesChange() {
        // Given
        BookRequest request = new BookRequest();
        request.setTitle("Test Book");
        request.setIsbn("978-0-123456-78-9");
        request.setDescription("Test description");
        request.setPublicationYear(2024);
        request.setGenre("Fiction");
        request.setTotalCopies(10);
        request.setAvailableCopies(5); // Changed from 10 to 5
        request.setAuthorIds(Set.of(1L));

        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Test Book");
        updatedBook.setIsbn("978-0-123456-78-9");
        updatedBook.setDescription("Test description");
        updatedBook.setPublicationYear(2024);
        updatedBook.setGenre("Fiction");
        updatedBook.setTotalCopies(10);
        updatedBook.setAvailableCopies(5);
        updatedBook.setAuthors(new HashSet<>(Set.of(testAuthor)));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        // When
        bookService.updateBook(1L, request);

        // Then
        verify(eventPublisher).publishBookUpdated(any(BookUpdatedEvent.class));
        verify(eventPublisher).publishBookAvailabilityChanged(any(BookAvailabilityChangedEvent.class));
    }

    @Test
    void deleteBook_shouldPublishBookDeletedEvent() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(any(Book.class));

        // When
        bookService.deleteBook(1L);

        // Then
        ArgumentCaptor<BookDeletedEvent> eventCaptor = ArgumentCaptor.forClass(BookDeletedEvent.class);
        verify(eventPublisher).publishBookDeleted(eventCaptor.capture());

        BookDeletedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getBookId()).isEqualTo(1L);
        assertThat(capturedEvent.getIsbn()).isEqualTo("978-0-123456-78-9");
        assertThat(capturedEvent.getTitle()).isEqualTo("Test Book");
        assertThat(capturedEvent.getEventType()).isEqualTo("BOOK_DELETED");
    }
}
