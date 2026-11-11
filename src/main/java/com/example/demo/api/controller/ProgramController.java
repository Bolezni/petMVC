package com.example.demo.api.controller;

import com.example.demo.api.dto.ProgramCreateRequest;
import com.example.demo.api.dto.ProgramResponse;
import com.example.demo.api.dto.ProgramStatusRequest;
import com.example.demo.api.dto.ProgramSummaryResponse;
import com.example.demo.api.service.program.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @PostMapping("/specialist/programs")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgramResponse create(@Valid @RequestBody ProgramCreateRequest request) {
        return programService.create(request);
    }

    @GetMapping("/specialist/programs/{id}")
    public ProgramResponse getForSpecialist(@PathVariable Long id) {
        return programService.getForSpecialist(id);
    }

    @GetMapping("/specialist/users/{userId}/programs")
    public List<ProgramSummaryResponse> listByUser(@PathVariable Long userId) {
        return programService.listByUser(userId);
    }

    @PatchMapping("/specialist/programs/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@PathVariable Long id, @Valid @RequestBody ProgramStatusRequest request) {
        programService.changeStatus(id, request.status());
    }
}