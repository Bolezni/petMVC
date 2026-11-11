package com.example.demo.api.service.media;

import com.example.demo.api.dto.MediaContent;
import com.example.demo.api.dto.MediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {
    MediaResponseDto upload(MultipartFile file);

    List<MediaResponseDto> getByUser(Long userId);

    MediaContent getContent(Long id);


}
