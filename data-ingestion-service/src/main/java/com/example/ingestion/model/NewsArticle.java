package com.example.ingestion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {
    private String id = UUID.randomUUID().toString();
    private String title;
    private String description;
    private String content;
    private String author;
    private String sourceId;
    private String sourceName;
    private String url;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime fetchedAt = LocalDateTime.now();
    private String category;
}
