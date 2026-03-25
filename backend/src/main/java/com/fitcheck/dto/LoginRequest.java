package com.fitcheck.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 手机号后四位，用于区分同名用户
     */
    @NotBlank(message = "手机号后四位不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "请输入手机号后四位数字")
    private String phoneLast4;
}
