package com.example.ingestion.service;

import com.example.ingestion.model.NewsApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsApiClientTest {

    @Mock
    private WebClient webClientMock;
    
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;
    
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;
    
    @Mock
    private WebClient.ResponseSpec responseSpecMock;
    
    private NewsApiClient newsApiClient;
    
    @BeforeEach
    void setUp() {
        // Create the NewsApiClient with test values
        newsApiClient = new NewsApiClient(
                "https://newsapi.org/v2", 
                "test-api-key", 
                "technology");
        
        // Inject mocked WebClient
        ReflectionTestUtils.setField(newsApiClient, "webClient", webClientMock);
        
        // Set up the WebClient method chain mocks
        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        
        // This is the key part - using the specific argument matcher
        when(requestHeadersUriSpecMock.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any()))
                .thenReturn(requestHeadersSpecMock);
                
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
    }
    
    @Test
    void shouldFetchTopHeadlinesWithCategory() {
        // Arrange
        NewsApiResponse expectedResponse = new NewsApiResponse();
        expectedResponse.setStatus("ok");
        
        when(responseSpecMock.bodyToMono(NewsApiResponse.class)).thenReturn(Mono.just(expectedResponse));
        
        // Act
        Mono<NewsApiResponse> result = newsApiClient.fetchTopHeadlines("business");
        
        // Assert
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }
    
    @Test
    void shouldUseDefaultCategoryWhenCategoryIsNull() {
        // Arrange
        NewsApiResponse expectedResponse = new NewsApiResponse();
        expectedResponse.setStatus("ok");
        
        when(responseSpecMock.bodyToMono(NewsApiResponse.class)).thenReturn(Mono.just(expectedResponse));
        
        // Act
        Mono<NewsApiResponse> result = newsApiClient.fetchTopHeadlines(null);
        
        // Assert
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleErrorFromApi() {
        // Arrange
        RuntimeException testException = new RuntimeException("API Error");
        when(responseSpecMock.bodyToMono(NewsApiResponse.class)).thenReturn(Mono.error(testException));
        
        // Act
        Mono<NewsApiResponse> result = newsApiClient.fetchTopHeadlines("science");
        
        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error -> error.equals(testException))
                .verify();
    }
    
    @Test
    void shouldCorrectlyParseValidApiResponse() {
        // Arrange - create a realistic API response
        NewsApiResponse apiResponse = new NewsApiResponse();
        apiResponse.setStatus("ok");
        apiResponse.setTotalResults(2);
        
        // Create sample articles
        NewsApiResponse.Article article1 = new NewsApiResponse.Article();
        article1.setTitle("Test Article 1");
        article1.setDescription("Test Description 1");
        article1.setPublishedAt("2023-04-09T15:30:00Z");
        
        NewsApiResponse.Source source1 = new NewsApiResponse.Source();
        source1.setId("source-1");
        source1.setName("Test Source");
        article1.setSource(source1);
        
        NewsApiResponse.Article article2 = new NewsApiResponse.Article();
        article2.setTitle("Test Article 2");
        // Intentionally missing some fields to test robustness
        
        List<NewsApiResponse.Article> articles = new ArrayList<>();
        articles.add(article1);
        articles.add(article2);
        apiResponse.setArticles(articles);
        
        // Mock the API to return our test response
        when(responseSpecMock.bodyToMono(NewsApiResponse.class)).thenReturn(Mono.just(apiResponse));
        
        // Act
        Mono<NewsApiResponse> result = newsApiClient.fetchTopHeadlines("technology");
        
        // Assert - verify the response structure is preserved
        StepVerifier.create(result)
            .expectNextMatches(response -> {
                // Verify status code
                boolean statusCorrect = "ok".equals(response.getStatus());
                
                // Verify article count
                boolean countCorrect = response.getTotalResults() == 2;
                
                // Verify first article details
                boolean article1Correct = response.getArticles().get(0).getTitle().equals("Test Article 1") &&
                                         "Test Source".equals(response.getArticles().get(0).getSource().getName());
                
                // Verify the client properly handled the second article with missing fields
                boolean article2Correct = response.getArticles().size() >= 2 && 
                                         "Test Article 2".equals(response.getArticles().get(1).getTitle());
                
                return statusCorrect && countCorrect && article1Correct && article2Correct;
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyArticlesList() {
        // Arrange - response with empty articles list
        NewsApiResponse emptyResponse = new NewsApiResponse();
        emptyResponse.setStatus("ok");
        emptyResponse.setTotalResults(0);
        emptyResponse.setArticles(new ArrayList<>()); // Using ArrayList instead of Collections.emptyList()
        
        when(responseSpecMock.bodyToMono(NewsApiResponse.class)).thenReturn(Mono.just(emptyResponse));
        
        // Act
        Mono<NewsApiResponse> result = newsApiClient.fetchTopHeadlines("business");
        
        // Assert
        StepVerifier.create(result)
            .expectNextMatches(response -> 
                "ok".equals(response.getStatus()) && 
                response.getTotalResults() == 0 && 
                response.getArticles().isEmpty()
            )
            .verifyComplete();
    }

    @Test
    void shouldHandleErrorResponseFromApi() {
        // Arrange - API returns an error response rather than throwing an exception
        NewsApiResponse errorResponse = new NewsApiResponse();
        errorResponse.setStatus("error");
        errorResponse.setTotalResults(0);
        // No articles provided
        
        when(responseSpecMock.bodyToMono(NewsApiResponse.class)).thenReturn(Mono.just(errorResponse));
        
        // Act
        Mono<NewsApiResponse> result = newsApiClient.fetchTopHeadlines("health");
        
        // Assert
        StepVerifier.create(result)
            .expectNextMatches(response -> "error".equals(response.getStatus()))
            .verifyComplete();
    }
}
