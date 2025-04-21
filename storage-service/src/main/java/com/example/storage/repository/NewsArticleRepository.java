package com.example.storage.repository;

import com.example.storage.model.NewsArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticleEntity, String> {
    
    // Find articles by category
    List<NewsArticleEntity> findByCategory(String category);
    
    // Find articles by category with pagination
    Page<NewsArticleEntity> findByCategory(String category, Pageable pageable);
    
    // Find articles published after a certain date
    List<NewsArticleEntity> findByPublishedAtAfter(LocalDateTime date);
    
    // Find articles by source name
    List<NewsArticleEntity> findBySourceName(String sourceName);
    
    // Find articles containing text in title or description (case insensitive)
    List<NewsArticleEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleText, String descriptionText);
            
    // Find articles by category and published date range
    List<NewsArticleEntity> findByCategoryAndPublishedAtBetween(
            String category, LocalDateTime start, LocalDateTime end);
}
