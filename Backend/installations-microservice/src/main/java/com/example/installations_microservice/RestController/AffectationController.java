package com.example.installations_microservice.RestController;

import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.installations_microservice.clients.OrdersServiceClient;
import com.example.installations_microservice.dto.AffectationDTO;
import com.example.installations_microservice.entities.Affectation;
import com.example.installations_microservice.entities.Installateur;
import com.example.installations_microservice.repos.InstallateurRepository;
import com.example.installations_microservice.services.AffectationService;
import com.example.installations_microservice.services.CommandeSyncService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/affectations")
public class AffectationController {
    
    @Autowired
    AffectationService affectationService;
    
    @Autowired
    CommandeSyncService commandeSyncService;
    
    @Autowired
    InstallateurRepository installateurRepository;
    
    @PostMapping
    public ResponseEntity<Affectation> createAffectation(@RequestBody AffectationDTO affectationDTO) {
        Affectation createdAffectation = affectationService.createAffectation(affectationDTO);
        return ResponseEntity.ok(createdAffectation);
    }
    
    /*@PostMapping
    public ResponseEntity<Affectation> createAffectation(@RequestBody AffectationDTO affectationDTO) {
        Affectation createdAffectation = affectationService.createAffectation(affectationDTO);
        return ResponseEntity.ok(createdAffectation);
    }*/
    
    @PostMapping("/affecterinstallation/{idcommande}")
    public ResponseEntity<?> affecterInstallation(
            @PathVariable Long idcommande,
            @RequestBody @Valid AffectationDTO affectationDTO) {
        
        try {
            if (affectationDTO.getInstallateurId() == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Installateur ID must not be null"));
            }
            
            if (!installateurRepository.existsByUserId(affectationDTO.getInstallateurId())) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Installateur non trouvé"));
            }
            
            affectationDTO.setCommandeId(idcommande);
            
            if (affectationDTO.getHeureDebut() == null) {
                affectationDTO.setHeureDebut(LocalTime.of(8, 0));
            }
            if (affectationDTO.getHeureFin() == null) {
                affectationDTO.setHeureFin(LocalTime.of(17, 0));
            }
            
            Affectation createdAffectation = affectationService.createAffectation(affectationDTO);
            return ResponseEntity.ok(createdAffectation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage()));
        }
    }
}