package com.example.ingestion.scheduler;

import com.example.ingestion.service.NewsIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@EnableScheduling
@Slf4j
public class NewsIngestionScheduler {

    private final NewsIngestionService newsIngestionService;
    private final List<String> categories;

    public NewsIngestionScheduler(
            NewsIngestionService newsIngestionService,
            @Value("${newsapi.categories:technology}") String categoriesConfig) {
        this.newsIngestionService = newsIngestionService;
        this.categories = Arrays.asList(categoriesConfig.split(","));
        log.info("Configured news categories for ingestion: {}", this.categories);
    }

    /**
     * Periodically fetches news for all configured categories.
     * The fixedRateString is configured in milliseconds via application properties.
     */
    @Scheduled(fixedRateString = "${newsapi.fetch.interval:300000}")
    public void fetchNewsForAllCategories() {
        log.info("Starting scheduled news ingestion at {}", LocalDateTime.now());
        
        Flux.fromIterable(categories)
            .flatMap(category -> {
                log.info("Fetching news for category: {}", category);
                return newsIngestionService.fetchAndPublishArticles(category)
                    .doOnSuccess(v -> log.info("Completed ingestion for category: {}", category))
                    .onErrorResume(e -> {
                        log.error("Error ingesting category {}: {}", category, e.getMessage());
                        return Mono.empty();
                    });
            })
            .doOnComplete(() -> log.info("Completed news ingestion cycle at {}", LocalDateTime.now()))
            .subscribe();
    }
}
