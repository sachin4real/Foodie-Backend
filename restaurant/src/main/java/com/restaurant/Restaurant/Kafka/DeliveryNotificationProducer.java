package com.restaurant.Restaurant.Kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restaurant.Restaurant.model.Restaurant;
import com.restaurant.Restaurant.service.RestaurantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Producer service that sends delivery notifications with enriched restaurant data
 */
@Service
public class DeliveryNotificationProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RestaurantService restaurantService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TOPIC = "delivery-notifications";

    /**
     * Enriches order data with restaurant information and sends to delivery service
     *
     * @param orderJson The original order JSON from the order service
     */
    public void sendOrderReadyNotification(String orderJson) {
        try {
            // Parse the JSON message
            JsonNode orderNode = objectMapper.readTree(orderJson);

            // Extract restaurant ID from items array (first item)
            String restaurantId = null;
            if (orderNode.has("items") && orderNode.get("items").isArray() &&
                    orderNode.get("items").size() > 0) {
                JsonNode firstItem = orderNode.get("items").get(0);
                if (firstItem.has("restaurantId")) {
                    restaurantId = firstItem.get("restaurantId").asText();
                }
            }

            // Create a modifiable copy of the order
            ObjectNode enrichedOrder = orderNode.deepCopy();

            // Lookup restaurant details if ID was found
            if (restaurantId != null && !restaurantId.isEmpty()) {
                Optional<Restaurant> restaurant = restaurantService.getRestaurantById(restaurantId);

                if (restaurant.isPresent()) {
                    Restaurant r = restaurant.get();
                    // Add restaurant details
                    enrichedOrder.put("restaurantAddress", r.getAddress());
                    enrichedOrder.put("restaurantEmail", r.getEmail());

                    System.out.println("✅ Added restaurant address for order notification");
                } else {
                    System.out.println("⚠️ Restaurant not found with ID: " + restaurantId);
                }
            } else {
                System.out.println("⚠️ No restaurant ID found in order JSON");
            }

            // Set order status to READY_FOR_DELIVERY
            enrichedOrder.put("status", "READY_FOR_DELIVERY");

            // Convert to string and send to Kafka
            String enrichedJson = objectMapper.writeValueAsString(enrichedOrder);
            kafkaTemplate.send(TOPIC, enrichedJson);
            System.out.println("🚨 Sent delivery notification: " + enrichedJson);

        } catch (Exception e) {
            System.err.println("❌ Error sending notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}