package com.example.ingestion.service;

import com.example.ingestion.model.NewsApiResponse;
import com.example.ingestion.model.NewsArticle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsIngestionServiceTest {

    @Mock
    private NewsApiClient newsApiClient;
    
    @Mock
    private ArticleMapper articleMapper;
    
    @Mock
    private KafkaTemplate<String, NewsArticle> kafkaTemplate;
    
    private NewsIngestionService newsIngestionService;
    
    @BeforeEach
    void setUp() {
        newsIngestionService = new NewsIngestionService(newsApiClient, articleMapper, kafkaTemplate);
        // Set the Kafka topic via reflection since it would normally be injected from properties
        ReflectionTestUtils.setField(newsIngestionService, "kafkaTopic", "news.incoming");
    }
    
    @Test
    void shouldFetchAndPublishArticles() {
        // Arrange
        String category = "technology";
        
        // Create a mock response from the news API
        NewsApiResponse apiResponse = new NewsApiResponse();
        NewsApiResponse.Article article1 = new NewsApiResponse.Article();
        article1.setTitle("Test Article 1");
        NewsApiResponse.Article article2 = new NewsApiResponse.Article();
        article2.setTitle("Test Article 2");
        apiResponse.setArticles(Arrays.asList(article1, article2));
        
        // Create mock mapped articles
        NewsArticle mappedArticle1 = new NewsArticle();
        mappedArticle1.setId("1");
        mappedArticle1.setTitle("Test Article 1");
        
        NewsArticle mappedArticle2 = new NewsArticle();
        mappedArticle2.setId("2");
        mappedArticle2.setTitle("Test Article 2");
        
        List<NewsArticle> mappedArticles = Arrays.asList(mappedArticle1, mappedArticle2);
        
        // Setup mock behavior
        when(newsApiClient.fetchTopHeadlines(category)).thenReturn(Mono.just(apiResponse));
        when(articleMapper.mapFromNewsApiResponse(apiResponse, category)).thenReturn(mappedArticles);
        
        // Act
        newsIngestionService.fetchAndPublishArticles(category).block(); // Block to make the test synchronous
        
        // Assert
        verify(newsApiClient).fetchTopHeadlines(category);
        verify(articleMapper).mapFromNewsApiResponse(apiResponse, category);
        
        // Verify each article was published to Kafka
        verify(kafkaTemplate).send(eq("news.incoming"), eq(mappedArticle1.getId()), eq(mappedArticle1));
        verify(kafkaTemplate).send(eq("news.incoming"), eq(mappedArticle2.getId()), eq(mappedArticle2));
    }
    
    @Test
    void shouldHandleEmptyArticlesList() {
        // Arrange
        String category = "business";
        
        // Create a mock response with no articles
        NewsApiResponse apiResponse = new NewsApiResponse();
        apiResponse.setArticles(new ArrayList<>());
        
        // Setup mock behavior
        when(newsApiClient.fetchTopHeadlines(category)).thenReturn(Mono.just(apiResponse));
        when(articleMapper.mapFromNewsApiResponse(apiResponse, category)).thenReturn(new ArrayList<>());
        
        // Act
        newsIngestionService.fetchAndPublishArticles(category).block();
        
        // Assert
        verify(newsApiClient).fetchTopHeadlines(category);
        verify(articleMapper).mapFromNewsApiResponse(apiResponse, category);
        
        // Verify no articles were published to Kafka
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(NewsArticle.class));
    }
    
    @Test
    void shouldHandleErrorFromNewsApi() {
        // Arrange
        String category = "health";
        RuntimeException testException = new RuntimeException("API Error");
        
        // Setup mock behavior to throw an exception
        when(newsApiClient.fetchTopHeadlines(category)).thenReturn(Mono.error(testException));
        
        // Act & Assert
        // The service should handle the error and return an empty Mono
        newsIngestionService.fetchAndPublishArticles(category).block();
        
        // Verify the client was called but no articles were processed
        verify(newsApiClient).fetchTopHeadlines(category);
        verify(articleMapper, never()).mapFromNewsApiResponse(any(), anyString());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(NewsArticle.class));
    }
}
