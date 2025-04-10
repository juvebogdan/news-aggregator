package com.example.ingestion.service;

import com.example.ingestion.model.NewsApiResponse;
import com.example.ingestion.model.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class NewsIngestionService {

    private final NewsApiClient newsApiClient;
    private final ArticleMapper articleMapper;
    private final KafkaTemplate<String, NewsArticle> kafkaTemplate;
    
    @Value("${kafka.topic.news}")
    private String kafkaTopic;
    
    public NewsIngestionService(
            NewsApiClient newsApiClient,
            ArticleMapper articleMapper,
            KafkaTemplate<String, NewsArticle> kafkaTemplate) {
        this.newsApiClient = newsApiClient;
        this.articleMapper = articleMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Fetches articles from the News API for a given category and publishes them to Kafka.
     * 
     * @param category The news category to fetch (e.g., "technology", "business")
     * @return A Mono that completes when all articles have been published
     */
    public Mono<Void> fetchAndPublishArticles(String category) {
        return newsApiClient.fetchTopHeadlines(category)
                .doOnNext(response -> log.info("Fetched {} articles for category: {}", 
                        response.getArticles() != null ? response.getArticles().size() : 0, 
                        category))
                .map(apiResponse -> articleMapper.mapFromNewsApiResponse(apiResponse, category))
                .doOnNext(articles -> log.info("Mapped {} articles for publishing", articles.size()))
                .flatMap(this::publishArticlesToKafka)
                .onErrorResume(e -> {
                    log.error("Error fetching or publishing articles: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }
    
    /**
     * Publishes a list of articles to Kafka, using each article's ID as the message key.
     * 
     * @param articles The list of articles to publish
     * @return A Mono that completes when all articles have been published
     */
    private Mono<Void> publishArticlesToKafka(List<NewsArticle> articles) {
        articles.forEach(article -> {
            log.debug("Publishing article: {} - {}", article.getId(), article.getTitle());
            kafkaTemplate.send(kafkaTopic, article.getId(), article);
        });
        
        return Mono.empty(); // Return an empty Mono to indicate completion
    }
}
