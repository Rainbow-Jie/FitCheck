package com.fitcheck.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String avatar;
    
    private Integer gender;  // 0-未知 1-男 2-女
    
    private Double height;   // 身高(cm)
    
    private Double weight;   // 体重(kg)
    
    private String phone;    // 手机号
    
    private String email;    // 邮箱(可选)
    
    private LocalDate birthday;  // 生日
    
    private Integer status;  // 账号状态: 1-正常 0-禁用
    
    private LocalDateTime lastCheckIn;  // 最后打卡时间
    
    private Integer profileUpdateCount;  // 本月已修改资料次数
    
    private Integer profileUpdateMonth;  // 计数月份 YYYYMM，跨月重置用
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
