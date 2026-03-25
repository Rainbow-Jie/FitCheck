package com.fitcheck.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    private Integer totalDays;        // 累计打卡天数
    private Integer currentMonthDays; // 本月打卡天数
    private Integer rank;             // 我的排名
}
