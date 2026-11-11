package com.example.demo.api.service.complete;

import com.example.demo.api.dto.CompletionResponse;
import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.store.entities.*;
import com.example.demo.store.repo.ProgramCompletionRepository;
import com.example.demo.store.repo.ProgramItemRepository;
import com.example.demo.store.repo.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletionServiceImpl implements CompletionService {

    private final ProgramCompletionRepository completionRepository;
    private final ProgramItemRepository itemRepository;
    private final ProgramRepository programRepository;

    @Transactional
    @Override
    public CompletionResponse complete(Long itemId, String feedback) {
        UserEntity user = CheckAuthUtil.getAuthUser();

        ProgramItemEntity item = itemRepository.findByIdWithProgram(itemId)
                .orElseThrow(() -> new NotFoundException("Упражнение в программе не найдено"));

        ProgramEntity program = item.getProgram();
        if (!program.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Упражнение в программе не найдено");   // чужое: 404
        }
        if (program.getStatus() != ProgramStatus.ACTIVE) {
            throw new RuntimeException("Программа не активна");
        }

        ProgramCompletionEntity completion = new ProgramCompletionEntity();
        completion.setProgramItem(item);
        completion.setUser(user);
        completion.setFeedback(feedback == null || feedback.isBlank() ? null : feedback.trim());

        return CompletionResponse.from(completionRepository.save(completion));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<CompletionResponse> listForSpecialist(Long programId, int page, int size) {
        if (!programRepository.existsById(programId)) {
            throw new NotFoundException("Программа не найдена");
        }
        return load(programId, page, size);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<CompletionResponse> listMine(Long programId, int page, int size) {
        Long userId = CheckAuthUtil.getAuthUser().getId();
        ProgramEntity program = programRepository.findById(programId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Программа не найдена"));
        return load(program.getId(), page, size);
    }

    private PageResponse<CompletionResponse> load(Long programId, int page, int size) {
        return PageResponse.from(
                completionRepository.findByProgramId(programId, PageRequest.of(page, size))
                        .map(CompletionResponse::from));
    }
}
