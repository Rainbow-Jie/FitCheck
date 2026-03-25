package com.fitcheck.controller;

import com.fitcheck.common.Result;
import com.fitcheck.dto.RankItem;
import com.fitcheck.dto.RankListRequest;
import com.fitcheck.service.RankService;
import com.fitcheck.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/rank")
public class RankController {
    
    @Autowired
    private RankService rankService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取排行榜
     */
    @PostMapping("/list")
    public Result<List<RankItem>> getRankList(@RequestBody RankListRequest request) {
        String type = request.getType() != null ? request.getType() : "total";
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        List<RankItem> rankList = rankService.getRankList(type, page, size);
        return Result.success(rankList);
    }
    
    /**
     * 获取我的排名
     */
    @PostMapping("/my-rank")
    public Result<RankItem> getMyRank(HttpServletRequest request, @RequestBody RankListRequest requestBody) {
        String type = requestBody.getType() != null ? requestBody.getType() : "total";
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        RankItem myRank = rankService.getUserRank(userId, type);
        return Result.success(myRank);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
