package com.example.demo.web;

import com.example.demo.api.service.media.MediaService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/profile/avatar")
@RequiredArgsConstructor
public class AvatarPageController {

    private final MediaService mediaService;

    @PostMapping
    public String upload(@RequestParam("avatar") MultipartFile file,
                         RedirectAttributes flash) {
        try {
            mediaService.setAvatar(file);
            flash.addFlashAttribute("successMessage", "Аватар обновлён");
        } catch (BadRequestException e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/app/profile";
    }

    @PostMapping("/delete")
    public String delete(RedirectAttributes flash) {
        mediaService.removeAvatar();
        flash.addFlashAttribute("successMessage", "Аватар удалён");
        return "redirect:/app/profile";
    }
}
