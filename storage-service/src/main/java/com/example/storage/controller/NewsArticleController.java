package com.example.storage.controller;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class NewsArticleController {

    private final NewsArticleService articleService;

    /**
     * Get article by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsArticleDto> getArticleById(@PathVariable String id) {
        log.info("Request to get article with id: {}", id);
        return articleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get paginated list of all articles
     */
    @GetMapping
    public ResponseEntity<Page<NewsArticleDto>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Request to get all articles - page: {}, size: {}", page, size);
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NewsArticleDto> articles = articleService.findAll(pageRequest);
        return ResponseEntity.ok(articles);
    }

    /**
     * Get articles by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<NewsArticleDto>> getArticlesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request to get articles by category: {}", category);
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<NewsArticleDto> articles = articleService.findByCategory(category, pageRequest);
        
        return ResponseEntity.ok(articles);
    }

    /**
     * Get articles published after a specific date
     */
    @GetMapping("/published-after")
    public ResponseEntity<List<NewsArticleDto>> getArticlesPublishedAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        
        log.info("Request to get articles published after: {}", date);
        
        List<NewsArticleDto> articles = articleService.findByPublishedAfter(date);
        return ResponseEntity.ok(articles);
    }

    /**
     * Search articles by text in title or description
     */
    @GetMapping("/search")
    public ResponseEntity<List<NewsArticleDto>> searchArticles(@RequestParam String query) {
        log.info("Request to search articles with query: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<NewsArticleDto> articles = articleService.searchByText(query);
        return ResponseEntity.ok(articles);
    }
}
