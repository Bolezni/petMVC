package com.example.demo.api.controller;

import com.example.demo.api.dto.RegisterRequest;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.service.authentication.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RegisterService registerService;

    @Value("${app.admin-key:}")
    private String adminKey;

    @PostMapping("/specialists")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSpecialist(
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @Valid @RequestBody RegisterRequest request) {


        boolean allowed = !adminKey.isBlank() && key != null
                && MessageDigest.isEqual(
                adminKey.getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8));

        if (!allowed) {
            throw new NotFoundException("Not found");
        }
        registerService.registerSpecialist(request);
    }
}