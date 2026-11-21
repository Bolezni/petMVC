package com.example.demo.api.controller;

import com.example.demo.api.dto.*;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.service.complete.CompletionService;
import com.example.demo.api.service.program.ProgramService;
import com.example.demo.api.service.questionnaire.QuestionnaireService;
import com.example.demo.api.service.user.UserService;
import com.example.demo.api.security.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final QuestionnaireService service;

    private final ProgramService programService;

    private final CompletionService completionService;

    private final UserService userService;

    @GetMapping("/profile")
    public UserDto myProfile() {
        return userService.getMyProfile();
    }

    @PutMapping("/profile")
    public UserDto updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                 @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(currentUser.user(), request);
    }

    @PostMapping("/questionnaire")
    public ResponseEntity<?> buildQuestionnaire(@Valid @RequestBody QuestionnaireCreateDto request) {
        service.createQuestionnaire(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/questionnaire")
    public QuestionnaireResponseDto myQuestionnaire() {
        return service.getMyQuestionnaire();
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
