package com.example.storage.integration;

import com.example.storage.model.NewsArticleEntity;
import com.example.storage.repository.NewsArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for database operations.
 * This test verifies that our JPA entities and repositories 
 * correctly interact with the PostgreSQL database.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-integration.properties")
@Transactional  // This ensures test operations are rolled back after each test
public class DatabaseIntegrationTest {

    @Autowired
    private NewsArticleRepository repository;

    @Test
    public void testSaveAndRetrieveArticle() {
        // Create a test article
        String id = UUID.randomUUID().toString();
        NewsArticleEntity article = createTestArticle(id);
        
        // Save to the database
        repository.save(article);
        
        // Retrieve it from the database
        Optional<NewsArticleEntity> retrievedArticle = repository.findById(id);
        
        // Verify
        assertThat(retrievedArticle).isPresent();
        assertThat(retrievedArticle.get().getTitle()).isEqualTo("Integration Test Article");
        assertThat(retrievedArticle.get().getCategory()).isEqualTo("integration-test");
    }

    @Test
    public void testFindByCategory() {
        // Create and save several test articles with different categories
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        
        NewsArticleEntity techArticle1 = createTestArticle(id1);
        techArticle1.setCategory("technology");
        
        NewsArticleEntity techArticle2 = createTestArticle(id2);
        techArticle2.setCategory("technology");
        
        NewsArticleEntity businessArticle = createTestArticle(id3);
        businessArticle.setCategory("business");
        
        repository.save(techArticle1);
        repository.save(techArticle2);
        repository.save(businessArticle);
        
        // Test finding by category
        List<NewsArticleEntity> techArticles = repository.findByCategory("technology");
        
        // Verify
        assertThat(techArticles).hasSize(2);
        assertThat(techArticles.stream().map(NewsArticleEntity::getCategory).distinct().toList())
            .containsExactly("technology");
    }
    
    @Test
    public void testFindByPublishedAtAfter() {
        // Create test articles with different publication dates
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        
        NewsArticleEntity recentArticle = createTestArticle(id1);
        recentArticle.setPublishedAt(LocalDateTime.now());
        
        NewsArticleEntity oldArticle = createTestArticle(id2);
        oldArticle.setPublishedAt(LocalDateTime.now().minusDays(7));
        
        repository.save(recentArticle);
        repository.save(oldArticle);
        
        // Search for articles published after 3 days ago
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<NewsArticleEntity> recentArticles = repository.findByPublishedAtAfter(threeDaysAgo);
        
        // Verify we only get the recent article
        assertThat(recentArticles).hasSize(1);
        assertThat(recentArticles.get(0).getId()).isEqualTo(id1);
    }
    
    @Test
    public void testTextSearch() {
        // Create articles with specific text content
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        
        NewsArticleEntity article1 = createTestArticle(id1);
        article1.setTitle("Special Climate Change Article");
        article1.setDescription("This article discusses climate impacts");
        
        NewsArticleEntity article2 = createTestArticle(id2);
        article2.setTitle("Technology News");
        article2.setDescription("Latest advancements in AI");
        
        repository.save(article1);
        repository.save(article2);
        
        // Search for articles with "climate" in title or description
        List<NewsArticleEntity> climateArticles = repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "climate", "climate");
        
        // Verify
        assertThat(climateArticles).hasSize(1);
        assertThat(climateArticles.get(0).getId()).isEqualTo(id1);
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
