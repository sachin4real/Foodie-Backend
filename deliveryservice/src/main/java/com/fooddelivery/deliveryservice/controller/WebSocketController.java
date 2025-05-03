package com.fooddelivery.deliveryservice.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Listen to Kafka topic to get delivery notifications
    @KafkaListener(topics = "delivery-notifications", groupId = "delivery-service-group")
    public void onDeliveryNotification(String message) {
        // Send the message to the frontend via WebSocket
        messagingTemplate.convertAndSend("/topic/delivery", message);
    }
}
