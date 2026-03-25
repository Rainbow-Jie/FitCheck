package com.fitcheck.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitcheck.dto.LoginResponse;
import com.fitcheck.dto.UserStats;
import com.fitcheck.dto.UserUpdateRequest;
import com.fitcheck.entity.CheckIn;
import com.fitcheck.entity.User;
import com.fitcheck.mapper.CheckInMapper;
import com.fitcheck.mapper.UserMapper;
import com.fitcheck.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CheckInMapper checkInMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 注册（含手机号）
     * 唯一性规则：手机号全局唯一；同一用户名+手机号组合全局唯一
     */
    public User register(String username, String phone, String password) {
        // 检查手机号是否已被注册（手机号全局唯一）
        QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phone", phone);
        User phoneUser = userMapper.selectOne(phoneWrapper);
        if (phoneUser != null) {
            throw new RuntimeException("该手机号已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setAvatar("/avatar/default-avatar.png");
        user.setGender(0);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }
    
    /**
     * 登录（用户名 + 密码 + 手机号后四位三重校验）
     * 先按用户名+后四位定位唯一用户，再校验密码
     */
    public LoginResponse login(String username, String password, String phoneLast4) {
        // 查询所有同名用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<User> users = userMapper.selectList(queryWrapper);

        if (users == null || users.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 用手机号后四位过滤
        User matched = null;
        for (User u : users) {
            if (u.getPhone() != null && u.getPhone().endsWith(phoneLast4)) {
                matched = u;
                break;
            }
        }

        if (matched == null) {
            throw new RuntimeException("用户名、手机号后四位或密码错误");
        }

        if (!passwordEncoder.matches(password, matched.getPassword())) {
            throw new RuntimeException("用户名、手机号后四位或密码错误");
        }
        
        String token = jwtUtil.generateToken(matched.getId(), matched.getUsername());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(matched.getId());
        response.setUsername(matched.getUsername());
        response.setAvatar(matched.getAvatar());
        
        return response;
    }
    
    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }
    
    /**
     * 更新用户信息（支持用户名、手机号、性别、生日）
     */
    public User updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // ===== 每月最多修改 3 次限制 =====
        int currentMonth = Integer.parseInt(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMM").format(LocalDate.now())
        );
        int savedMonth = user.getProfileUpdateMonth() != null ? user.getProfileUpdateMonth() : 0;
        int count = (savedMonth == currentMonth && user.getProfileUpdateCount() != null)
                    ? user.getProfileUpdateCount() : 0;
        if (count >= 3) {
            throw new RuntimeException("本月修改次数已达上限（每月最多3次），下月可继续修改");
        }

        // 更新用户名（允许不同手机号的用户重名，但同一用户名+手机号组合全局唯一）
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user.setUsername(request.getUsername().trim());
        }

        // 更新手机号（需保证全局唯一）
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(user.getPhone())) {
                QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
                phoneWrapper.eq("phone", newPhone).ne("id", userId);
                User existPhone = userMapper.selectOne(phoneWrapper);
                if (existPhone != null) {
                    throw new RuntimeException("该手机号已被其他账号使用");
                }
            }
            user.setPhone(newPhone);
        }

        // 校验用户名+手机号组合唯一性（排除自身）
        String finalUsername = user.getUsername();
        String finalPhone = user.getPhone();
        if (finalUsername != null && finalPhone != null) {
            QueryWrapper<User> comboWrapper = new QueryWrapper<>();
            comboWrapper.eq("username", finalUsername).eq("phone", finalPhone).ne("id", userId);
            if (userMapper.selectCount(comboWrapper) > 0) {
                throw new RuntimeException("该用户名与手机号的组合已被占用");
            }
        }

        // 更新性别
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        // 更新生日
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
        }

        user.setUpdatedAt(LocalDateTime.now());
        // 计数 +1，并记录当前月份（跨月时重置为 1）
        user.setProfileUpdateCount(count + 1);
        user.setProfileUpdateMonth(currentMonth);
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片文件");
        }
        
        // 检查文件大小 (2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过2MB");
        }
        
        // 创建上传目录
        String uploadPath = System.getProperty("user.dir") + "/uploads/avatar";
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String newFilename = userId + "_" + UUID.randomUUID().toString().replace("-", "") + suffix;
        
        // 保存文件
        try {
            File destFile = new File(uploadPath, newFilename);
            file.transferTo(destFile);
            
            // 删除旧头像（如果不是默认头像）
            if (user.getAvatar() != null && !user.getAvatar().equals("/avatar/default-avatar.png")) {
                String oldPath = System.getProperty("user.dir") + user.getAvatar();
                File oldFile = new File(oldPath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            
            // 更新用户头像
            String avatarUrl = "/avatar/" + newFilename;
            user.setAvatar(avatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            
            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("上传头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     */
    public UserStats getUserStats(Long userId) {
        UserStats stats = new UserStats();
        
        // 累计打卡天数
        int totalDays = checkInMapper.getTotalCheckInDays(userId);
        stats.setTotalDays(totalDays);
        
        // 本月打卡天数
        int currentMonthDays = checkInMapper.getCurrentMonthDays(userId, LocalDate.now());
        stats.setCurrentMonthDays(currentMonthDays);
        
        return stats;
    }

    /**
     * 获取用户排名（优化版：用 SQL 直接统计，避免全表加载）
     */
    public int getUserRank(Long userId) {
        int userCheckInCount = checkInMapper.getTotalCheckInDays(userId);
        // SQL 直接算出"比我打卡次数多的用户数 + 1"即为排名
        return checkInMapper.getRankByCount(userCheckInCount);
    }
    
    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    
    /**
     * 重置密码
     */
    public void resetPassword(String username, String newPassword) {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(Long userId, String password) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    /**
     * 更新密码
     */
    public void updatePassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
