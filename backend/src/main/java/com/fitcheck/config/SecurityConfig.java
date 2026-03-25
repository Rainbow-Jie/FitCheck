package com.fitcheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全配置 - 仅提供密码编码器
 * 注意：项目已禁用 Spring Security 自动配置，使用自定义 JWT 认证
 * BCryptPasswordEncoder 来自 spring-security-crypto，不依赖 Security 自动配置
 */
@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
