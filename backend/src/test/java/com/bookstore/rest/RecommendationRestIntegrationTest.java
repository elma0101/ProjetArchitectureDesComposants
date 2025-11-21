package com.bookstore.rest;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Recommendation;
import com.bookstore.entity.RecommendationType;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RecommendationRestIntegrationTest extends BaseRestIntegrationTest {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Recommendation testRecommendation;
    private Book testBook;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test author
        testAuthor = new Author("Test", "Author");
        testAuthor = authorRepository.save(testAuthor);

        // Create test book
        testBook = new Book("Test Book", "978-0-123456-78-9");
        testBook.setGenre("Fiction");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook.addAuthor(testAuthor);
        testBook = bookRepository.save(testBook);

        // Create test recommendation
        testRecommendation = new Recommendation("USER001", testBook, RecommendationType.COLLABORATIVE, 0.85);
        testRecommendation.setReason("Based on similar reading preferences");
        testRecommendation = recommendationRepository.save(testRecommendation);
    }

    @Test
    void shouldGetAllRecommendations() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.recommendations[0].userId", is("USER001")))
                .andExpect(jsonPath("$._embedded.recommendations[0].type", is("COLLABORATIVE")))
                .andExpect(jsonPath("$._embedded.recommendations[0].score", is(0.85)))
                .andExpect(jsonPath("$.page.size", is(20)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

    @Test
    void shouldGetRecommendationById() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/{id}", testRecommendation.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is("USER001")))
                .andExpect(jsonPath("$.type", is("COLLABORATIVE")))
                .andExpect(jsonPath("$.score", is(0.85)))
                .andExpect(jsonPath("$.reason", is("Based on similar reading preferences")));
    }

    @Test
    void shouldCreateNewRecommendation() throws Exception {
        // Create another book for the new recommendation
        Book anotherBook = new Book("Another Book", "978-0-987654-32-1");
        anotherBook.setGenre("Science Fiction");
        anotherBook.setTotalCopies(3);
        anotherBook.setAvailableCopies(3);
        anotherBook = bookRepository.save(anotherBook);

        Recommendation newRecommendation = new Recommendation();
        newRecommendation.setUserId("USER002");
        newRecommendation.setBook(anotherBook);
        newRecommendation.setType(RecommendationType.CONTENT_BASED);
        newRecommendation.setScore(0.75);
        newRecommendation.setReason("Based on genre preferences");

        mockMvc.perform(post(RECOMMENDATIONS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRecommendation)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is("USER002")))
                .andExpect(jsonPath("$.type", is("CONTENT_BASED")))
                .andExpect(jsonPath("$.score", is(0.75)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldUpdateExistingRecommendation() throws Exception {
        testRecommendation.setScore(0.90);
        testRecommendation.setReason("Updated recommendation reason");

        mockMvc.perform(put(RECOMMENDATIONS_PATH + "/{id}", testRecommendation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecommendation)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.score", is(0.90)))
                .andExpect(jsonPath("$.reason", is("Updated recommendation reason")))
                .andExpect(jsonPath("$.userId", is("USER001")));
    }

    @Test
    void shouldPartiallyUpdateRecommendation() throws Exception {
        String partialUpdate = "{\"score\":0.95}";

        mockMvc.perform(patch(RECOMMENDATIONS_PATH + "/{id}", testRecommendation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialUpdate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.score", is(0.95)))
                .andExpect(jsonPath("$.userId", is("USER001")))
                .andExpect(jsonPath("$.type", is("COLLABORATIVE")));
    }

    @Test
    void shouldSearchRecommendationsByUserId() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/search/findByUserIdOrderByScoreDesc")
                .param("userId", "USER001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.recommendations[0].userId", is("USER001")));
    }

    @Test
    void shouldSearchRecommendationsByType() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/search/findByTypeOrderByScoreDesc")
                .param("type", "COLLABORATIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.recommendations[0].type", is("COLLABORATIVE")));
    }

    @Test
    void shouldSearchRecommendationsByUserIdAndType() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/search/findByUserIdAndTypeOrderByScoreDesc")
                .param("userId", "USER001")
                .param("type", "COLLABORATIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.recommendations[0].userId", is("USER001")))
                .andExpect(jsonPath("$._embedded.recommendations[0].type", is("COLLABORATIVE")));
    }

    @Test
    void shouldSearchRecommendationsByBookId() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/search/findByBookId")
                .param("bookId", testBook.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(1)));
    }

    @Test
    void shouldFindHighScoreRecommendations() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/search/findHighScoreRecommendations")
                .param("threshold", "0.8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.recommendations[0].score", greaterThanOrEqualTo(0.8)));
    }

    @Test
    void shouldSearchRecommendationsByBookGenre() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/search/findByBookGenre")
                .param("genre", "Fiction"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(1)));
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Create additional recommendations for pagination test
        for (int i = 1; i <= 25; i++) {
            Book book = new Book("Book " + i, "978-0-123456-" + String.format("%02d", i) + "-0");
            book.setGenre("Test Genre");
            book.setTotalCopies(1);
            book.setAvailableCopies(1);
            book = bookRepository.save(book);

            Recommendation recommendation = new Recommendation("USER" + i, book, 
                                                             RecommendationType.POPULAR, 0.5 + (i * 0.01));
            recommendationRepository.save(recommendation);
        }

        mockMvc.perform(get(RECOMMENDATIONS_PATH)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.recommendations", hasSize(10)))
                .andExpect(jsonPath("$.page.size", is(10)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.totalPages", greaterThan(1)));
    }

    @Test
    void shouldHandleSorting() throws Exception {
        // Create additional recommendation with different score for sorting
        Book anotherBook = new Book("Another Book", "978-0-111111-11-1");
        anotherBook.setGenre("Fiction");
        anotherBook.setTotalCopies(1);
        anotherBook.setAvailableCopies(1);
        anotherBook = bookRepository.save(anotherBook);

        Recommendation anotherRecommendation = new Recommendation("USER001", anotherBook, 
                                                                 RecommendationType.COLLABORATIVE, 0.95);
        recommendationRepository.save(anotherRecommendation);

        mockMvc.perform(get(RECOMMENDATIONS_PATH)
                .param("sort", "score,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.recommendations[0].score", is(0.95)))
                .andExpect(jsonPath("$._embedded.recommendations[1].score", is(0.85)));
    }

    @Test
    void shouldReturnNotFoundForNonExistentRecommendation() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        Recommendation invalidRecommendation = new Recommendation();
        // Missing required fields: book and type

        mockMvc.perform(post(RECOMMENDATIONS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRecommendation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThan(0))));
    }

    @Test
    void shouldValidateScoreRange() throws Exception {
        Recommendation invalidRecommendation = new Recommendation();
        invalidRecommendation.setBook(testBook);
        invalidRecommendation.setType(RecommendationType.COLLABORATIVE);
        invalidRecommendation.setScore(1.5); // Invalid score > 1.0

        mockMvc.perform(post(RECOMMENDATIONS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRecommendation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'score')].message", 
                    hasItem(containsString("1.0"))));
    }

    @Test
    void shouldPreventDeleteOnIndividualRecommendations() throws Exception {
        // Based on our configuration, DELETE should be disabled for individual recommendations
        mockMvc.perform(delete(RECOMMENDATIONS_PATH + "/{id}", testRecommendation.getId()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldIncludeHATEOASLinks() throws Exception {
        mockMvc.perform(get(RECOMMENDATIONS_PATH + "/{id}", testRecommendation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.recommendation.href", notNullValue()))
                .andExpect(jsonPath("$._links.book.href", notNullValue()));
    }
}