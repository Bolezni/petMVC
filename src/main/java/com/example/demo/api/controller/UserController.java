package com.example.demo.api.controller;

import com.example.demo.api.dto.*;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.service.complete.CompletionService;
import com.example.demo.api.service.media.MediaService;
import com.example.demo.api.service.program.ProgramService;
import com.example.demo.api.service.questionnaire.QuestionnaireService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final QuestionnaireService service;

    private final MediaService mediaService;

    private final ProgramService programService;

    private final CompletionService completionService;

    @PostMapping("/questionnaire")
    public ResponseEntity<?> buildQuestionnaire(@Valid @RequestBody QuestionnaireCreateDto request) {
        service.createQuestionnaire(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaResponseDto upload(@RequestParam("file") MultipartFile file) {
        return mediaService.upload(file);
    }

    @GetMapping("/media")
    public List<MediaResponseDto> myMedia() {
        return mediaService.getByUser(CheckAuthUtil.getAuthUser().getId());
    }


    @GetMapping("/programs")
    public List<ProgramSummaryResponse> myPrograms() {
        return programService.myPrograms();
    }

    @GetMapping("/programs/{id}")
    public ProgramResponse getMine(@PathVariable Long id) {
        return programService.getMine(id);
    }


    @PostMapping("/program-items/{itemId}/completions")
    @ResponseStatus(HttpStatus.CREATED)
    public CompletionResponse complete(@PathVariable Long itemId,
                                       @Valid @RequestBody(required = false) CompletionRequest request) {
        String feedback = request == null ? null : request.feedback();
        return completionService.complete(itemId, feedback);
    }

    @GetMapping("/programs/{programId}/completions")
    public PageResponse<CompletionResponse> mine(
            @PathVariable Long programId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return completionService.listMine(programId, page, size);
    }
}
