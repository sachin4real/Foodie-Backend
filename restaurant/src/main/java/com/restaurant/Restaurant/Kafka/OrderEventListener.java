package com.restaurant.Restaurant.Kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListener {
    @Autowired
    private DeliveryNotificationProducer notificationProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "order-events", groupId = "restaurant-service")
    public void listen(String message) {
        try {
            System.out.println("Received order event: " + message);

            // Parse the order JSON
            JsonNode orderJson = objectMapper.readTree(message);

            // Extract order ID and customer ID for logging
            String orderId = orderJson.has("id") ? orderJson.get("id").asText() : "null";
            String customerId = orderJson.has("customerId") ? orderJson.get("customerId").asText() : "null";

            System.out.println("👩‍🍳 Preparing order: " + orderId + "," + customerId);

            // Simulate food preparation (5 seconds)
            Thread.sleep(5000);

            // Create a modified JSON with READY status for delivery service
            ObjectNode readyOrder = objectMapper.createObjectNode();
            readyOrder.put("status", "READY_FOR_DELIVERY");

            // Copy all fields from original order
            orderJson.fields().forEachRemaining(entry ->
                    readyOrder.set(entry.getKey(), entry.getValue()));

            // Notify delivery service with the complete order details
            notificationProducer.sendOrderReadyNotification(readyOrder.toString());

        } catch (Exception e) {
            System.err.println("Error processing order: " + e.getMessage());
            e.printStackTrace();
        }
    }
}