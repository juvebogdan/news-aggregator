package com.example.storage.integration;

import com.example.storage.model.NewsArticleDto;
import com.example.storage.model.NewsArticleEntity;
import com.example.storage.repository.NewsArticleRepository;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test for Kafka message consumption.
 * This test verifies that our service correctly consumes messages
 * from Kafka and processes them.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"news.incoming"})
@TestPropertySource(locations = "classpath:application-kafka-test.properties")
@DirtiesContext
public class KafkaIntegrationTest {

    @Autowired
    private NewsArticleRepository repository;
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Value("${kafka.topic.news}")
    private String topic;
    
    private Producer<String, NewsArticleDto> producer;
    
    @BeforeEach
    void setUp() {
        // Configure the producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producer = new DefaultKafkaProducerFactory<>(
                producerProps, 
                new StringSerializer(), 
                new JsonSerializer<NewsArticleDto>()).createProducer();
    }
    
    @AfterEach
    void tearDown() {
        if (producer != null) {
            producer.close();
        }
    }

    @Test
    public void testKafkaConsumer() {
        // Create a test article DTO
        String id = UUID.randomUUID().toString();
        NewsArticleDto articleDto = new NewsArticleDto();
        articleDto.setId(id);
        articleDto.setTitle("Kafka Test Article");
        articleDto.setDescription("Description for Kafka test");
        articleDto.setContent("Content for Kafka test");
        articleDto.setAuthor("Kafka Tester");
        articleDto.setSourceId("kafka-source");
        articleDto.setSourceName("Kafka Test Source");
        articleDto.setUrl("https://example.com/kafka-test/" + id);
        articleDto.setImageUrl("https://example.com/images/kafka-test/" + id);
        articleDto.setPublishedAt(LocalDateTime.now());
        articleDto.setFetchedAt(LocalDateTime.now());
        articleDto.setCategory("kafka-test");
        
        // Send the message to Kafka
        producer.send(new ProducerRecord<>(topic, id, articleDto));
        producer.flush();
        
        // Wait for the consumer to process the message
        // This uses Awaitility to poll until condition is met or timeout occurs
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            // Verify that the article has been saved to the database
            NewsArticleEntity saved = repository.findById(id).orElse(null);
            assertThat(saved).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("Kafka Test Article");
            assertThat(saved.getCategory()).isEqualTo("kafka-test");
        });
    }
}
