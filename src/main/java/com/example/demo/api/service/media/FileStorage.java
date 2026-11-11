package com.example.demo.api.service.media;


import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorage {
    void save(String key, InputStream in) throws IOException;

    Resource load(String key);

    void delete(String key);
}
