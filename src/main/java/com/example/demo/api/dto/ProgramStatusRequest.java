package com.example.demo.api.dto;

import com.example.demo.store.entities.ProgramStatus;
import jakarta.validation.constraints.NotNull;

public record ProgramStatusRequest(@NotNull ProgramStatus status) {
}
