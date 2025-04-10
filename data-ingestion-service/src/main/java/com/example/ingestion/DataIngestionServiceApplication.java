package com.example.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
public class DataIngestionServiceApplication {

    public static void main(String[] args) {
        // Load .env file before Spring starts
        loadEnvFile();
        
        // Start Spring application
        ConfigurableApplicationContext context = SpringApplication.run(DataIngestionServiceApplication.class, args);
        
        // Print the loaded API key (first few characters only, for security)
        String apiKey = System.getProperty("NEWS_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            String maskedKey = apiKey.substring(0, Math.min(4, apiKey.length())) + "...";
            System.out.println("API Key loaded successfully: " + maskedKey);
        } else {
            System.out.println("WARNING: NEWS_API_KEY not found in environment!");
        }
    }
    
    private static void loadEnvFile() {
        try {
            System.out.println("Attempting to load .env file...");
            Properties props = new Properties();
            props.load(Files.newBufferedReader(Paths.get(".env")));
            
            // Set as system properties so they're accessible everywhere
            props.forEach((key, value) -> {
                System.setProperty(key.toString(), value.toString());
                System.out.println("Loaded environment variable: " + key);
            });
            
            System.out.println(".env file loaded successfully");
        } catch (IOException e) {
            System.out.println("Could not load .env file: " + e.getMessage());
            System.out.println("Current directory: " + Paths.get(".").toAbsolutePath());
            try {
                // List files in current directory to help diagnose issues
                Files.list(Paths.get("."))
                    .forEach(path -> System.out.println("Found file: " + path));
            } catch (IOException ignored) {
                // Ignore
            }
        }
    }
}
