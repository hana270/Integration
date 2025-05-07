package com.example.installations_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "affectations")
@Data
public class Affectation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installateur_id")
    private Installateur installateur;
    
    private Long commandeId; // Référence à la commande dans l'autre microservice
    
    private LocalDate dateInstallation;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    
    @Enumerated(EnumType.STRING)
    private StatutAffectation statut = StatutAffectation.PLANIFIEE;
    
    private String notes;
}

