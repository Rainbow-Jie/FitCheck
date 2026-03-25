package com.fitcheck.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitcheck.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {
    
    /**
     * 检查用户某日是否已打卡
     */
    @Select("SELECT COUNT(*) FROM check_ins WHERE user_id = #{userId} AND check_date = #{date}")
    int checkExist(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户在某个日期范围内的打卡记录
     */
    @Select("SELECT check_date FROM check_ins WHERE user_id = #{userId} AND check_date BETWEEN #{startDate} AND #{endDate} ORDER BY check_date")
    List<LocalDate> getCheckInDates(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 获取用户累计打卡天数
     */
    @Select("SELECT COUNT(*) FROM check_ins WHERE user_id = #{userId}")
    int getTotalCheckInDays(@Param("userId") Long userId);
    
    /**
     * 获取用户本月打卡天数
     */
    @Select("SELECT COUNT(*) FROM check_ins WHERE user_id = #{userId} AND YEAR(check_date) = YEAR(#{date}) AND MONTH(check_date) = MONTH(#{date})")
    int getCurrentMonthDays(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 获取用户某日的打卡记录
     */
    @Select("SELECT * FROM check_ins WHERE user_id = #{userId} AND check_date = #{date} LIMIT 1")
    CheckIn getCheckInByDate(@Param("userId") Long userId, @Param("date") LocalDate date);


    /**
     * 用 SQL 直接统计用户排名：比当前用户打卡次数多的用户数 + 1
     */
    @Select("SELECT COUNT(DISTINCT user_id) + 1 FROM (" +
            "  SELECT user_id, COUNT(*) AS cnt FROM check_ins GROUP BY user_id" +
            ") t WHERE t.cnt > #{userCount}")
    int getRankByCount(@Param("userCount") int userCount);
}
