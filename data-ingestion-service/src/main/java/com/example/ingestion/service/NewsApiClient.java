package com.example.ingestion.service;

import com.example.ingestion.model.NewsApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class NewsApiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String defaultCategory;

    public NewsApiClient(
            @Value("${newsapi.url}") String apiUrl,
            @Value("${newsapi.key}") String apiKey,
            @Value("${newsapi.default.category}") String defaultCategory) {
        
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
        this.apiKey = apiKey;
        this.defaultCategory = defaultCategory;
    }

    public Mono<NewsApiResponse> fetchTopHeadlines(String category) {
        String actualCategory = category != null ? category : defaultCategory;
        
        log.info("Fetching news articles for category: {}", actualCategory);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/top-headlines")
                        .queryParam("category", actualCategory)
                        .queryParam("language", "en")
                        // Add this line:
                        .queryParam("country", "us")  // The News API requires a country parameter
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(NewsApiResponse.class)
                .doOnError(e -> log.error("Error fetching news: {}", e.getMessage()));
    }
}
