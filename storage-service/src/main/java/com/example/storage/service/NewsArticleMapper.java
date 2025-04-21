package com.example.storage.service;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.model.NewsArticleEntity;
import org.springframework.stereotype.Component;

@Component
public class NewsArticleMapper {

    /**
     * Converts a DTO object (from Kafka) to an entity object (for database)
     */
    public NewsArticleEntity toEntity(NewsArticleDto dto) {
        if (dto == null) {
            return null;
        }
        
        NewsArticleEntity entity = new NewsArticleEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setContent(dto.getContent());
        entity.setAuthor(dto.getAuthor());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceName(dto.getSourceName());
        entity.setUrl(dto.getUrl());
        entity.setImageUrl(dto.getImageUrl());
        entity.setPublishedAt(dto.getPublishedAt());
        entity.setFetchedAt(dto.getFetchedAt());
        entity.setCategory(dto.getCategory());
        
        return entity;
    }

    /**
     * Converts an entity object (from database) to a DTO object
     */
    public NewsArticleDto toDto(NewsArticleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        NewsArticleDto dto = new NewsArticleDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setContent(entity.getContent());
        dto.setAuthor(entity.getAuthor());
        dto.setSourceId(entity.getSourceId());
        dto.setSourceName(entity.getSourceName());
        dto.setUrl(entity.getUrl());
        dto.setImageUrl(entity.getImageUrl());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setFetchedAt(entity.getFetchedAt());
        dto.setCategory(entity.getCategory());
        
        return dto;
    }
}
