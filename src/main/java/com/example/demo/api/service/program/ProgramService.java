package com.example.demo.api.service.program;

import com.example.demo.api.dto.ProgramCreateRequest;
import com.example.demo.api.dto.ProgramResponse;
import com.example.demo.api.dto.ProgramSummaryResponse;
import com.example.demo.store.entities.ProgramStatus;

import java.util.List;

public interface ProgramService {
    ProgramResponse create(ProgramCreateRequest request);

    ProgramResponse getForSpecialist(Long id);

    List<ProgramSummaryResponse> listByUser(Long userId);

    void changeStatus(Long id, ProgramStatus status);

    List<ProgramSummaryResponse> myPrograms();

    ProgramResponse getMine(Long id);
}
