package com.example.storage.kafka;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.service.NewsArticleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsArticleConsumerTest {

    @Mock
    private NewsArticleService articleService;

    @InjectMocks
    private NewsArticleConsumer consumer;

    @Test
    void testConsume() {
        // Given
        NewsArticleDto article = new NewsArticleDto();
        article.setId(UUID.randomUUID().toString());
        article.setTitle("Test Article");
        article.setDescription("Test Description");
        article.setPublishedAt(LocalDateTime.now());
        
        when(articleService.saveArticle(any(NewsArticleDto.class))).thenReturn(article);
        
        // When
        consumer.consume(article);
        
        // Then
        verify(articleService, times(1)).saveArticle(article);
    }

    @Test
    void testConsumeHandlesException() {
        // Given
        NewsArticleDto article = new NewsArticleDto();
        article.setId(UUID.randomUUID().toString());
        
        doThrow(new RuntimeException("Test exception"))
                .when(articleService).saveArticle(any(NewsArticleDto.class));
        
        // When - this should not throw exception outside
        consumer.consume(article);
        
        // Then
        verify(articleService, times(1)).saveArticle(article);
        // No assertion needed - we're just verifying it doesn't throw
    }
}
