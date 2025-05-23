package com.example.gestionbassins.restcontrollers;

import com.example.gestionbassins.dto.AccessoireDTO;
import com.example.gestionbassins.dto.BassinPersonnaliseDTO;
import com.example.gestionbassins.entities.Accessoire;
import com.example.gestionbassins.entities.Bassin;
import com.example.gestionbassins.entities.BassinPersonnalise;
import com.example.gestionbassins.repos.AccessoireRepository;
import com.example.gestionbassins.repos.BassinPersonnaliseRepository;
import com.example.gestionbassins.repos.BassinRepository;
import com.example.gestionbassins.service.BassinPersonnaliseServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bassinpersonnalise/")
public class BassinPersonnaliseRestController {

    @Autowired
    private BassinPersonnaliseServiceImpl bassinPersonnaliseService;
    
    @Autowired
    private BassinPersonnaliseRepository bassinPersonnaliseRepository;
    
    @Value("${upload.dir}")
    private String UPLOAD_DIR;

    @Autowired 
    private BassinRepository bassinRepository;
    
    @Autowired
    private AccessoireRepository accessoireRepository;
    
    
    @PostMapping("/ajouterBassinPersonnalise/{idBassin}")
    public ResponseEntity<?> ajouterBassinPersonnalise(
            @PathVariable("idBassin") Long idBassin, // Récupérer l'ID du bassin depuis l'URL
            @RequestParam("bassinPersonnalise") String bassinPersonnaliseJson,
            @RequestParam(value = "accessoireImages", required = false) List<MultipartFile> accessoireImages) {
        try {
            // 1️⃣ Convertir le JSON en objet Java
            ObjectMapper objectMapper = new ObjectMapper();
            BassinPersonnaliseDTO bassinPersonnaliseDTO = objectMapper.readValue(bassinPersonnaliseJson, BassinPersonnaliseDTO.class);

            // 2️⃣ Récupérer le bassin existant par son ID (depuis l'URL)
            Bassin bassin = bassinRepository.findById(idBassin)
                    .orElseThrow(() -> new RuntimeException("Bassin non trouvé avec l'ID : " + idBassin));

            // 3️⃣ Créer un nouvel objet BassinPersonnalise
            BassinPersonnalise bassinPersonnalise = new BassinPersonnalise();
            bassinPersonnalise.setBassin(bassin); // Associer le bassin récupéré
            bassinPersonnalise.setMateriaux(bassinPersonnaliseDTO.getMateriaux());
            bassinPersonnalise.setDimensions(bassinPersonnaliseDTO.getDimensions());
            bassinPersonnalise.setDureeFabrication(bassinPersonnaliseDTO.getDureeFabrication());

            // 4️⃣ Sauvegarder le bassin personnalisé pour obtenir un ID
            bassinPersonnalise = bassinPersonnaliseRepository.save(bassinPersonnalise);

            // 5️⃣ Créer une copie finale de bassinPersonnalise pour l'utiliser dans le stream
            final BassinPersonnalise finalBassinPersonnalise = bassinPersonnalise;

            // 6️⃣ Convertir les AccessoireDTO en Accessoire en utilisant la méthode du service
            List<Accessoire> accessoires = bassinPersonnaliseService.convertirAccessoireDTOEnAccessoire(bassinPersonnaliseDTO.getAccessoires());

            // Associer chaque accessoire au bassin personnalisé
            accessoires.forEach(accessoire -> accessoire.setBassinPersonnalise(finalBassinPersonnalise));

            // 7️⃣ Sauvegarder les accessoires dans la base de données
            bassinPersonnalise.setAccessoires(accessoires);
            bassinPersonnaliseRepository.save(bassinPersonnalise);

            // 8️⃣ Sauvegarder les images des accessoires
            if (accessoireImages != null && !accessoireImages.isEmpty()) {
                for (int i = 0; i < accessoireImages.size(); i++) {
                    MultipartFile file = accessoireImages.get(i);

                    if (file != null && !file.isEmpty()) {
                        // Associer l'image au bon accessoire
                        if (i < accessoires.size()) {
                            Accessoire accessoire = accessoires.get(i);
                            bassinPersonnaliseService.uploadImageAccessoireForAdd(bassinPersonnalise, accessoire, file); // Utiliser la nouvelle méthode
                        }
                    }
                }
            }

            // 9️⃣ Sauvegarder à nouveau pour mettre à jour les chemins d'image
            bassinPersonnaliseRepository.save(bassinPersonnalise);

            // 🔟 Créer une réponse personnalisée avec seulement l'ID du bassin
            BassinPersonnaliseDTO responseDTO = new BassinPersonnaliseDTO();
            responseDTO.setIdBassinPersonnalise(bassinPersonnalise.getIdBassinPersonnalise());
            responseDTO.setIdBassin(bassin.getIdBassin()); // Seulement l'ID du bassin
            responseDTO.setMateriaux(bassinPersonnalise.getMateriaux());
            responseDTO.setDimensions(bassinPersonnalise.getDimensions());

            // Convertir les Accessoire en AccessoireDTO
            List<AccessoireDTO> accessoireDTOs = convertirAccessoiresEnDTO(bassinPersonnalise.getAccessoires());
            responseDTO.setAccessoires(accessoireDTOs);

            // Retourner la réponse personnalisée
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            // Log de l'erreur
            System.err.println("Erreur lors de l'ajout du bassin personnalisé : " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, retourner une réponse d'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'ajout du bassin personnalisé : " + e.getMessage());
        }
    }
    
    private List<AccessoireDTO> convertirAccessoiresEnDTO(List<Accessoire> accessoires) {
        return accessoires.stream()
                .map(accessoire -> {
                    AccessoireDTO accessoireDTO = new AccessoireDTO();
                    accessoireDTO.setIdAccessoire(accessoire.getIdAccessoire());
                    accessoireDTO.setNomAccessoire(accessoire.getNomAccessoire());
                    accessoireDTO.setPrixAccessoire(accessoire.getPrixAccessoire());
                    accessoireDTO.setImagePath(accessoire.getImagePath());
                    accessoireDTO.setIdBassinPersonnalise(accessoire.getBassinPersonnalise().getIdBassinPersonnalise());
                    return accessoireDTO;
                })
                .collect(Collectors.toList());
    }


    @GetMapping("/getAllBassinPersonnalise")
    public ResponseEntity<List<BassinPersonnalise>> listeBassinsPersonnalises() {
        List<BassinPersonnalise> bassins = bassinPersonnaliseService.listeBassinsPersonnalises();
        return ResponseEntity.ok(bassins);
    }

    /*@GetMapping("/getBassinPersonnalise/{id}")
    public ResponseEntity<BassinPersonnalise> trouverBassinPersonnaliseParId(@PathVariable Long id) {
        BassinPersonnalise bassin = bassinPersonnaliseService.trouverBassinPersonnaliseParId(id);
        return ResponseEntity.ok(bassin);
    }*/
    
    @GetMapping("/detailBassinPersonnalise/{idBassin}")
    public ResponseEntity<?> getDetailBassin(@PathVariable("idBassin") Long idBassin) {
        try {
            // Récupérer le bassin personnalisé par l'ID du bassin
            BassinPersonnalise bassinPersonnalise = bassinPersonnaliseService.trouverBassinPersonnaliseParIdBassin(idBassin);

            if (bassinPersonnalise == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("message", "Bassin personnalisé non trouvé pour le bassin avec l'ID : " + idBassin));
            }

            // Retourner les détails du bassin personnalisé
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(bassinPersonnalise);
        } catch (Exception e) {
            // Log de l'erreur
            System.err.println("Erreur lors de la récupération des détails du bassin personnalisé : " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, retourner une réponse d'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Erreur lors de la récupération des détails du bassin personnalisé : " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/supprimerBassinPersonnalise/{id}")
    public ResponseEntity<?> supprimerBassinPersonnalise(@PathVariable("id") Long idBassinPersonnalise) {
        try {
            // Supprimer le bassin personnalisé
            bassinPersonnaliseService.supprimerBassinPersonnalise(idBassinPersonnalise);

            // Retourner une réponse de succès
            return ResponseEntity.ok().body(Map.of("message", "Bassin personnalisé supprimé avec succès"));
        } catch (Exception e) {
            // Log de l'erreur
            System.err.println("Erreur lors de la suppression du bassin personnalisé : " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, retourner une réponse d'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la suppression du bassin personnalisé : " + e.getMessage()));
        }
    }
    
    
    @PutMapping("/mettreAJourBassinPersonnalise/{id}")
    public ResponseEntity<?> mettreAJourBassinPersonnalise(
            @PathVariable("id") Long idBassinPersonnalise,
            @RequestParam("bassinPersonnalise") String bassinPersonnaliseJson,
            HttpServletRequest request) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BassinPersonnaliseDTO bassinPersonnaliseDTO = objectMapper.readValue(bassinPersonnaliseJson, BassinPersonnaliseDTO.class);
            List<Accessoire> accessoires = bassinPersonnaliseService.convertirAccessoireDTOEnAccessoire(bassinPersonnaliseDTO.getAccessoires());

            // Traiter les requêtes multipart manuellement pour obtenir les images
            Map<Integer, MultipartFile> accessoireImagesMap = new HashMap<>();
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                
                // Parcourir tous les paramètres pour trouver les index d'accessoires
                Map<String, String[]> paramMap = multipartRequest.getParameterMap();
                for (String paramName : paramMap.keySet()) {
                    if (paramName.startsWith("accessoireIndex_")) {
                        int accessoireIndex = Integer.parseInt(paramMap.get(paramName)[0]);
                        
                        // Récupérer l'image correspondante
                        MultipartFile file = multipartRequest.getFile("accessoireImage_" + accessoireIndex);
                        if (file != null && !file.isEmpty()) {
                            accessoireImagesMap.put(accessoireIndex, file);
                            System.out.println("Image trouvée pour l'accessoire " + accessoireIndex);
                        }
                    }
                }
            }

            // Afficher des informations de debug
            System.out.println("Nombre d'accessoires : " + accessoires.size());
            System.out.println("Nombre d'images : " + accessoireImagesMap.size());

            BassinPersonnalise bassinPersonnalise = bassinPersonnaliseService.mettreAJourBassinPersonnaliseWithMap(
                    idBassinPersonnalise,
                    bassinPersonnaliseDTO.getIdBassin(),
                    bassinPersonnaliseDTO.getMateriaux(),
                    bassinPersonnaliseDTO.getDimensions(),
                    accessoires,
                    accessoireImagesMap,
                    bassinPersonnaliseDTO.getDureeFabrication());

            return ResponseEntity.ok(bassinPersonnalise);
        } catch (Exception e) {
            e.printStackTrace(); // Pour le debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/imagesaccessoiresbassin/{fileName}")
    public ResponseEntity<?> deleteAccessoireImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("C:/shared/imagesaccessoiresbassin/" + fileName).normalize();
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok().body(Map.of("message", "Image supprimée avec succès"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la suppression de l'image: " + e.getMessage()));
        }
    }
    
    private String normalizePath(String path) {
        if (path == null) return null;
        // Remplacer toutes les barres obliques inversées par des barres obliques normales
        return path.replace("\\", "/");
    }

   
    
    @GetMapping("/{idBassinPersonnalise}/accessoires/images")
    public ResponseEntity<?> getAccessoireImages(@PathVariable Long idBassinPersonnalise) {
        try {
            // Récupérer le bassin personnalisé par son ID
            BassinPersonnalise bassinPersonnalise = bassinPersonnaliseRepository.findById(idBassinPersonnalise)
                    .orElseThrow(() -> new RuntimeException("Bassin personnalisé non trouvé avec l'ID : " + idBassinPersonnalise));

            // Récupérer les accessoires du bassin personnalisé
            List<Accessoire> accessoires = bassinPersonnalise.getAccessoires();

            // Extraire les chemins des images des accessoires
            List<String> imagePaths = accessoires.stream()
                    .map(Accessoire::getImagePath)
                    .collect(Collectors.toList());

            // Retourner les chemins des images
            return ResponseEntity.ok(imagePaths);
        } catch (Exception e) {
            // En cas d'erreur, retourner une réponse d'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des images des accessoires : " + e.getMessage());
        }
    }
    
    @GetMapping("/{idBassinPersonnalise}/accessoires")
    public ResponseEntity<List<Accessoire>> getAccessoiresByBassinPersonnaliseId(@PathVariable Long idBassinPersonnalise) {
        List<Accessoire> accessoires = bassinPersonnaliseService.getAccessoiresByBassinPersonnaliseId(idBassinPersonnalise);
        return ResponseEntity.ok(accessoires);
    }
    
    @GetMapping("/options/{idBassin}")
    public ResponseEntity<Map<String, Object>> getOptions(@PathVariable("idBassin") Long idBassin) {
        Map<String, Object> options = bassinPersonnaliseService.getOptionsForBassin(idBassin);
        return ResponseEntity.ok(options);
    }
    
    @GetMapping("/getBassinPersonnaliseByBassin/{idBassin}")
    public ResponseEntity<BassinPersonnalise> getBassinPersonnaliseByBassinId(@PathVariable("idBassin") Long idBassin) {
        BassinPersonnalise bassinPersonnalise = bassinPersonnaliseService.getBassinPersonnaliseByBassinId(idBassin);
        if (bassinPersonnalise != null) {
            return ResponseEntity.ok(bassinPersonnalise);
        } else {
        	System.out.println("probleme");
            return ResponseEntity.noContent().build();
        }
    }
    
    private final Path rootLocation = Paths.get("C:/shared/imagesaccessoiresbassin");

    @GetMapping("/imagesaccessoiresbassin/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable("filename") String filename) {  // Notez l'ajout de ("filename")
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = determineContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
    @PostMapping("/accessoires/by-ids")
    public ResponseEntity<List<Accessoire>> getAccessoiresByIds(@RequestBody List<Long> accessoireIds) {
        List<Accessoire> accessoires = accessoireRepository.findAllById(accessoireIds);
        return ResponseEntity.ok(accessoires);
    }
    @GetMapping("/accessoires/{id}")
    public ResponseEntity<AccessoireDTO> getAccessoireDetails(@PathVariable Long id) {
        try {
            Accessoire accessoire = accessoireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accessoire not found"));
            
            AccessoireDTO dto = new AccessoireDTO();
            dto.setIdAccessoire(accessoire.getIdAccessoire());
            dto.setNomAccessoire(accessoire.getNomAccessoire());
            dto.setPrixAccessoire(accessoire.getPrixAccessoire());
            dto.setImagePath(accessoire.getImagePath());
            // Set other fields as needed
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}