package com.fitcheck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;

// 项目使用自定义 JWT 认证，完全禁用 Spring Security 的默认配置
// 避免 Security 拦截请求并重定向到默认 /login 页
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@MapperScan("com.fitcheck.mapper")
public class FitCheckApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitCheckApplication.class, args);
    }
}
