package com.example.demo.api.service.program;

import com.example.demo.api.dto.ProgramCreateRequest;
import com.example.demo.api.dto.ProgramItemRequest;
import com.example.demo.api.dto.ProgramResponse;
import com.example.demo.api.dto.ProgramSummaryResponse;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.store.entities.*;
import com.example.demo.store.repo.ExerciseRepository;
import com.example.demo.store.repo.ProgramRepository;
import com.example.demo.store.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    @Override
    public ProgramResponse create(ProgramCreateRequest request) {
        UserEntity specialist = CheckAuthUtil.getAuthUser();

        UserEntity user = userRepository.findById(request.userId())
                .filter(u -> u.getRole() == Roles.USER)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Long> ids = request.items().stream()
                .map(ProgramItemRequest::exerciseId).distinct().toList();

        Map<Long, ExerciseEntity> exercises = exerciseRepository.findAllByIdInAndActiveTrue(ids)
                .stream().collect(Collectors.toMap(ExerciseEntity::getId, Function.identity()));

        List<Long> missing = ids.stream().filter(id -> !exercises.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            throw new RuntimeException("Упражнения не найдены: " + missing);
        }

        ProgramEntity program = new ProgramEntity();
        program.setUser(user);
        program.setSpecialist(specialist);
        program.setTitle(request.title().trim());
        program.setSpecialistComment(request.specialistComment());
        program.setPriorities(request.priorities());

        List<ProgramItemRequest> items = request.items();
        for (int i = 0; i < items.size(); i++) {
            ProgramItemRequest item = items.get(i);
            program.addItem(exercises.get(item.exerciseId()),
                    item.sets(), item.reps(), item.note(), i);
        }

        return ProgramResponse.from(programRepository.save(program));
    }

    @Transactional(readOnly = true)
    @Override
    public ProgramResponse getForSpecialist(Long id) {
        return ProgramResponse.from(findWithItems(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProgramSummaryResponse> listByUser(Long userId) {
        return programRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ProgramSummaryResponse::from).toList();
    }

    @Transactional
    @Override
    public void changeStatus(Long id, ProgramStatus status) {
        ProgramEntity program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Программа не найдена"));
        program.setStatus(status);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProgramSummaryResponse> myPrograms() {
        Long userId = CheckAuthUtil.getAuthUser().getId();
        return programRepository
                .findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, ProgramStatus.ARCHIVED)
                .stream().map(ProgramSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ProgramResponse getMine(Long id) {
        Long userId = CheckAuthUtil.getAuthUser().getId();
        ProgramEntity program = findWithItems(id);

        boolean mine = program.getUser().getId().equals(userId);
        if (!mine || program.getStatus() == ProgramStatus.ARCHIVED) {
            throw new NotFoundException("Программа не найдена");
        }
        return ProgramResponse.from(program);
    }

    private ProgramEntity findWithItems(Long id) {
        return programRepository.findByIdWithItems(id)
                .orElseThrow(() -> new NotFoundException("Программа не найдена"));
    }
}