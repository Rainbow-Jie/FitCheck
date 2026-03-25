package com.fitcheck.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitcheck.dto.RankItem;
import com.fitcheck.entity.CheckIn;
import com.fitcheck.entity.User;
import com.fitcheck.mapper.CheckInMapper;
import com.fitcheck.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankService extends ServiceImpl<CheckInMapper, CheckIn> {
    
    @Autowired
    private CheckInMapper checkInMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取排行榜
     * @param type: day, week, month, total
     */
    public List<RankItem> getRankList(String type, int page, int size) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = null;
        LocalDate endDate = today;
        
        switch (type) {
            case "day":
                startDate = today;
                break;
            case "week":
                startDate = today.minusDays(6);
                break;
            case "month":
                startDate = today.with(TemporalAdjusters.firstDayOfMonth());
                break;
            case "total":
            default:
                startDate = LocalDate.of(2000, 1, 1); // 设置一个很早的日期
                break;
        }
        
        // 查询指定日期范围内的所有打卡记录
        QueryWrapper<CheckIn> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("check_date", startDate, endDate);
        List<CheckIn> checkIns = checkInMapper.selectList(queryWrapper);
        
        // 按用户分组统计打卡次数
        Map<Long, Integer> userCheckInCount = new HashMap<>();
        for (CheckIn checkIn : checkIns) {
            userCheckInCount.merge(checkIn.getUserId(), 1, Integer::sum);
        }
        
        // 排序并获取用户信息
        List<Map.Entry<Long, Integer>> sortedList = userCheckInCount.entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    int cmp = e2.getValue().compareTo(e1.getValue());
                    if (cmp != 0) return cmp;
                    // 打卡次数相同，按最后打卡时间排序
                    CheckIn c1 = checkInMapper.getCheckInByDate(e1.getKey(), today);
                    CheckIn c2 = checkInMapper.getCheckInByDate(e2.getKey(), today);
                    if (c1 == null) return 1;
                    if (c2 == null) return -1;
                    return c1.getCreatedAt().compareTo(c2.getCreatedAt());
                })
                .collect(Collectors.toList());
        
        // 分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, sortedList.size());
        if (start >= sortedList.size()) {
            return new ArrayList<>();
        }
        List<Map.Entry<Long, Integer>> pagedList = sortedList.subList(start, end);
        
        // 构建返回结果
        List<RankItem> rankList = new ArrayList<>();
        int rank = start + 1;
        for (Map.Entry<Long, Integer> entry : pagedList) {
            User user = userMapper.selectById(entry.getKey());
            if (user != null) {
                RankItem item = new RankItem();
                item.setRank(rank);
                item.setUserId(user.getId());
                item.setUsername(user.getUsername());
                item.setAvatar(user.getAvatar());
                item.setCheckInCount(entry.getValue());
                item.setGender(user.getGender());
                item.setBirthday(user.getBirthday());
                rankList.add(item);
                rank++;
            }
        }
        
        return rankList;
    }
    
    /**
     * 获取用户排名
     */
    public RankItem getUserRank(Long userId, String type) {
        List<RankItem> rankList = getRankList(type, 1, 1000); // 获取足够多的数据
        
        for (RankItem item : rankList) {
            if (item.getUserId().equals(userId)) {
                return item;
            }
        }
        
        // 用户未上榜，返回基本信息
        User user = userMapper.selectById(userId);
        RankItem item = new RankItem();
        item.setRank(-1);
        item.setUserId(userId);
        item.setUsername(user != null ? user.getUsername() : "");
        item.setAvatar(user != null ? user.getAvatar() : "");
        item.setCheckInCount(checkInMapper.getTotalCheckInDays(userId));
        return item;
    }
    
    /**
     * 获取用户总排名
     */
    public int getUserTotalRank(Long userId) {
        RankItem item = getUserRank(userId, "total");
        return item.getRank();
    }
}
