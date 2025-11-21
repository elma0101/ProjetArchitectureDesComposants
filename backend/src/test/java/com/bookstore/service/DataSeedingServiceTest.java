package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DataSeedingServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private LoanTrackingRepository loanTrackingRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeedingService dataSeedingService;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(any(String.class))).thenReturn("encoded_password");
    }

    @Test
    void shouldSeedDataWhenDatabaseIsEmpty() throws Exception {
        // Given
        when(authorRepository.count()).thenReturn(0L);
        when(bookRepository.count()).thenReturn(0L);
        when(authorRepository.findAll()).thenReturn(createMockAuthors());

        // When
        dataSeedingService.run();

        // Then
        verify(authorRepository).saveAll(anyList());
        verify(bookRepository).saveAll(anyList());
        verify(userRepository).saveAll(anyList());
        // Note: loans, recommendations, and loan tracking are not seeded when books/loans are empty
        verify(loanRepository, never()).saveAll(anyList());
        verify(recommendationRepository, never()).saveAll(anyList());
        verify(loanTrackingRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldSkipSeedingWhenDataExists() throws Exception {
        // Given
        lenient().when(authorRepository.count()).thenReturn(5L);
        lenient().when(bookRepository.count()).thenReturn(10L);

        // When
        dataSeedingService.run();

        // Then
        verify(authorRepository, never()).saveAll(anyList());
        verify(bookRepository, never()).saveAll(anyList());
        verify(userRepository, never()).saveAll(anyList());
        verify(loanRepository, never()).saveAll(anyList());
        verify(recommendationRepository, never()).saveAll(anyList());
        verify(loanTrackingRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldSeedCorrectNumberOfEntities() throws Exception {
        // Given
        when(authorRepository.count()).thenReturn(0L);
        when(bookRepository.count()).thenReturn(0L);
        when(authorRepository.findAll()).thenReturn(createMockAuthors());

        // When
        dataSeedingService.run();

        // Then
        verify(authorRepository).saveAll(argThat(authors -> ((List<?>) authors).size() == 8));
        verify(bookRepository).saveAll(argThat(books -> ((List<?>) books).size() == 13));
        verify(userRepository).saveAll(argThat(users -> ((List<?>) users).size() == 4));
        // Note: loans, recommendations, and loan tracking are not seeded when books/loans are empty
        verify(loanRepository, never()).saveAll(anyList());
        verify(recommendationRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldEncodePasswordsForUsers() throws Exception {
        // Given
        when(authorRepository.count()).thenReturn(0L);
        when(bookRepository.count()).thenReturn(0L);
        when(authorRepository.findAll()).thenReturn(createMockAuthors());

        // When
        dataSeedingService.run();

        // Then
        verify(passwordEncoder, times(4)).encode("password123");
        verify(userRepository).saveAll(argThat(users -> 
            ((List<User>) users).stream().allMatch(user -> "encoded_password".equals(user.getPassword()))
        ));
    }

    private List<Author> createMockAuthors() {
        return Arrays.asList(
            createMockAuthor(1L, "George", "Orwell"),
            createMockAuthor(2L, "Jane", "Austen"),
            createMockAuthor(3L, "Harper", "Lee"),
            createMockAuthor(4L, "F. Scott", "Fitzgerald"),
            createMockAuthor(5L, "J.K.", "Rowling"),
            createMockAuthor(6L, "Agatha", "Christie"),
            createMockAuthor(7L, "Stephen", "King"),
            createMockAuthor(8L, "Gabriel", "García Márquez")
        );
    }

    private Author createMockAuthor(Long id, String firstName, String lastName) {
        Author author = new Author();
        author.setId(id);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        return author;
    }
}