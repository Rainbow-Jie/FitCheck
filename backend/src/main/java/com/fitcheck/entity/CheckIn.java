package com.fitcheck.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("check_ins")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private LocalDate checkDate;
    
    private LocalTime checkTime;  // 打卡时间(可选)
    
    private Integer duration;      // 运动时长/分钟(可选)
    
    private String note;           // 打卡备注(可选)
    
    private LocalDateTime createdAt;
}
