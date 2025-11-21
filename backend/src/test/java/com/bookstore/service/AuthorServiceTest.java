package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.exception.AuthorNotFoundException;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {
    
    @Mock
    private AuthorRepository authorRepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @InjectMocks
    private AuthorService authorService;
    
    private Author testAuthor;
    private Book testBook;
    private AuthorCreateRequest createRequest;
    private AuthorUpdateRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("Robert");
        testAuthor.setLastName("Martin");
        testAuthor.setBiography("Software engineer and author");
        testAuthor.setBirthDate(LocalDate.of(1952, 12, 5));
        testAuthor.setNationality("American");
        
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Clean Code");
        testBook.setIsbn("978-0132350884");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);
        
        createRequest = new AuthorCreateRequest();
        createRequest.setFirstName("Robert");
        createRequest.setLastName("Martin");
        createRequest.setBiography("Software engineer and author");
        createRequest.setBirthDate(LocalDate.of(1952, 12, 5));
        createRequest.setNationality("American");
        
        updateRequest = new AuthorUpdateRequest();
        updateRequest.setBiography("Updated biography");
        updateRequest.setNationality("Canadian");
    }
    
    @Test
    void createAuthor_Success() {
        // Given
        when(authorRepository.existsByFullName("Robert", "Martin")).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        
        // When
        Author result = authorService.createAuthor(createRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("Robert", result.getFirstName());
        assertEquals("Martin", result.getLastName());
        verify(authorRepository).existsByFullName("Robert", "Martin");
        verify(authorRepository).save(any(Author.class));
    }
    
    @Test
    void createAuthor_DuplicateAuthor_ThrowsException() {
        // Given
        when(authorRepository.existsByFullName("Robert", "Martin")).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            authorService.createAuthor(createRequest);
        });
        
        verify(authorRepository).existsByFullName("Robert", "Martin");
        verify(authorRepository, never()).save(any(Author.class));
    }
    
    @Test
    void updateAuthor_Success() {
        // Given
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        
        // When
        Author result = authorService.updateAuthor(1L, updateRequest);
        
        // Then
        assertNotNull(result);
        verify(authorRepository).findById(1L);
        verify(authorRepository).save(testAuthor);
    }
    
    @Test
    void updateAuthor_AuthorNotFound_ThrowsException() {
        // Given
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(AuthorNotFoundException.class, () -> {
            authorService.updateAuthor(1L, updateRequest);
        });
        
        verify(authorRepository).findById(1L);
        verify(authorRepository, never()).save(any(Author.class));
    }
    
    @Test
    void getAuthorById_Success() {
        // Given
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        
        // When
        Author result = authorService.getAuthorById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(authorRepository).findById(1L);
    }
    
    @Test
    void getAuthorById_NotFound_ThrowsException() {
        // Given
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(AuthorNotFoundException.class, () -> {
            authorService.getAuthorById(1L);
        });
        
        verify(authorRepository).findById(1L);
    }
    
    @Test
    void getAllAuthors_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findAll(pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.getAllAuthors(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findAll(pageable);
    }
    
    @Test
    void deleteAuthor_Success() {
        // Given
        testAuthor.addBook(testBook);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        
        // When
        authorService.deleteAuthor(1L);
        
        // Then
        verify(authorRepository).findById(1L);
        verify(authorRepository).delete(testAuthor);
        assertFalse(testBook.getAuthors().contains(testAuthor));
    }
    
    @Test
    void addBooksToAuthor_Success() {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(1L));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
        
        // When
        Author result = authorService.addBooksToAuthor(1L, request);
        
        // Then
        assertNotNull(result);
        verify(authorRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(authorRepository).save(testAuthor);
        assertTrue(testAuthor.getBooks().contains(testBook));
        assertTrue(testBook.getAuthors().contains(testAuthor));
    }
    
    @Test
    void addBooksToAuthor_BookNotFound_ThrowsException() {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(1L));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BookNotFoundException.class, () -> {
            authorService.addBooksToAuthor(1L, request);
        });
        
        verify(authorRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(authorRepository, never()).save(any(Author.class));
    }
    
    @Test
    void removeBooksFromAuthor_Success() {
        // Given
        testAuthor.addBook(testBook);
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(1L));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
        
        // When
        Author result = authorService.removeBooksFromAuthor(1L, request);
        
        // Then
        assertNotNull(result);
        verify(authorRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(authorRepository).save(testAuthor);
        assertFalse(testAuthor.getBooks().contains(testBook));
        assertFalse(testBook.getAuthors().contains(testAuthor));
    }
    
    @Test
    void setBooksForAuthor_Success() {
        // Given
        Book anotherBook = new Book();
        anotherBook.setId(2L);
        anotherBook.setTitle("Clean Architecture");
        anotherBook.setIsbn("978-0134494166");
        
        testAuthor.addBook(testBook);
        
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(2L));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(anotherBook));
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
        
        // When
        Author result = authorService.setBooksForAuthor(1L, request);
        
        // Then
        assertNotNull(result);
        verify(authorRepository).findById(1L);
        verify(bookRepository).findById(2L);
        verify(authorRepository).save(testAuthor);
        assertFalse(testAuthor.getBooks().contains(testBook));
        assertTrue(testAuthor.getBooks().contains(anotherBook));
    }
    
    @Test
    void getBooksByAuthor_Success() {
        // Given
        testAuthor.addBook(testBook);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        
        // When
        Set<Book> result = authorService.getBooksByAuthor(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testBook));
        verify(authorRepository).findById(1L);
    }
    
    @Test
    void searchAuthors_WithFullName_Success() {
        // Given
        AuthorSearchRequest request = new AuthorSearchRequest();
        request.setFullName("Robert Martin");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findByFullNameContainingIgnoreCase("Robert Martin", pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.searchAuthors(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findByFullNameContainingIgnoreCase("Robert Martin", pageable);
    }
    
    @Test
    void searchAuthors_WithGenre_Success() {
        // Given
        AuthorSearchRequest request = new AuthorSearchRequest();
        request.setGenre("Programming");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findByBookGenre("Programming", pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.searchAuthors(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findByBookGenre("Programming", pageable);
    }
    
    @Test
    void searchAuthors_WithHasBooks_Success() {
        // Given
        AuthorSearchRequest request = new AuthorSearchRequest();
        request.setHasBooks(true);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findAuthorsWithBooks(pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.searchAuthors(request, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findAuthorsWithBooks(pageable);
    }
    
    @Test
    void getAuthorStatistics_Success() {
        // Given
        testAuthor.addBook(testBook);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        
        // When
        AuthorStatistics result = authorService.getAuthorStatistics(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getAuthorId());
        assertEquals("Robert Martin", result.getFullName());
        assertEquals(1L, result.getTotalBooks());
        assertEquals(5L, result.getTotalCopies());
        assertEquals(3L, result.getAvailableCopies());
        verify(authorRepository).findById(1L);
    }
    
    @Test
    void getMostProlificAuthors_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findMostProlificAuthors(pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.getMostProlificAuthors(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findMostProlificAuthors(pageable);
    }
    
    @Test
    void getRecentlyAddedAuthors_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findRecentlyAddedAuthors(pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.getRecentlyAddedAuthors(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findRecentlyAddedAuthors(pageable);
    }
    
    @Test
    void findAuthorsByNationality_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findByNationalityContainingIgnoreCase("American", pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.findAuthorsByNationality("American", pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findByNationalityContainingIgnoreCase("American", pageable);
    }
    
    @Test
    void findAuthorsByBirthYear_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor));
        when(authorRepository.findByBirthYear(1952, pageable)).thenReturn(authorPage);
        
        // When
        Page<Author> result = authorService.findAuthorsByBirthYear(1952, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(authorRepository).findByBirthYear(1952, pageable);
    }
}