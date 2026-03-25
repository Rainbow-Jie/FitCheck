package com.fitcheck.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);
    
    @Value("${upload.path:./uploads}")
    private String uploadPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadDir = new File(uploadPath);
        String absolutePath = uploadDir.getAbsolutePath().replace("\\", "/");
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        String location = "file:///" + absolutePath + "avatar/";

        log.info("=== 静态资源映射 /avatar/** -> {}", location);
        log.info("=== user.dir = {}", System.getProperty("user.dir"));
        log.info("=== 目录是否存在: {}", new File(uploadDir, "avatar").exists());

        registry.addResourceHandler("/avatar/**")
                .addResourceLocations(location);
    }
}
