package com.fitcheck.dto;

import lombok.Data;

@Data
public class RankListRequest {
    private String type;
    private Integer page;
    private Integer size;
}
