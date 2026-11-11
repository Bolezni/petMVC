package com.example.demo.api.service.complete;

import com.example.demo.api.dto.CompletionResponse;
import com.example.demo.api.dto.PageResponse;

public interface CompletionService {
    CompletionResponse complete(Long itemId, String feedback);

    PageResponse<CompletionResponse> listForSpecialist(Long programId, int page, int size);

    PageResponse<CompletionResponse> listMine(Long programId, int page, int size);
}
