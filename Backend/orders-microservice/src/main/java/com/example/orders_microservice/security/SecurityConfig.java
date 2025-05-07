package com.example.orders_microservice.security;

import java.io.*;
import java.util.*;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled= true)
public class SecurityConfig {

    @Bean
    public JWTAuthorizationFilter jwtAuthorizationFilter() {
        return new JWTAuthorizationFilter();
    }

   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
    /*            // Public endpoints
                .requestMatchers("/api/panier/**").permitAll()
                .requestMatchers("/api/panier/items").permitAll()
                .requestMatchers("/api/panier/items/{itemId}").permitAll()
                .requestMatchers("/api/panier/user/{user_id}").permitAll()
                .requestMatchers("/api/panier/user/{user_id}/add").permitAll()
                .requestMatchers("/api/panier/migrate").permitAll()
                .requestMatchers("/api/panier/items/**").permitAll()
                
                // HTTP method specific permissions
                .requestMatchers(HttpMethod.GET, "/api/panier/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/panier/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/panier/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/panier/**").permitAll()
                
                // Admin only endpoint
                .requestMatchers("/api/non-archives").hasAuthority("ADMIN")
                
                // Commande endpoints
                .requestMatchers("/api/panier/commandes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/panier/commandes/pour-installation").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/panier/commandes/en-installation").permitAll()
             */  
                /*********Hana*********/
                
                .requestMatchers("/api/panier/**").permitAll()
                .requestMatchers("/api/panier/commandes/**").permitAll()   
                // Payment endpoints
                .requestMatchers("/api/panier/payments/methods").permitAll()
                .requestMatchers("/api/panier/payments/initiate").permitAll()
                .requestMatchers("/api/panier/payments/verify-code").permitAll()
                .requestMatchers("/api/panier/payments/resend-code").permitAll()
                
                // For non-archived items
                .requestMatchers("/api/panier/non-archives").hasAuthority("ADMIN")
               
                /*********Hana**********/
                
                
                
                
                // Actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
    
    ///////////////**************///////////////

   /* @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }*/
    
    ///////////////**************///////////////
    
    /*@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "X-Session-ID"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Session-ID"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }*/
}