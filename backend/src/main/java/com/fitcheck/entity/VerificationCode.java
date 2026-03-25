package com.fitcheck.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("verification_codes")
public class VerificationCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;     // 用户名
    
    private String code;         // 验证码
    
    private String type;         // 类型: reset_password-重置密码 register-注册 bind_phone-绑定手机
    
    private LocalDateTime expireAt;  // 过期时间
    
    private Integer used;        // 是否已使用: 0-未使用 1-已使用
    
    private LocalDateTime createdAt;
}
