package com.fitcheck.controller;

import com.fitcheck.common.Result;
import com.fitcheck.dto.*;
import com.fitcheck.entity.User;
import com.fitcheck.service.UserService;
import com.fitcheck.service.VerificationCodeService;
import com.fitcheck.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        try {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return Result.error("两次密码输入不一致");
            }
            
            User user = userService.register(request.getUsername(), request.getPhone(), request.getPassword());
            user.setPassword(null);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request.getUsername(), request.getPassword(), request.getPhoneLast4());
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
    
    /**
     * 获取当前用户信息
     */
    @PostMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }
        
        try {
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getUserInfo(userId);
            if (user == null) {
                return Result.error(401, "用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(401, "Token无效或已过期");
        }
    }
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            // 检查用户是否存在
            User user = userService.getUserByUsername(request.getUsername());
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            verificationCodeService.generateCode(request.getUsername());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // 检查用户是否存在
            User user = userService.getUserByUsername(request.getUsername());
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 验证验证码
            if (!verificationCodeService.verifyCode(request.getUsername(), request.getCode())) {
                return Result.error("验证码错误或已过期");
            }
            
            // 重置密码
            userService.resetPassword(request.getUsername(), request.getNewPassword());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改密码（已登录）
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(HttpServletRequest httpRequest, @RequestBody ChangePasswordRequest request) {
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }
        
        try {
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getUserById(userId);
            
            // 验证旧密码
            if (!userService.verifyPassword(userId, request.getOldPassword())) {
                return Result.error("旧密码错误");
            }
            
            // 检查新密码和确认密码
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return Result.error("两次密码输入不一致");
            }
            
            // 更新密码
            userService.updatePassword(userId, request.getNewPassword());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
