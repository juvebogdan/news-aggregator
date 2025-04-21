package com.example.storage.service;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.model.NewsArticleEntity;
import com.example.storage.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsArticleService {

    private final NewsArticleRepository repository;
    private final NewsArticleMapper mapper;

    /**
     * Save a news article to the database
     */
    @Transactional
    public NewsArticleDto saveArticle(NewsArticleDto articleDto) {
        log.info("Saving article with id: {}", articleDto.getId());
        
        NewsArticleEntity entity = mapper.toEntity(articleDto);
        entity = repository.save(entity);
        
        return mapper.toDto(entity);
    }

    /**
     * Find an article by ID
     */
    public Optional<NewsArticleDto> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDto);
    }

    /**
     * Get all articles with pagination
     */
    public Page<NewsArticleDto> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDto);
    }

    /**
     * Find articles by category with pagination
     */
    public Page<NewsArticleDto> findByCategory(String category, Pageable pageable) {
        return repository.findByCategory(category, pageable)
                .map(mapper::toDto);
    }

    /**
     * Find articles published after a specific date
     */
    public List<NewsArticleDto> findByPublishedAfter(LocalDateTime date) {
        return repository.findByPublishedAtAfter(date)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search for articles containing text in title or description
     */
    public List<NewsArticleDto> searchByText(String text) {
        return repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
