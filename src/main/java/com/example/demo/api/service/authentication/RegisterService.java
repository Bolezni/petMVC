package com.example.demo.api.service.authentication;

import com.example.demo.api.dto.RegisterRequest;

public interface RegisterService {
    void register(RegisterRequest request);
    void registerSpecialist(RegisterRequest request);
}
