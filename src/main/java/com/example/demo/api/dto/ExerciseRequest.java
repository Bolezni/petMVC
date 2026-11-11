package com.example.demo.api.dto;

import com.example.demo.store.entities.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExerciseRequest(
        @NotBlank(message = "Название не может быть пустым")
        @Size(max = 255, message = "Название не длиннее 255 символов")
        String name,

        @Size(max = 5000, message = "Описание не длиннее 5000 символов")
        String description,

        @NotBlank(message = "Укажите группу мышц")
        @Size(max = 100)
        String muscleGroup,

        Difficulty difficulty,

        @Size(max = 512)
        @Pattern(regexp = "^https://\\S+$", message = "Ссылка на видео должна начинаться с https://")
        String videoUrl
) {}
