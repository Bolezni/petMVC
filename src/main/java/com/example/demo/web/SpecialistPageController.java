package com.example.demo.web;

import com.example.demo.api.dto.CompletionResponse;
import com.example.demo.api.dto.ExerciseRequest;
import com.example.demo.api.dto.ExerciseResponse;
import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.dto.ProgramCreateRequest;
import com.example.demo.api.dto.ProgramItemRequest;
import com.example.demo.api.dto.ProgramItemResponse;
import com.example.demo.api.dto.ProgramResponse;
import com.example.demo.api.dto.ProgramUpdateRequest;
import com.example.demo.api.dto.QuestionnaireResponseDto;
import com.example.demo.api.dto.UserListItemDto;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.MediaUrlSigner;
import com.example.demo.api.service.complete.CompletionService;
import com.example.demo.api.service.exercise.ExerciseService;
import com.example.demo.api.service.media.MediaService;
import com.example.demo.api.service.program.ProgramService;
import com.example.demo.api.service.questionnaire.QuestionnaireService;
import com.example.demo.store.entities.Difficulty;
import com.example.demo.store.entities.ProgramStatus;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/specialist")
@RequiredArgsConstructor
public class SpecialistPageController {

    /** Сколько пустых строк упражнений показываем в форме. */
    private static final int VISIBLE_ROWS = 8;

    /** Верхняя граница при разборе формы: совпадает с ограничением API. */
    private static final int MAX_ROWS = 30;

    private final QuestionnaireService questionnaireService;
    private final ProgramService programService;
    private final CompletionService completionService;
    private final ExerciseService exerciseService;
    private final MediaService mediaService;
    private final MediaUrlSigner mediaUrlSigner;
    private final UserRepository userRepository;

    /** Одна строка формы программы: пустая или с текущими значениями. */
    public record ProgramRow(int index, Long exerciseId, Integer sets, Integer reps, String note) {
    }

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
        model.addAttribute("programs", programService.listByUser(id));
        return "specialist/user";
    }

    // ---------- программы ----------

    @GetMapping("/users/{id}/programs/new")
    public String newProgram(@PathVariable Long id, Model model) {
        model.addAttribute("userId", id);
        model.addAttribute("exercises", exerciseService.search(null, null, 0, 100).content());
        model.addAttribute("rows", emptyRows());
        return "specialist/program-new";
    }

    @PostMapping("/users/{id}/programs")
    public String createProgram(@PathVariable Long id,
                                @RequestParam String title,
                                @RequestParam(required = false) String comment,
                                @RequestParam(required = false) String priorities,
                                @RequestParam Map<String, String> form) {

        List<ProgramItemRequest> items = parseItems(form);
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

    @GetMapping("/programs/{id}/edit")
    public String editProgram(@PathVariable Long id, Model model) {
        ProgramResponse program = programService.getForSpecialist(id);

        model.addAttribute("program", program);
        model.addAttribute("exercises", exerciseService.search(null, null, 0, 100).content());
        model.addAttribute("rows", rowsFromProgram(program));
        model.addAttribute("prioritiesText", program.priorities() == null
                ? "" : String.join("\n", program.priorities()));
        return "specialist/program-edit";
    }

    @PostMapping("/programs/{id}")
    public String updateProgram(@PathVariable Long id,
                                @RequestParam String title,
                                @RequestParam(required = false) String comment,
                                @RequestParam(required = false) String priorities,
                                @RequestParam Map<String, String> form) {

        List<ProgramItemRequest> items = parseItems(form);
        if (items.isEmpty()) {
            return "redirect:/specialist/programs/" + id + "/edit?empty";
        }

        try {
            programService.update(id, new ProgramUpdateRequest(
                    title.trim(), blankToNull(comment), lines(priorities), items));
            return "redirect:/specialist/programs/" + id;
        } catch (RuntimeException ex) {
            log.warn("Не удалось обновить программу {}: {}", id, ex.getMessage());
            return "redirect:/specialist/programs/" + id + "/edit?error";
        }
    }

    /** Единственный способ перевести программу в «завершена» или «в архиве». */
    @PostMapping("/programs/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam ProgramStatus status) {
        programService.changeStatus(id, status);
        return "redirect:/specialist/programs/" + id;
    }

    @GetMapping("/programs/{id}")
    public String program(@PathVariable Long id, Model model) {
        ProgramResponse program = programService.getForSpecialist(id);
        PageResponse<CompletionResponse> completions = completionService.listForSpecialist(id, 0, 50);

        model.addAttribute("program", program);
        model.addAttribute("completions", completions);
        model.addAttribute("statuses", ProgramStatus.values());
        return "specialist/program";
    }

    // ---------- библиотека упражнений ----------

    @GetMapping("/exercises")
    public String exercises(@RequestParam(required = false) String q,
                            @RequestParam(required = false) String muscleGroup,
                            Model model) {
        PageResponse<ExerciseResponse> page = exerciseService.search(
                blankToNull(q), blankToNull(muscleGroup), 0, 100);

        model.addAttribute("exercises", page.content());
        model.addAttribute("total", page.totalElements());
        model.addAttribute("q", q);
        model.addAttribute("muscleGroup", muscleGroup);
        model.addAttribute("difficulties", Difficulty.values());
        return "specialist/exercises";
    }

    @PostMapping("/exercises")
    public String createExercise(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam String muscleGroup,
                                 @RequestParam(required = false) Difficulty difficulty,
                                 @RequestParam(required = false) String videoUrl,
                                 @RequestParam(required = false) MultipartFile video) {
        ExerciseResponse created;
        try {
            created = exerciseService.create(new ExerciseRequest(name.trim(), blankToNull(description),
                    muscleGroup.trim(), difficulty, blankToNull(videoUrl)));
        } catch (RuntimeException ex) {
            log.warn("Не удалось создать упражнение: {}", ex.getMessage());
            return "redirect:/specialist/exercises?error";
        }

        if (video != null && !video.isEmpty()) {
            if (!attachVideo(created.id(), video)) {
                return "redirect:/specialist/exercises?videoError";
            }
        }

        return "redirect:/specialist/exercises?created";
    }

    @GetMapping("/exercises/{id}/edit")
    public String editExercise(@PathVariable Long id, Model model) {
        ExerciseResponse exercise = exerciseService.get(id);

        model.addAttribute("exercise", exercise);
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("videoUrl", exercise.videoMediaId() == null
                ? null : mediaUrlSigner.url(exercise.videoMediaId()));
        return "specialist/exercise-edit";
    }

    @PostMapping("/exercises/{id}")
    public String updateExercise(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam String muscleGroup,
                                 @RequestParam(required = false) Difficulty difficulty,
                                 @RequestParam(required = false) String videoUrl,
                                 @RequestParam(required = false) MultipartFile video,
                                 @RequestParam(defaultValue = "false") boolean removeVideo) {
        try {
            exerciseService.update(id, new ExerciseRequest(name.trim(), blankToNull(description),
                    muscleGroup.trim(), difficulty, blankToNull(videoUrl)));
        } catch (RuntimeException ex) {
            log.warn("Не удалось обновить упражнение {}: {}", id, ex.getMessage());
            return "redirect:/specialist/exercises/" + id + "/edit?error";
        }

        if (video != null && !video.isEmpty()) {
            if (!attachVideo(id, video)) {
                return "redirect:/specialist/exercises/" + id + "/edit?videoError";
            }
        } else if (removeVideo) {
            removeVideo(id);
        }

        return "redirect:/specialist/exercises";
    }

    @PostMapping("/exercises/{id}/deactivate")
    public String deactivateExercise(@PathVariable Long id) {
        exerciseService.deactivate(id);
        return "redirect:/specialist/exercises";
    }

    // ---------- вспомогательное ----------

    private boolean attachVideo(Long exerciseId, MultipartFile file) {
        try {
            Long mediaId = mediaService.upload(file).id();
            exerciseService.attachVideo(exerciseId, mediaId);
            return true;
        } catch (RuntimeException ex) {
            log.warn("Не удалось загрузить видео для упражнения {}: {}", exerciseId, ex.getMessage());
            return false;
        }
    }

    private void removeVideo(Long exerciseId) {
        ExerciseResponse current = exerciseService.get(exerciseId);
        exerciseService.detachVideo(exerciseId);

        if (current.videoMediaId() != null) {
            try {
                mediaService.delete(current.videoMediaId());
            } catch (RuntimeException ex) {
                log.warn("Видео {} отвязано, но файл не удалён: {}",
                        current.videoMediaId(), ex.getMessage());
            }
        }
    }

    private List<ProgramRow> emptyRows() {
        List<ProgramRow> rows = new ArrayList<>();
        for (int i = 1; i <= VISIBLE_ROWS; i++) {
            rows.add(new ProgramRow(i, null, null, null, null));
        }
        return rows;
    }

    private List<ProgramRow> rowsFromProgram(ProgramResponse program) {
        List<ProgramItemResponse> items = program.items();
        int size = Math.max(VISIBLE_ROWS, items.size());

        List<ProgramRow> rows = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i < items.size()) {
                ProgramItemResponse item = items.get(i);
                rows.add(new ProgramRow(i + 1, item.exerciseId(), item.sets(), item.reps(), item.note()));
            } else {
                rows.add(new ProgramRow(i + 1, null, null, null, null));
            }
        }
        return rows;
    }

    private List<ProgramItemRequest> parseItems(Map<String, String> form) {
        List<ProgramItemRequest> items = new ArrayList<>();
        for (int row = 1; row <= MAX_ROWS; row++) {
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
        return items;
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
