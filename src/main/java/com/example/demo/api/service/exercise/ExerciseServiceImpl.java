package com.example.demo.api.service.exercise;

import com.example.demo.api.dto.ExerciseRequest;
import com.example.demo.api.dto.ExerciseResponse;
import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.store.entities.ExerciseEntity;
import com.example.demo.store.repo.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository repository;

    @Transactional
    @Override
    public ExerciseResponse create(ExerciseRequest request) {
        ExerciseEntity exercise = new ExerciseEntity();
        apply(exercise, request);
        exercise.setCreatedBy(CheckAuthUtil.getAuthUser());
        return ExerciseResponse.from(repository.save(exercise));
    }

    @Transactional
    @Override
    public ExerciseResponse update(Long id, ExerciseRequest request) {
        ExerciseEntity exercise = findActive(id);
        apply(exercise, request);
        return ExerciseResponse.from(exercise);   // dirty checking сохранит сам
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        findActive(id).setActive(false);
    }

    @Transactional(readOnly = true)
    @Override
    public ExerciseResponse get(Long id) {
        return ExerciseResponse.from(findActive(id));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<ExerciseResponse> search(String q, String muscleGroup, int page, int size) {
        Specification<ExerciseEntity> spec = (root, query, cb) -> cb.isTrue(root.get("active"));

        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }
        if (muscleGroup != null && !muscleGroup.isBlank()) {
            String group = muscleGroup.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("muscleGroup"), group));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return PageResponse.from(repository.findAll(spec, pageable).map(ExerciseResponse::from));
    }

    private ExerciseEntity findActive(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Упражнение не найдено"));
    }

    private void apply(ExerciseEntity e, ExerciseRequest r) {
        if (r.name() != null)        e.setName(r.name().trim());
        if (r.muscleGroup() != null) e.setMuscleGroup(r.muscleGroup().trim());
        if (r.difficulty() != null)  e.setDifficulty(r.difficulty());
        if (r.description() != null) e.setDescription(blankToNull(r.description()));
        if (r.videoUrl() != null)    e.setVideoUrl(blankToNull(r.videoUrl()));
    }

    private static String blankToNull(String s) {
        return s.isBlank() ? null : s.trim();
    }
}
