package com.example.storage.repository;

import com.example.storage.model.NewsArticleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NewsArticleRepositoryTest {

    @Autowired
    private NewsArticleRepository repository;

    @Test
    public void testSaveAndFindById() {
        // Given
        String id = UUID.randomUUID().toString();
        NewsArticleEntity article = createArticle(id, "Test Title", "technology");
        
        // When
        repository.save(article);
        
        // Then
        NewsArticleEntity found = repository.findById(id).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Test Title");
        assertThat(found.getCategory()).isEqualTo("technology");
    }

    @Test
    public void testFindByCategory() {
        // Given
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        
        repository.save(createArticle(id1, "Tech News 1", "technology"));
        repository.save(createArticle(id2, "Business News", "business"));
        
        // When
        List<NewsArticleEntity> techArticles = repository.findByCategory("technology");
        
        // Then
        assertThat(techArticles).hasSize(1);
        assertThat(techArticles.get(0).getTitle()).isEqualTo("Tech News 1");
    }

    @Test
    public void testFindByCategoryWithPagination() {
        // Given
        for (int i = 0; i < 15; i++) {
            repository.save(createArticle(
                    UUID.randomUUID().toString(),
                    "Tech News " + i,
                    "technology"
            ));
        }
        
        // When - get first page (10 items)
        Page<NewsArticleEntity> firstPage = repository.findByCategory(
                "technology", 
                PageRequest.of(0, 10)
        );
        
        // Then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    public void testFindByPublishedAtAfter() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        
        NewsArticleEntity recentArticle = createArticle(
                UUID.randomUUID().toString(),
                "Recent News",
                "general"
        );
        recentArticle.setPublishedAt(LocalDateTime.now());
        
        NewsArticleEntity oldArticle = createArticle(
                UUID.randomUUID().toString(),
                "Old News",
                "general"
        );
        oldArticle.setPublishedAt(twoDaysAgo);
        
        repository.save(recentArticle);
        repository.save(oldArticle);
        
        // When
        List<NewsArticleEntity> recentArticles = repository.findByPublishedAtAfter(yesterday);
        
        // Then
        assertThat(recentArticles).hasSize(1);
        assertThat(recentArticles.get(0).getTitle()).isEqualTo("Recent News");
    }

    @Test
    public void testFindByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase() {
        // Given
        NewsArticleEntity article1 = createArticle(
                UUID.randomUUID().toString(),
                "Climate Change Impact",
                "science"
        );
        article1.setDescription("Research on global warming effects");
        
        NewsArticleEntity article2 = createArticle(
                UUID.randomUUID().toString(),
                "Tech News",
                "technology"
        );
        article2.setDescription("New study on climate patterns");
        
        repository.save(article1);
        repository.save(article2);
        
        // When
        List<NewsArticleEntity> climateArticles = repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "climate", "climate"
        );
        
        // Then
        assertThat(climateArticles).hasSize(2);
    }

    private NewsArticleEntity createArticle(String id, String title, String category) {
        NewsArticleEntity article = new NewsArticleEntity();
        article.setId(id);
        article.setTitle(title);
        article.setDescription("Description for " + title);
        article.setContent("Content for " + title);
        article.setAuthor("Test Author");
        article.setSourceId("test-source");
        article.setSourceName("Test Source");
        article.setUrl("https://example.com/" + id);
        article.setImageUrl("https://example.com/images/" + id);
        article.setPublishedAt(LocalDateTime.now());
        article.setFetchedAt(LocalDateTime.now());
        article.setCategory(category);
        return article;
    }
}
