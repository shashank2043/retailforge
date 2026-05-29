package com.retailforge.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class NotificationServiceApplication {

//    static {
//        try {
//            java.nio.file.Path envPath = java.nio.file.Paths.get(".env");
//            if (!java.nio.file.Files.exists(envPath)) {
//                envPath = java.nio.file.Paths.get("../.env");
//            }
//            if (java.nio.file.Files.exists(envPath)) {
//                java.nio.file.Files.readAllLines(envPath).forEach(line -> {
//                    line = line.trim();
//                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
//                        int index = line.indexOf("=");
//                        String key = line.substring(0, index).trim();
//                        String value = line.substring(index + 1).trim();
//                        if (value.startsWith("\"") && value.endsWith("\"")) {
//                            value = value.substring(1, value.length() - 1);
//                        } else if (value.startsWith("'") && value.endsWith("'")) {
//                            value = value.substring(1, value.length() - 1);
//                        }
//                        System.setProperty(key, value);
//                    }
//                });
//                System.out.println("Successfully loaded Environment variables from: " + envPath.toAbsolutePath());
//            } else {
//                System.out.println(".env file not found in current or parent directory. Operating with configuration defaults.");
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to parse .env file: " + e.getMessage());
//        }
//    }

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
