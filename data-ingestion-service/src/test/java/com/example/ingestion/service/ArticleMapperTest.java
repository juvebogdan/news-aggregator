package com.example.ingestion.service;

import com.example.ingestion.model.NewsApiResponse;
import com.example.ingestion.model.NewsArticle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleMapperTest {

    private ArticleMapper articleMapper;
    private NewsApiResponse testResponse;
    
    @BeforeEach
    void setUp() {
        // Initialize the mapper
        articleMapper = new ArticleMapper();
        
        // Create a test NewsApiResponse
        testResponse = new NewsApiResponse();
        testResponse.setStatus("ok");
        testResponse.setTotalResults(1);
        
        // Create a test article
        NewsApiResponse.Article article = new NewsApiResponse.Article();
        article.setTitle("Test Article Title");
        article.setDescription("Test article description");
        article.setContent("This is test content for the article");
        article.setAuthor("Test Author");
        article.setUrl("https://example.com/test-article");
        article.setUrlToImage("https://example.com/test-image.jpg");
        article.setPublishedAt("2023-04-09T10:30:00Z");
        
        // Create a test source
        NewsApiResponse.Source source = new NewsApiResponse.Source();
        source.setId("test-source-id");
        source.setName("Test Source");
        article.setSource(source);
        
        // Add the article to the response
        testResponse.setArticles(List.of(article));
    }
    
    @Test
    void shouldMapNewsApiResponseToNewsArticles() {
        // Arrange - we already set up our test data in @BeforeEach
        String testCategory = "technology";
        
        // Act
        List<NewsArticle> result = articleMapper.mapFromNewsApiResponse(testResponse, testCategory);
        
        // Assert
        assertEquals(1, result.size(), "Should map exactly one article");
        
        NewsArticle mappedArticle = result.get(0);
        
        // Verify all fields are mapped correctly
        assertEquals("Test Article Title", mappedArticle.getTitle());
        assertEquals("Test article description", mappedArticle.getDescription());
        assertEquals("This is test content for the article", mappedArticle.getContent());
        assertEquals("Test Author", mappedArticle.getAuthor());
        assertEquals("https://example.com/test-article", mappedArticle.getUrl());
        assertEquals("https://example.com/test-image.jpg", mappedArticle.getImageUrl());
        
        // Verify source mapping
        assertEquals("test-source-id", mappedArticle.getSourceId());
        assertEquals("Test Source", mappedArticle.getSourceName());
        
        // Verify date parsing
        assertNotNull(mappedArticle.getPublishedAt(), "Published date should be parsed");
        
        // Verify category is set
        assertEquals("technology", mappedArticle.getCategory());
        
        // Verify ID is generated
        assertNotNull(mappedArticle.getId(), "Article ID should be generated");
        
        // Verify fetchedAt timestamp is set
        assertNotNull(mappedArticle.getFetchedAt(), "Fetched timestamp should be set");
    }
    
    @Test
    void shouldHandleNullValues() {
        // Arrange - create a response with null values
        NewsApiResponse nullResponse = new NewsApiResponse();
        nullResponse.setStatus("ok");
        nullResponse.setTotalResults(1);
        
        NewsApiResponse.Article articleWithNulls = new NewsApiResponse.Article();
        // Only set title, leave other fields null
        articleWithNulls.setTitle("Article With Nulls");
        
        nullResponse.setArticles(List.of(articleWithNulls));
        
        // Act
        List<NewsArticle> result = articleMapper.mapFromNewsApiResponse(nullResponse, "technology");
        
        // Assert
        assertEquals(1, result.size(), "Should map article even with null fields");
        
        NewsArticle mappedArticle = result.get(0);
        assertEquals("Article With Nulls", mappedArticle.getTitle());
        
        // These should be null but not cause exceptions
        assertNull(mappedArticle.getDescription());
        assertNull(mappedArticle.getContent());
        assertNull(mappedArticle.getAuthor());
        assertNull(mappedArticle.getSourceId());
        assertNull(mappedArticle.getSourceName());
    }
    
    @Test
    void shouldHandleInvalidDateFormat() {
        // Arrange
        NewsApiResponse badDateResponse = new NewsApiResponse();
        badDateResponse.setStatus("ok");
        badDateResponse.setTotalResults(1);
        
        NewsApiResponse.Article articleWithBadDate = new NewsApiResponse.Article();
        articleWithBadDate.setTitle("Article With Bad Date");
        articleWithBadDate.setPublishedAt("not-a-date-format");
        
        badDateResponse.setArticles(List.of(articleWithBadDate));
        
        // Act - This should not throw an exception despite the bad date format
        List<NewsArticle> result = articleMapper.mapFromNewsApiResponse(badDateResponse, "technology");
        
        // Assert
        assertEquals(1, result.size(), "Should handle invalid date format gracefully");
        
        NewsArticle mappedArticle = result.get(0);
        assertNotNull(mappedArticle.getPublishedAt(), "Should set a fallback date when parsing fails");
    }
}
