package com.fitcheck.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class SendCodeRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
}
