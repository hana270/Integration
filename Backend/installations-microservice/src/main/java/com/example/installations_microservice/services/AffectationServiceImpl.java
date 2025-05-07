package com.example.installations_microservice.services;

import com.example.installations_microservice.clients.OrdersServiceClient;
import com.example.installations_microservice.dto.AffectationDTO;
import com.example.installations_microservice.entities.Affectation;
import com.example.installations_microservice.entities.Installateur;
import com.example.installations_microservice.entities.StatutAffectation;
import com.example.installations_microservice.repos.AffectationRepository;

import java.time.LocalTime;

import org.springframework.stereotype.Service;

@Service
public class AffectationServiceImpl implements AffectationService {
    
    private final AffectationRepository affectationRepository;
    private final InstallateurService installateurService;
    private final OrdersServiceClient ordersServiceClient;
    private final CommandeSyncService commandeSyncService;
    
    public AffectationServiceImpl(
            AffectationRepository affectationRepository,
            InstallateurService installateurService,
            OrdersServiceClient ordersServiceClient,
            CommandeSyncService commandeSyncService) {
        this.affectationRepository = affectationRepository;
        this.installateurService = installateurService;
        this.ordersServiceClient = ordersServiceClient;
        this.commandeSyncService = commandeSyncService;
    }
    
    @Override
    public Affectation createAffectation(AffectationDTO dto) {
        // Validation du DTO
        validateAffectationDTO(dto);

        // Vérification de l'installateur
        Installateur installateur = verifyInstallateur(dto.getInstallateurId());
        
        // Vérification de la commande
        verifyCommande(dto.getCommandeId());
        
        // Création de l'affectation
        Affectation affectation = createNewAffectation(dto, installateur);
        
        // Mise à jour du statut
        updateOrderStatus(dto.getCommandeId());
        
        return affectationRepository.save(affectation);
    }
    
    private void validateAffectationDTO(AffectationDTO dto) {
        if (dto == null || dto.getInstallateurId() == null || dto.getCommandeId() == null) {
            throw new IllegalArgumentException("Les données d'affectation et les IDs doivent être renseignés");
        }
    }
    
    private Installateur verifyInstallateur(Long installateurId) {
        return installateurService.findByUserId(installateurId);
    }
    
    private void verifyCommande(Long commandeId) {
        if (!commandeSyncService.verifyCommandeExists(commandeId)) {
            throw new RuntimeException("Commande non trouvée avec ID: " + commandeId);
        }
    }
    
    private Affectation createNewAffectation(AffectationDTO dto, Installateur installateur) {
        Affectation affectation = new Affectation();
        affectation.setInstallateur(installateur);
        affectation.setCommandeId(dto.getCommandeId());
        affectation.setDateInstallation(dto.getDateInstallation());
        affectation.setHeureDebut(dto.getHeureDebut() != null ? dto.getHeureDebut() : LocalTime.of(8, 0));
        affectation.setHeureFin(dto.getHeureFin() != null ? dto.getHeureFin() : LocalTime.of(17, 0));
        affectation.setStatut(StatutAffectation.PLANIFIEE);
        return affectation;
    }
    
    private void updateOrderStatus(Long commandeId) {
        try {
            // Vérifie d'abord que la commande existe
            if (!commandeSyncService.verifyCommandeExists(commandeId)) {
                throw new RuntimeException("Commande non trouvée");
            }
            
            // Met à jour le statut dans orders-microservice
            ordersServiceClient.updateStatutCommande(commandeId, "EN_INSTALLATION");
            
        } catch (Exception e) {
            throw new RuntimeException("Échec de la mise à jour du statut de la commande", e);
        }
    }
}