package com.example.demo.api.service.questionnaire;

import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.dto.QuestionnaireCreateDto;
import com.example.demo.api.dto.QuestionnaireResponseDto;
import com.example.demo.api.dto.UserListItemDto;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.store.entities.QuestionnaireEntity;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.QuestionnaireRepository;
import com.example.demo.store.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionnaireServiceImpl implements QuestionnaireService {

    private final QuestionnaireRepository repository;

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createQuestionnaire(QuestionnaireCreateDto dto) {
        save(CheckAuthUtil.getAuthUser(), dto);
    }

    @Override
    @Transactional
    public void createQuestionnaireForUser(Long userId, QuestionnaireCreateDto dto) {
        UserEntity user = userRepository.findById(userId)
                .filter(u -> u.getRole() == Roles.USER)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        save(user, dto);
    }

    @Transactional(readOnly = true)
    @Override
    public QuestionnaireResponseDto getQuestionnaireByUserId(Long userId) {
        QuestionnaireEntity questionnaire = repository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("Анкета не найдена"));

        return new QuestionnaireResponseDto(
                questionnaire.getId(),
                questionnaire.getUser().getId(),
                questionnaire.getSchemaVersion(),
                questionnaire.getData(),
                questionnaire.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public QuestionnaireResponseDto getMyQuestionnaire() {
        return getQuestionnaireByUserId(CheckAuthUtil.getAuthUser().getId());
    }

    //TODO: Сделать тесты
    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserListItemDto> getUsersWithQuestionnaire(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(
                repository.findUsersWithQuestionnaire(Roles.USER, pageable)
        );
    }

    private void save(UserEntity user, QuestionnaireCreateDto dto) {
        QuestionnaireEntity questionnaire = QuestionnaireEntity.builder()
                .user(user)
                .schemaVersion(dto.schemaVersion())
                .data(buildData(dto))
                .build();

        repository.save(questionnaire);
    }

    private Map<String, Object> buildData(QuestionnaireCreateDto dto) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("request", dto.request());
        data.put("goals", dto.goals());
        data.put("wellbeing", dto.wellbeing());
        data.put("restrictions", dto.restrictions());
        if (dto.extra() != null) {
            data.putAll(dto.extra());
        }
        return data;
    }
}
