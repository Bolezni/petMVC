package com.example.demo.api.controller;

import com.example.demo.api.dto.AuthenticationRequest;
import com.example.demo.api.dto.AuthenticationResponse;
import com.example.demo.api.dto.RefreshRequest;
import com.example.demo.api.dto.RegisterRequest;
import com.example.demo.api.service.authentication.AuthenticationService;
import com.example.demo.api.service.authentication.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        registerService.register(request);
        return ResponseEntity.ok().build();
    }
    //Исправить дыру с Role

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request){
        AuthenticationResponse response = authenticationService.authentication(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshRequest request){
        return ResponseEntity.ok(authenticationService.refresh(request.refreshToken()));
    }
}
