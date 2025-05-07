package com.example.installations_microservice.repos;

import com.example.installations_microservice.entities.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    // Méthodes personnalisées si nécessaire
}