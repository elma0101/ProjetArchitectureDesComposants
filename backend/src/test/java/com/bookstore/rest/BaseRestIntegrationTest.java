package com.bookstore.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseRestIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final String API_BASE_PATH = "/api";
    protected static final String BOOKS_PATH = API_BASE_PATH + "/books";
    protected static final String AUTHORS_PATH = API_BASE_PATH + "/authors";
    protected static final String LOANS_PATH = API_BASE_PATH + "/loans";
    protected static final String RECOMMENDATIONS_PATH = API_BASE_PATH + "/recommendations";
}