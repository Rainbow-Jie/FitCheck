package com.fitcheck.controller;

import com.fitcheck.common.Result;
import com.fitcheck.dto.CheckInHistoryRequest;
import com.fitcheck.dto.CheckInMonthRequest;
import com.fitcheck.entity.CheckIn;
import com.fitcheck.service.CheckInService;
import com.fitcheck.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/check-in")
public class CheckInController {
    
    @Autowired
    private CheckInService checkInService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 打卡
     */
    @PostMapping("/add")
    public Result<CheckIn> doCheckIn(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        try {
            CheckIn checkIn = checkInService.doCheckIn(userId);
            return Result.success(checkIn);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查今日是否已打卡
     */
    @PostMapping("/today")
    public Result<Map<String, Boolean>> checkToday(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        boolean isCheckedIn = checkInService.isCheckedInToday(userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("checkedIn", isCheckedIn);
        return Result.success(result);
    }
    
    /**
     * 获取打卡历史
     */
    @PostMapping("/history")
    public Result<Map<String, Object>> getHistory(HttpServletRequest request, @RequestBody(required = false) CheckInHistoryRequest req) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        java.time.LocalDate today = java.time.LocalDate.now();
        int year = (req != null && req.getYear() != null) ? req.getYear() : today.getYear();
        int month = (req != null && req.getMonth() != null) ? req.getMonth() : today.getMonthValue();
        
        Map<String, Object> history = checkInService.getCheckInHistory(userId, year, month);
        return Result.success(history);
    }
    
    /**
     * 获取指定月份的打卡日期
     */
    @PostMapping("/month")
    public Result<List<String>> getMonthCheckIns(HttpServletRequest request, @RequestBody CheckInMonthRequest req) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        int year = req.getYear();
        int month = req.getMonth();
        
        List<java.time.LocalDate> dates = checkInService.getCheckInDatesByMonth(userId, year, month);
        List<String> dateStrings = new java.util.ArrayList<>();
        for (java.time.LocalDate date : dates) {
            dateStrings.add(date.toString());
        }
        return Result.success(dateStrings);
    }
    
    /**
     * 获取打卡日历
     */
    @PostMapping("/calendar")
    public Result<List<Map<String, Object>>> getCalendar(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Map<String, Object>> days = new java.util.ArrayList<>();
        
        for (int i = 29; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            List<java.time.LocalDate> checkInDates = checkInService.getCheckInDatesByMonth(userId, date.getYear(), date.getMonthValue());
            boolean checked = checkInDates.contains(date);
            
            Map<String, Object> day = new java.util.HashMap<>();
            day.put("date", date.toString());
            day.put("day", date.getDayOfMonth());
            day.put("checked", checked);
            days.add(day);
        }
        
        return Result.success(days);
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
