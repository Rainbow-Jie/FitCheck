package com.fitcheck.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    /** 用户名 */
    private String username;
    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;
    /** 手机号（修改时需要重新验证唯一性） */
    private String phone;
    /** 生日 */
    private LocalDate birthday;
    /** 身高(cm) */
    private Double height;
    /** 体重(kg) */
    private Double weight;
}
