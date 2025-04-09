package com.example.ingestion.service;

import com.example.ingestion.model.NewsArticle;
import com.example.ingestion.model.NewsApiResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticleMapper {

    public List<NewsArticle> mapFromNewsApiResponse(NewsApiResponse response, String category) {
        return response.getArticles().stream()
                .map(article -> mapArticle(article, category))
                .collect(Collectors.toList());
    }

    private NewsArticle mapArticle(NewsApiResponse.Article apiArticle, String category) {
        NewsArticle article = new NewsArticle();
        
        article.setTitle(apiArticle.getTitle());
        article.setDescription(apiArticle.getDescription());
        article.setContent(apiArticle.getContent());
        article.setAuthor(apiArticle.getAuthor());
        article.setUrl(apiArticle.getUrl());
        article.setImageUrl(apiArticle.getUrlToImage());
        
        // Map source information
        if (apiArticle.getSource() != null) {
            article.setSourceId(apiArticle.getSource().getId());
            article.setSourceName(apiArticle.getSource().getName());
        }
        
        // Parse and set the publication date
        if (apiArticle.getPublishedAt() != null) {
            try {
                article.setPublishedAt(ZonedDateTime.parse(apiArticle.getPublishedAt()).toLocalDateTime());
            } catch (Exception e) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }
        
        article.setCategory(category);
        
        return article;
    }
}
