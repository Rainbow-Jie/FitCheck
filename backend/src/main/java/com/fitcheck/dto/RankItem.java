package com.fitcheck.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class RankItem {
    private Integer rank;
    private Long userId;
    private String username;
    private String avatar;
    private Integer checkInCount;
    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;
    /** 生日，用于前端计算年龄 */
    private LocalDate birthday;
}
