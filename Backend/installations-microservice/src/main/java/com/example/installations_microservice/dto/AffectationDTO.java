package com.example.installations_microservice.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public class AffectationDTO {
	@NotNull(message = "Installateur ID is required")
    private Long installateurId;
	
    @NotNull(message = "Commande ID is required")
    private Long commandeId;
    private LocalDate dateInstallation;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    
    // Getters and Setters
    public Long getInstallateurId() {
        return installateurId;
    }

    public void setInstallateurId(Long installateurId) {
        this.installateurId = installateurId;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long commandeId) {
        this.commandeId = commandeId;
    }

    public LocalDate getDateInstallation() {
        return dateInstallation;
    }

    public void setDateInstallation(LocalDate dateInstallation) {
        this.dateInstallation = dateInstallation;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }
}