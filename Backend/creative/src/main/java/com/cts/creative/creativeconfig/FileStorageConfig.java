package com.cts.creative.creativeconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    @Value("${creative.upload.path}")
    private String uploadPath;

    public String getUploadPath() {
        return uploadPath;
    }
}