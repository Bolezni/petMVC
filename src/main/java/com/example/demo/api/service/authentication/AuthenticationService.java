package com.example.demo.api.service.authentication;

import com.example.demo.api.dto.AuthenticationRequest;
import com.example.demo.api.dto.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authentication(AuthenticationRequest request);
}
