package com.example.gestionbassins.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.gestionbassins.entities.Notification;

@Service
public class RemoteNotificationService {
    
    private final RestTemplate restTemplate;
    private final String notificationServiceUrl = "http://localhost:8082/notifications/api/notifications";

    public RemoteNotificationService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void sendNotification(String title, String message, String username) {
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create notification object
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setUsername(username);
        
        // Create the request entity
        HttpEntity<Notification> request = new HttpEntity<>(notification, headers);
        
        // Make the POST request
        restTemplate.postForObject(notificationServiceUrl + "/send", request, Void.class);
    }
}