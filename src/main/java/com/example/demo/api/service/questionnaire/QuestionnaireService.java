package com.example.demo.api.service.questionnaire;

import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.dto.QuestionnaireCreateDto;
import com.example.demo.api.dto.QuestionnaireResponseDto;
import com.example.demo.api.dto.UserListItemDto;

public interface QuestionnaireService {
    void createQuestionnaire(QuestionnaireCreateDto dto);

    QuestionnaireResponseDto getQuestionnaireByUserId(Long userId);

    PageResponse<UserListItemDto> getUsersWithQuestionnaire(int page, int size);
}
