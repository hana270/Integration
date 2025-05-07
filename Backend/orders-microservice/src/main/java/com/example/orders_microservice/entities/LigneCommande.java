package com.example.orders_microservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class LigneCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    @JsonIgnoreProperties("lignesCommande")
    private Commande commande;
    
    private Long produitId;
    
    @Enumerated(EnumType.STRING)
    private TypeProduit typeProduit;
    
    private String nomProduit;
    private Integer quantite = 1;
    private Double prixUnitaire;
    
    public Double getMontantTotal() {
        return prixUnitaire * quantite;
    }
}