package com.example.storage.service;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.model.NewsArticleEntity;
import com.example.storage.repository.NewsArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsArticleServiceTest {

    @Mock
    private NewsArticleRepository repository;

    @Mock
    private NewsArticleMapper mapper;

    @InjectMocks
    private NewsArticleService service;

    private NewsArticleDto articleDto;
    private NewsArticleEntity articleEntity;
    private String articleId;

    @BeforeEach
    void setUp() {
        articleId = UUID.randomUUID().toString();
        
        // Setup test DTO
        articleDto = new NewsArticleDto();
        articleDto.setId(articleId);
        articleDto.setTitle("Test Article");
        articleDto.setDescription("Test Description");
        articleDto.setCategory("technology");
        articleDto.setPublishedAt(LocalDateTime.now());
        
        // Setup test Entity
        articleEntity = new NewsArticleEntity();
        articleEntity.setId(articleId);
        articleEntity.setTitle("Test Article");
        articleEntity.setDescription("Test Description");
        articleEntity.setCategory("technology");
        articleEntity.setPublishedAt(LocalDateTime.now());
        
        // We'll set up specific mock behaviors in each test method
    }

    @Test
    void testSaveArticle() {
        // Setup mock behavior specific to this test
        when(mapper.toEntity(articleDto)).thenReturn(articleEntity);
        when(repository.save(articleEntity)).thenReturn(articleEntity);
        when(mapper.toDto(articleEntity)).thenReturn(articleDto);
        
        // When
        NewsArticleDto result = service.saveArticle(articleDto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(articleId);
        verify(repository, times(1)).save(any(NewsArticleEntity.class));
    }

    @Test
    void testFindById() {
        // Setup mock behavior specific to this test
        when(repository.findById(articleId)).thenReturn(Optional.of(articleEntity));
        when(mapper.toDto(articleEntity)).thenReturn(articleDto);
        
        // When
        Optional<NewsArticleDto> result = service.findById(articleId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(articleId);
        verify(repository, times(1)).findById(articleId);
    }

    @Test
    void testFindByIdNotFound() {
        // Setup mock behavior specific to this test
        when(repository.findById(articleId)).thenReturn(Optional.empty());
        
        // When
        Optional<NewsArticleDto> result = service.findById(articleId);
        
        // Then
        assertThat(result).isEmpty();
        verify(repository, times(1)).findById(articleId);
    }

    @Test
    void testFindAll() {
        // Setup mock behavior specific to this test
        Pageable pageable = PageRequest.of(0, 10);
        List<NewsArticleEntity> entities = Arrays.asList(articleEntity);
        Page<NewsArticleEntity> entityPage = new PageImpl<>(entities, pageable, 1);
        
        when(repository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.toDto(articleEntity)).thenReturn(articleDto);
        
        // When
        Page<NewsArticleDto> result = service.findAll(pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(articleId);
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByCategory() {
        // Setup mock behavior specific to this test
        Pageable pageable = PageRequest.of(0, 10);
        List<NewsArticleEntity> entities = Arrays.asList(articleEntity);
        Page<NewsArticleEntity> entityPage = new PageImpl<>(entities, pageable, 1);
        
        when(repository.findByCategory("technology", pageable)).thenReturn(entityPage);
        when(mapper.toDto(articleEntity)).thenReturn(articleDto);
        
        // When
        Page<NewsArticleDto> result = service.findByCategory("technology", pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("technology");
        verify(repository, times(1)).findByCategory("technology", pageable);
    }

    @Test
    void testFindByPublishedAfter() {
        // Setup mock behavior specific to this test
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        List<NewsArticleEntity> entities = Arrays.asList(articleEntity);
        
        when(repository.findByPublishedAtAfter(date)).thenReturn(entities);
        when(mapper.toDto(articleEntity)).thenReturn(articleDto);
        
        // When
        List<NewsArticleDto> result = service.findByPublishedAfter(date);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(repository, times(1)).findByPublishedAtAfter(date);
    }

    @Test
    void testSearchByText() {
        // Setup mock behavior specific to this test
        String searchText = "test";
        List<NewsArticleEntity> entities = Arrays.asList(articleEntity);
        
        when(repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchText, searchText)).thenReturn(entities);
        when(mapper.toDto(articleEntity)).thenReturn(articleDto);
        
        // When
        List<NewsArticleDto> result = service.searchByText(searchText);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(repository, times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchText, searchText);
    }
}
