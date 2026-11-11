package com.example.demo.api.controller;

import com.example.demo.api.dto.CompletionResponse;
import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.service.complete.CompletionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class CompletionController {

    private final CompletionService completionService;

    @GetMapping("/specialist/programs/{programId}/completions")
    public PageResponse<CompletionResponse> forSpecialist(
            @PathVariable Long programId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return completionService.listForSpecialist(programId, page, size);
    }
}
