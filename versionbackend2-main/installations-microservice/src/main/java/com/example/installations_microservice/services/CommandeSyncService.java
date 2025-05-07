package com.example.installations_microservice.services;

import com.example.installations_microservice.clients.OrdersServiceClient;
import com.example.installations_microservice.dto.CommandeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CommandeSyncService {
    
    @Autowired
    private OrdersServiceClient ordersServiceClient;
    
    @Autowired
    @Qualifier("defaultRestTemplate")
    private RestTemplate restTemplate;
    
    @Value("${external-services.orders.url}")
    private String ordersServiceUrl;
    
    // Synchronisation forcée
    public CommandeResponse forceSyncCommande(Long commandeId) {
        System.out.println("Attempting to sync commande: " + commandeId);
        System.out.println("Using orders service URL: " + ordersServiceUrl);
        
        try {
            // 1. Try Feign
            System.out.println("Trying Feign client...");
            CommandeResponse commande = ordersServiceClient.getCommandeById(commandeId);
            System.out.println("Feign success: " + commande);
            return commande;
        } catch (Exception feignEx) {
            System.err.println("Feign failed: " + feignEx.getMessage());
            feignEx.printStackTrace();
            
            // 2. Try RestTemplate
            try {
                String url = ordersServiceUrl + "/api/panier/commandes/commande/" + commandeId;
                System.out.println("Trying RestTemplate with URL: " + url);
                
                ResponseEntity<CommandeResponse> response = restTemplate.getForEntity(url, CommandeResponse.class);
                System.out.println("RestTemplate response: " + response.getStatusCode());
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                }
            } catch (Exception restEx) {
                System.err.println("RestTemplate failed: " + restEx.getMessage());
                restEx.printStackTrace();
                throw new RuntimeException("Complete sync failure", restEx);
            }
        }
        throw new RuntimeException("Failed to sync commande");
    }

    // Vérification standard (utilisée dans le flux normal)
    public boolean verifyCommandeExists(Long commandeId) {
        try {
            forceSyncCommande(commandeId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}