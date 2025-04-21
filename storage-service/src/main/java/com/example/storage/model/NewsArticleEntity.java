package com.example.storage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_articles", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_published_at", columnList = "publishedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleEntity {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String author;
    
    private String sourceId;
    
    private String sourceName;
    
    private String url;
    
    private String imageUrl;
    
    private LocalDateTime publishedAt;
    
    private LocalDateTime fetchedAt;
    
    private String category;
}
