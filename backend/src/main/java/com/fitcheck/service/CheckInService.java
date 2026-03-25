package com.fitcheck.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitcheck.entity.CheckIn;
import com.fitcheck.entity.User;
import com.fitcheck.mapper.CheckInMapper;
import com.fitcheck.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckInService extends ServiceImpl<CheckInMapper, CheckIn> {
    
    @Autowired
    private CheckInMapper checkInMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 打卡
     */
    @Transactional
    public CheckIn doCheckIn(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        // 检查今天是否已打卡
        int exist = checkInMapper.checkExist(userId, today);
        if (exist > 0) {
            throw new RuntimeException("今日已打卡，请勿重复操作");
        }
        
        // 创建打卡记录
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(today);
        checkIn.setCheckTime(LocalTime.now());
        checkIn.setCreatedAt(now);
        
        checkInMapper.insert(checkIn);
        
        // 更新用户的最后打卡时间
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastCheckIn(now);
            userMapper.updateById(user);
        }
        
        return checkIn;
    }
    
    /**
     * 检查今日是否已打卡
     */
    public boolean isCheckedInToday(Long userId) {
        return checkInMapper.checkExist(userId, LocalDate.now()) > 0;
    }
    
    /**
     * 获取打卡历史（近三个月）
     */
    public Map<String, Object> getCheckInHistory(Long userId, int year, int month) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate startDate;
        LocalDate endDate;
        
        if (month < 3) {
            // 需要跨年
            startDate = LocalDate.of(year - 1, 12 - (3 - month) + 1, 1);
        } else {
            startDate = LocalDate.of(year, month - 2, 1);
        }
        endDate = startDate.plusMonths(3).minusDays(1);
        
        List<LocalDate> checkInDates = checkInMapper.getCheckInDates(userId, startDate, endDate);
        
        // 转换为字符串列表
        List<String> dateStrings = new ArrayList<>();
        for (LocalDate date : checkInDates) {
            dateStrings.add(date.toString());
        }
        
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("checkInDates", dateStrings);
        result.put("year", year);
        result.put("month", month);
        
        return result;
    }
    
    /**
     * 获取用户在指定月份的打卡日期
     */
    public List<LocalDate> getCheckInDatesByMonth(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return checkInMapper.getCheckInDates(userId, startDate, endDate);
    }
}
