package com.example.installations_microservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.installations_microservice.clients.dtos.UserDto;
import java.util.List;

@FeignClient(name = "users-microservice", url = "http://localhost:8087")
public interface UserServiceClient {
    //    @GetMapping("/api/users/installateursCommmande")

    @GetMapping("/api/users/installateursCommmande")
    List<UserDto> getInstallateursCommmande();
}