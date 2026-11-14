package com.example.demo.web;

import com.example.demo.api.dto.CompletionResponse;
import com.example.demo.api.dto.MediaResponseDto;
import com.example.demo.api.dto.PageResponse;
import com.example.demo.api.dto.ProgramResponse;
import com.example.demo.api.dto.ProgramSummaryResponse;
import com.example.demo.api.dto.QuestionnaireCreateDto;
import com.example.demo.api.dto.QuestionnaireResponseDto;
import com.example.demo.api.exception.exeptions.NotFoundException;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.service.complete.CompletionService;
import com.example.demo.api.service.media.MediaService;
import com.example.demo.api.service.program.ProgramService;
import com.example.demo.api.service.questionnaire.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppController {

    private final QuestionnaireService questionnaireService;
    private final ProgramService programService;
    private final CompletionService completionService;
    private final MediaService mediaService;

    @GetMapping
    public String dashboard(Model model) {
        QuestionnaireResponseDto questionnaire = null;
        try {
            questionnaire = questionnaireService.getMyQuestionnaire();
        } catch (NotFoundException ignored) {
            // анкета ещё не заполнена — это нормальное состояние
        }

        List<ProgramSummaryResponse> programs = programService.myPrograms();
        List<MediaResponseDto> media = mediaService.getByUser(CheckAuthUtil.getAuthUser().getId());

        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("programs", programs);
        model.addAttribute("media", media);
        return "app/dashboard";
    }

    @GetMapping("/questionnaire")
    public String questionnaireForm() {
        return "app/questionnaire";
    }

    @PostMapping("/questionnaire")
    public String saveQuestionnaire(@RequestParam String request,
                                    @RequestParam(required = false) String goals,
                                    @RequestParam(required = false) String wellbeing,
                                    @RequestParam(required = false) String restrictions) {
        QuestionnaireCreateDto dto = new QuestionnaireCreateDto(
                1,
                request.trim(),
                lines(goals),
                blankToNull(wellbeing),
                lines(restrictions),
                null);

        questionnaireService.createQuestionnaire(dto);
        return "redirect:/app";
    }

    @GetMapping("/media")
    public String media(Model model) {
        model.addAttribute("media", mediaService.getByUser(CheckAuthUtil.getAuthUser().getId()));
        return "app/media";
    }

    @PostMapping("/media")
    public String uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            mediaService.upload(file);
        } catch (RuntimeException ex) {
            return "redirect:/app/media?error";
        }
        return "redirect:/app/media?uploaded";
    }

    @PostMapping("/media/{id}/delete")
    public String deleteMedia(@PathVariable Long id) {
        try {
            mediaService.delete(id);
        } catch (RuntimeException ex) {
            return "redirect:/app/media?error=delete";
        }
        return "redirect:/app/media?deleted";
    }

    @GetMapping("/programs/{id}")
    public String program(@PathVariable Long id, Model model) {
        ProgramResponse program = programService.getMine(id);
        PageResponse<CompletionResponse> completions = completionService.listMine(id, 0, 50);

        Set<Long> completedItemIds = completions.content().stream()
                .map(CompletionResponse::programItemId)
                .collect(Collectors.toSet());

        model.addAttribute("program", program);
        model.addAttribute("completions", completions);
        model.addAttribute("completedItemIds", completedItemIds);
        return "app/program";
    }

    @PostMapping("/programs/{id}/items/{itemId}/completion")
    public String complete(@PathVariable Long id,
                           @PathVariable Long itemId,
                           @RequestParam(required = false) String feedback) {
        CompletionResponse response = completionService.complete(itemId, blankToNull(feedback));
        return "redirect:/app/programs/" + id + "?done=" + response.id();
    }

    /** Одна цель или ограничение на строку. */
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
