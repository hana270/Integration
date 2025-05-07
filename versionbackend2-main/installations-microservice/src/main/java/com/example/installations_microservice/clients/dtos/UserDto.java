package com.example.installations_microservice.clients.dtos;

import lombok.Data;

@Data
public class UserDto {
    private Long user_id;
   // private String nom;
   // private String prenom;
    private String email;
    private String username;
}