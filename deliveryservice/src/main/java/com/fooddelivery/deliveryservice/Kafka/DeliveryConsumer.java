package com.fooddelivery.deliveryservice.Kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DeliveryConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "delivery-notifications", groupId = "delivery-service")
    public void listen(String message) {
        try {
            System.out.println("🛵 Received delivery notification: " + message);

            // Parse the JSON
            JsonNode orderJson = objectMapper.readTree(message);

            // Extract delivery details
            String orderId = orderJson.has("id") ? orderJson.get("id").asText() : "unknown";
            String customerId = orderJson.has("customerId") ? orderJson.get("customerId").asText() : "unknown";
            String status = orderJson.has("status") ? orderJson.get("status").asText() : "unknown";
            String location = orderJson.has("deliveryLocation") ?
                    orderJson.get("deliveryLocation").asText() : "unknown";

            System.out.println("📋 Delivery task created - Order: " + orderId +
                    ", Customer: " + customerId +
                    ", Status: " + status +
                    ", Location: " + location);

            // Here you would notify the delivery rider (e.g., via WebSocket)

        } catch (Exception e) {
            System.err.println("Error processing delivery notification: " + e.getMessage());
        }
    }
}