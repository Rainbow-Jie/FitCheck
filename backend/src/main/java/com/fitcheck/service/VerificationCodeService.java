package com.fitcheck.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务
 * 用于忘记密码时的验证码校验
 */
@Service
public class VerificationCodeService {
    
    // 存储验证码：key为用户名，value为验证码对象
    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();
    
    // 验证码有效期：5分钟
    private static final long CODE_EXPIRY_MILLIS = 5 * 60 * 1000;
    
    /**
     * 生成验证码
     */
    public String generateCode(String username) {
        // 生成6位数字验证码
        String code = String.format("%06d", new Random().nextInt(999999));
        
        CodeInfo codeInfo = new CodeInfo(code, System.currentTimeMillis());
        codeStore.put(username, codeInfo);
        
        // 模拟发送验证码（实际项目中应发送短信或邮件）
        System.out.println("【FitCheck】验证码已发送至用户 " + username + "，验证码：" + code);
        
        return code; // 开发环境直接返回验证码
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(String username, String code) {
        CodeInfo codeInfo = codeStore.get(username);
        
        if (codeInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() - codeInfo.createTime > CODE_EXPIRY_MILLIS) {
            codeStore.remove(username);
            return false;
        }
        
        // 检查验证码是否匹配
        if (codeInfo.code.equals(code)) {
            codeStore.remove(username); // 验证成功后删除
            return true;
        }
        
        return false;
    }
    
    /**
     * 验证码信息
     */
    private static class CodeInfo {
        String code;
        long createTime;
        
        CodeInfo(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
    }
}
