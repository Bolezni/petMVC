package com.example.demo.web;

import com.example.demo.api.dto.CompletionResponse;
import com.example.demo.api.dto.ExerciseRequest;
import com.example.demo.api.dto.ExerciseResponse;
import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.dto.ProgramCreateRequest;
import com.example.demo.api.dto.ProgramItemRequest;
import com.example.demo.api.dto.ProgramResponse;
import com.example.demo.api.dto.ProgramSummaryResponse;
import com.example.demo.api.dto.QuestionnaireResponseDto;
import com.example.demo.api.dto.UserListItemDto;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.service.complete.CompletionService;
import com.example.demo.api.service.exercise.ExerciseService;
import com.example.demo.api.service.media.MediaService;
import com.example.demo.api.service.program.ProgramService;
import com.example.demo.api.service.questionnaire.QuestionnaireService;
import com.example.demo.store.entities.Difficulty;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/specialist")
@RequiredArgsConstructor
public class SpecialistPageController {

    /** Столько строк упражнений показываем в форме программы. */
    private static final int PROGRAM_ROWS = 8;

    private final QuestionnaireService questionnaireService;
    private final ProgramService programService;
    private final CompletionService completionService;
    private final MediaService mediaService;
    private final ExerciseService exerciseService;
    private final UserRepository userRepository;

    @GetMapping
    public String users(@RequestParam(defaultValue = "0") int page, Model model) {
        PageResponse<UserListItemDto> users = questionnaireService.getUsersWithQuestionnaire(page, 20);
        model.addAttribute("users", users);
        return "specialist/users";
    }

    @GetMapping("/users/{id}")
    public String user(@PathVariable Long id, Model model) {
        QuestionnaireResponseDto questionnaire = null;
        try {
            questionnaire = questionnaireService.getQuestionnaireByUserId(id);
        } catch (NotFoundException ignored) {
            // пользователь ещё не заполнял анкету
        }

        UserEntity user = userRepository.findById(id).orElse(null);

        model.addAttribute("userId", id);
        model.addAttribute("user", user);
        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("media", mediaService.getByUser(id));
        model.addAttribute("programs", programService.listByUser(id));
        return "specialist/user";
    }

    @GetMapping("/users/{id}/programs/new")
    public String newProgram(@PathVariable Long id, Model model) {
        model.addAttribute("userId", id);
        model.addAttribute("exercises", exerciseService.search(null, null, 0, 100).content());
        model.addAttribute("rows", java.util.stream.IntStream.rangeClosed(1, PROGRAM_ROWS).boxed().toList());
        return "specialist/program-new";
    }

    @PostMapping("/users/{id}/programs")
    public String createProgram(@PathVariable Long id,
                                @RequestParam String title,
                                @RequestParam(required = false) String comment,
                                @RequestParam(required = false) String priorities,
                                @RequestParam Map<String, String> form) {

        List<ProgramItemRequest> items = new ArrayList<>();
        for (int row = 1; row <= PROGRAM_ROWS; row++) {
            String exerciseId = form.get("exerciseId" + row);
            if (exerciseId == null || exerciseId.isBlank()) {
                continue;
            }
            items.add(new ProgramItemRequest(
                    Long.valueOf(exerciseId),
                    parseInteger(form.get("sets" + row)),
                    parseInteger(form.get("reps" + row)),
                    blankToNull(form.get("note" + row))));
        }

        if (items.isEmpty()) {
            return "redirect:/specialist/users/" + id + "/programs/new?empty";
        }

        try {
            ProgramResponse created = programService.create(new ProgramCreateRequest(
                    id, title.trim(), blankToNull(comment), lines(priorities), items));
            return "redirect:/specialist/programs/" + created.id();
        } catch (RuntimeException ex) {
            log.warn("Не удалось создать программу для {}: {}", id, ex.getMessage());
            return "redirect:/specialist/users/" + id + "/programs/new?error";
        }
    }

    @GetMapping("/programs/{id}")
    public String program(@PathVariable Long id, Model model) {
        ProgramResponse program = programService.getForSpecialist(id);
        PageResponse<CompletionResponse> completions = completionService.listForSpecialist(id, 0, 50);

        model.addAttribute("program", program);
        model.addAttribute("completions", completions);
        return "specialist/program";
    }

    @GetMapping("/exercises")
    public String exercises(Model model) {
        model.addAttribute("exercises", exerciseService.search(null, null, 0, 100).content());
        model.addAttribute("difficulties", Difficulty.values());
        return "specialist/exercises";
    }

    @PostMapping("/exercises")
    public String createExercise(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam String muscleGroup,
                                 @RequestParam(required = false) Difficulty difficulty,
                                 @RequestParam(required = false) String videoUrl) {
        try {
            exerciseService.create(new ExerciseRequest(name.trim(), blankToNull(description),
                    muscleGroup.trim(), difficulty, blankToNull(videoUrl)));
        } catch (RuntimeException ex) {
            log.warn("Не удалось создать упражнение: {}", ex.getMessage());
            return "redirect:/specialist/exercises?error";
        }
        return "redirect:/specialist/exercises";
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<String> lines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
