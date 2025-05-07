package com.example.installations_microservice.services;

import com.example.installations_microservice.dto.AffectationDTO;
import com.example.installations_microservice.entities.Affectation;

public interface AffectationService {
	public Affectation createAffectation(AffectationDTO dto);
}
