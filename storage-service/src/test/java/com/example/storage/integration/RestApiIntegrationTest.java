package com.example.storage.integration;

import com.example.storage.model.NewsArticleEntity;
import com.example.storage.repository.NewsArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the REST API of the storage service.
 * This test verifies that our REST endpoints correctly interact
 * with the database and return expected responses.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integration.properties")
@Transactional
public class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsArticleRepository repository;

    @Test
    public void testGetArticleById() throws Exception {
        // Create and save a test article
        String id = UUID.randomUUID().toString();
        NewsArticleEntity article = createTestArticle(id);
        repository.save(article);

        // Test the GET endpoint
        mockMvc.perform(get("/api/articles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.title", is("Integration Test Article")))
                .andExpect(jsonPath("$.category", is("integration-test")));
    }

    @Test
    public void testGetArticleByIdNotFound() throws Exception {
        // Test with a non-existent ID
        String nonExistentId = UUID.randomUUID().toString();
        
        mockMvc.perform(get("/api/articles/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllArticles() throws Exception {
        // Create and save multiple test articles
        for (int i = 0; i < 5; i++) {
            String id = UUID.randomUUID().toString();
            NewsArticleEntity article = createTestArticle(id);
            article.setTitle("Test Article " + i);
            repository.save(article);
        }
    
        // Test the GET all endpoint
        mockMvc.perform(get("/api/articles")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(5)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }
    
    @Test
    public void testGetArticlesByCategory() throws Exception {
        // Create and save articles with different categories
        String id1 = UUID.randomUUID().toString();
        NewsArticleEntity techArticle = createTestArticle(id1);
        techArticle.setCategory("technology");
        
        String id2 = UUID.randomUUID().toString();
        NewsArticleEntity businessArticle = createTestArticle(id2);
        businessArticle.setCategory("business");
        
        repository.save(techArticle);
        repository.save(businessArticle);
    
        // Test the category endpoint
        mockMvc.perform(get("/api/articles/category/{category}", "technology")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].category", is("technology")));
    }
    
    @Test
    public void testSearchArticles() throws Exception {
        // Create and save article with specific text
        String id = UUID.randomUUID().toString();
        NewsArticleEntity article = createTestArticle(id);
        article.setTitle("Special Unique Test Title");
        article.setDescription("This is a unique description for testing search");
        repository.save(article);
    
        // Test the search endpoint
        mockMvc.perform(get("/api/articles/search")
                .param("query", "unique")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].title", is("Special Unique Test Title")));
    }

    private NewsArticleEntity createTestArticle(String id) {
        NewsArticleEntity article = new NewsArticleEntity();
        article.setId(id);
        article.setTitle("Integration Test Article");
        article.setDescription("Description for integration test");
        article.setContent("Content for integration test");
        article.setAuthor("Integration Tester");
        article.setSourceId("test-source");
        article.setSourceName("Test Source");
        article.setUrl("https://example.com/" + id);
        article.setImageUrl("https://example.com/images/" + id);
        article.setPublishedAt(LocalDateTime.now());
        article.setFetchedAt(LocalDateTime.now());
        article.setCategory("integration-test");
        return article;
    }
}
