package com.example.storage.kafka;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewsArticleConsumer {

    private final NewsArticleService articleService;

    /**
     * Listens to the news topic and processes incoming articles
     */
    @KafkaListener(topics = "${kafka.topic.news}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(NewsArticleDto article) {
        log.info("Received article from Kafka: {}", article.getTitle());
        
        try {
            articleService.saveArticle(article);
            log.info("Successfully saved article with ID: {}", article.getId());
        } catch (Exception e) {
            log.error("Error saving article: {}", e.getMessage(), e);
        }
    }
}
