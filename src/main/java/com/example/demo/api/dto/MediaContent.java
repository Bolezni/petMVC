package com.example.demo.api.dto;

import org.springframework.core.io.Resource;

public record MediaContent(Resource resource, String contentType) {}
