package com.example.installations_microservice.services;

import com.example.installations_microservice.clients.UserServiceClient;
import com.example.installations_microservice.clients.dtos.UserDto;
import com.example.installations_microservice.entities.Disponibilite;
import com.example.installations_microservice.entities.Installateur;
import com.example.installations_microservice.repos.InstallateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class InstallateurSyncService {
    
    @Autowired
    private InstallateurRepository installateurRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    UserServiceClient userServiceClient;
    
    @Scheduled(fixedRate = 3600000)
    public void syncInstallateurs() {
        try {
            System.out.println("Début synchronisation installateurs...");
            List<UserDto> users = userServiceClient.getInstallateursCommmande();
            System.out.println("Reçu " + users.size() + " installateurs du microservice users");

            users.forEach(user -> {
                Installateur existing = installateurRepository.findByUserId(user.getUser_id())
                    .orElseGet(() -> {
                        System.out.println("Création nouvel installateur pour user_id: " + user.getUser_id());
                        Installateur newInst = new Installateur();
                        newInst.setDisponibilite(Disponibilite.DISPONIBLE);
                        newInst.setZoneIntervention("Toutes");
                        newInst.setSpecialite("Non spécifiée");
                        newInst.setTelephone("Non spécifié");
                        return newInst;
                    });

                existing.setUserId(user.getUser_id());
                existing.setNom(user.getUsername());
                existing.setEmail(user.getEmail());
                
                Installateur saved = installateurRepository.save(existing);
                System.out.println("Installateur synchronisé: ID=" + saved.getId() + ", UserID=" + saved.getUserId());
            });
        } catch (Exception e) {
            System.err.println("ERREUR Synchronisation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}