package com.example.demo.api.controller;


import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.dto.QuestionnaireCreateDto;
import com.example.demo.api.dto.QuestionnaireResponseDto;
import com.example.demo.api.dto.UserListItemDto;
import com.example.demo.api.service.questionnaire.QuestionnaireService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/specialist")
public class SpecialistController {

    private final QuestionnaireService service;

    /**
     * Анкета заполняется на конкретного пользователя, а не на текущего специалиста.
     * Нужно для сценария, когда специалист помогает заполнить анкету на созвоне.
     */
    @PostMapping("/users/{userId}/questionnaires")
    public ResponseEntity<Void> createQuestionnaire(@PathVariable Long userId,
                                                    @Valid @RequestBody QuestionnaireCreateDto dto){
        service.createQuestionnaireForUser(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/questionnaires/{userId}")
    public ResponseEntity<QuestionnaireResponseDto> getQuestionnairesUserById(@PathVariable("userId") String userId) {
        QuestionnaireResponseDto response = service.getQuestionnaireByUserId(Long.valueOf(userId));
        return ResponseEntity.ok(response);
    }

    //TODO: Сделать тесты
    @GetMapping("/users")
    public PageResponse<UserListItemDto> getUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.getUsersWithQuestionnaire(page, size);
    }
}
