package com.example.installations_microservice.services;

import com.example.installations_microservice.clients.UserServiceClient;
import com.example.installations_microservice.clients.dtos.UserDto; // ou .dto.UserDto selon votre choix
import com.example.installations_microservice.entities.Disponibilite;
import com.example.installations_microservice.entities.Installateur;
import com.example.installations_microservice.repos.InstallateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InstallateurService {
    
    @Autowired
    private InstallateurRepository installateurRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;

    public List<Installateur> findAll() {
        return installateurRepository.findAll();
    }

    public List<Installateur> findDisponibles(String date, String zone) {
        return installateurRepository.findByDisponibiliteAndZoneIntervention(
            Disponibilite.DISPONIBLE, zone);
    }

    public Installateur save(Installateur installateur) {
        return installateurRepository.save(installateur);
    }

    public Installateur update(Long id, Installateur installateur) {
        installateur.setId(id);
        return installateurRepository.save(installateur);
    }

    public List<Installateur> getAllInstallateurs() {
        // Synchroniser d'abord
        syncInstallateursFromUserService();
        
        // Maintenant retourner tous les installateurs
        return installateurRepository.findAll();
    }

    private void syncInstallateursFromUserService() {
        try {
            List<UserDto> users = userServiceClient.getInstallateursCommmande();
            users.forEach(user -> {
                if (!installateurRepository.existsByUserId(user.getUser_id())) {
                    Installateur newInst = new Installateur();
                    newInst.setUserId(user.getUser_id());
                    newInst.setNom(user.getUsername());
                    newInst.setEmail(user.getEmail());
                    newInst.setDisponibilite(Disponibilite.DISPONIBLE);
                    newInst.setZoneIntervention("Toutes");
                    newInst.setSpecialite("Non spécifiée");
                    newInst.setTelephone("Non spécifié");
                    
                    installateurRepository.save(newInst);
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la synchronisation: " + e.getMessage());
        }
    }
    
    @GetMapping("/debug/users")
    public List<UserDto> getUsersFromUserService() {
        return userServiceClient.getInstallateursCommmande();
    }

    @GetMapping("/debug/local")
    public List<Installateur> getLocalInstallateurs() {
        return installateurRepository.findAll();
    }

    public Installateur createInstallateur(UserDto user) {
        Installateur inst = new Installateur();
        inst.setUserId(user.getUser_id());
        inst.setNom(user.getUsername());
       // inst.setPrenom(user.getPrenom());
        inst.setEmail(user.getEmail());
        return installateurRepository.save(inst);
    }
    
    public Installateur findByUserId(Long userId) {
        return installateurRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Installateur non trouvé avec user ID: " + userId));
    }
}