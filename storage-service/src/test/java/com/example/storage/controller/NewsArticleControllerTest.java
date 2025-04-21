package com.example.storage.controller;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.service.NewsArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsArticleController.class)
public class NewsArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsArticleService articleService;

    private NewsArticleDto articleDto;
    private String articleId;

    @BeforeEach
    void setUp() {
        articleId = UUID.randomUUID().toString();
        
        articleDto = new NewsArticleDto();
        articleDto.setId(articleId);
        articleDto.setTitle("Test Article");
        articleDto.setDescription("Test Description");
        articleDto.setContent("Test Content");
        articleDto.setAuthor("Test Author");
        articleDto.setCategory("technology");
        articleDto.setPublishedAt(LocalDateTime.now());
    }

    @Test
    void testGetArticleById() throws Exception {
        // Given
        when(articleService.findById(articleId)).thenReturn(Optional.of(articleDto));
        
        // When & Then
        mockMvc.perform(get("/api/articles/{id}", articleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(articleId)))
                .andExpect(jsonPath("$.title", is("Test Article")));
    }

    @Test
    void testGetArticleByIdNotFound() throws Exception {
        // Given
        when(articleService.findById(articleId)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/articles/{id}", articleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllArticles() throws Exception {
        // Given
        List<NewsArticleDto> articles = Arrays.asList(articleDto);
        when(articleService.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(articles));
        
        // When & Then
        mockMvc.perform(get("/api/articles")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(articleId)));
    }

    @Test
    void testGetArticlesByCategory() throws Exception {
        // Given
        List<NewsArticleDto> articles = Arrays.asList(articleDto);
        when(articleService.findByCategory(eq("technology"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(articles));
        
        // When & Then
        mockMvc.perform(get("/api/articles/category/{category}", "technology")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category", is("technology")));
    }

    @Test
    void testGetArticlesPublishedAfter() throws Exception {
        // Given
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        String dateStr = date.format(DateTimeFormatter.ISO_DATE_TIME);
        List<NewsArticleDto> articles = Arrays.asList(articleDto);
        
        when(articleService.findByPublishedAfter(any(LocalDateTime.class)))
                .thenReturn(articles);
        
        // When & Then
        mockMvc.perform(get("/api/articles/published-after")
                .param("date", dateStr)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(articleId)));
    }

    @Test
    void testSearchArticles() throws Exception {
        // Given
        List<NewsArticleDto> articles = Arrays.asList(articleDto);
        when(articleService.searchByText("test"))
                .thenReturn(articles);
        
        // When & Then
        mockMvc.perform(get("/api/articles/search")
                .param("query", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Article")));
    }

    @Test
    void testSearchArticlesEmptyQuery() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/articles/search")
                .param("query", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
