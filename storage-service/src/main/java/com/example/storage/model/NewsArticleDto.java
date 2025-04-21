package com.example.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private String id;
    private String title;
    private String description;
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
