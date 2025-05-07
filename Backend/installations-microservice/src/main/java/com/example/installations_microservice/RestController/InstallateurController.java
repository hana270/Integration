package com.example.installations_microservice.RestController;


import com.example.installations_microservice.clients.UserServiceClient;
import com.example.installations_microservice.clients.dtos.UserDto;
import com.example.installations_microservice.entities.Installateur;
import com.example.installations_microservice.repos.InstallateurRepository;
import com.example.installations_microservice.services.InstallateurService;
import com.example.installations_microservice.services.InstallateurSyncService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/installateurs")
public class InstallateurController {
    
    @Autowired
    private InstallateurService installateurService;
    
    @Autowired
    private InstallateurSyncService installateurSyncService;
    
    @Autowired
    InstallateurRepository installateurRepository;
    
    @Autowired
    UserServiceClient userServiceClient;

    @GetMapping("/force-sync")
    public ResponseEntity<String> forceSync() {
        installateurSyncService.syncInstallateurs();
        return ResponseEntity.ok("Synchronisation forcée effectuée");
    }
    
    @GetMapping
    public ResponseEntity<List<Installateur>> getAllInstallateurs() {
        return ResponseEntity.ok(installateurService.findAll());
    }
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<Installateur>> getInstallateursDisponibles(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String zone) {
        return ResponseEntity.ok(installateurService.findDisponibles(date, zone));
    }
    
    @PostMapping
    public ResponseEntity<Installateur> createInstallateur(@RequestBody Installateur installateur) {
        return ResponseEntity.ok(installateurService.save(installateur));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Installateur> updateInstallateur(
            @PathVariable Long id, @RequestBody Installateur installateur) {
        return ResponseEntity.ok(installateurService.update(id, installateur));
    }
    
    @GetMapping("/debug/users")
    public ResponseEntity<List<UserDto>> debugUsers() {
        return ResponseEntity.ok(userServiceClient.getInstallateursCommmande());
    }

    @GetMapping("/debug/local")
    public ResponseEntity<List<Installateur>> debugLocal() {
        return ResponseEntity.ok(installateurRepository.findAll());
    }
}