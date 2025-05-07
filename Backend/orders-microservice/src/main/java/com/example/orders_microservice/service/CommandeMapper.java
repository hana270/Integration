package com.example.orders_microservice.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.orders_microservice.dto.CommandeDTO;
import com.example.orders_microservice.dto.LigneCommandeDTO;
import com.example.orders_microservice.entities.Commande;
/*
@Service
public class CommandeMapper {
    public CommandeDTO toDto(Commande commande) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(commande.getId());
        dto.setNumeroCommande(commande.getNumeroCommande());
        dto.setClientId(commande.getClientId());
        dto.setEmailClient(commande.getEmailClient());
        dto.setStatut(commande.getStatut().name());
        
        dto.setAdresseLivraison(commande.getAdresseLivraison());
        dto.setCodePostal(commande.getCodePostal());
        dto.setVille(commande.getVille());
        dto.setPays(commande.getPays());
        
        dto.setLignesCommande(commande.getLignesCommande().stream()
            .map(ligne -> {
                LigneCommandeDTO ligneDto = new LigneCommandeDTO();
                ligneDto.setId(ligne.getId());
                // Autres champs
                return ligneDto;
            })
            .collect(Collectors.toList()));
        
        return dto;
    }
}*/