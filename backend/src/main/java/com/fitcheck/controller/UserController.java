package com.fitcheck.controller;

import com.fitcheck.common.Result;
import com.fitcheck.dto.UserStats;
import com.fitcheck.dto.UserUpdateRequest;
import com.fitcheck.entity.User;
import com.fitcheck.service.UserService;
import com.fitcheck.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取用户信息
     */
    @PostMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PostMapping("/update/info")
    public Result<User> updateUserInfo(HttpServletRequest request, @RequestBody UserUpdateRequest updateRequest) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        try {
            User user = userService.updateUserInfo(userId, updateRequest);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        try {
            String avatarUrl = userService.uploadAvatar(userId, file);
            return Result.success(avatarUrl);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @PostMapping("/stats")
    public Result<UserStats> getUserStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        UserStats stats = userService.getUserStats(userId);
        // 设置排名
        int rank = userService.getUserRank(userId);
        stats.setRank(rank);
        return Result.success(stats);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
