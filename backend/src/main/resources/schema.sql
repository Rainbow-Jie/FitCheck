-- =============================================
-- FitCheck 健身打卡系统 数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fitcheck DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fitcheck;

-- ---------------------------------------------
-- 用户表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    avatar VARCHAR(255) DEFAULT '/avatar/default-avatar.png' COMMENT '头像路径',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    phone VARCHAR(20) COMMENT '手机号(可选，用于找回密码)',
    email VARCHAR(100) COMMENT '邮箱(可选)',
    status TINYINT DEFAULT 1 COMMENT '账号状态: 1-正常 0-禁用',
    last_check_in DATETIME COMMENT '最后打卡时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户名索引（加速登录查询）
CREATE INDEX idx_users_username ON users(username);

-- ---------------------------------------------
-- 打卡记录表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS check_ins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    check_date DATE NOT NULL COMMENT '打卡日期',
    check_time TIME COMMENT '打卡时间(可选)',
    duration INT COMMENT '运动时长/分钟(可选)',
    note VARCHAR(255) COMMENT '打卡备注(可选)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 唯一约束：每个用户每天只能打卡一次
    UNIQUE KEY uk_user_date (user_id, check_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 打卡日期索引
CREATE INDEX idx_check_in_date ON check_ins(check_date);

-- 用户打卡次数索引（用于排行榜加速）
CREATE INDEX idx_check_in_user ON check_ins(user_id);

-- ---------------------------------------------
-- 验证码记录表（可选，用于找回密码等场景）
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    type VARCHAR(20) NOT NULL COMMENT '类型: reset_password-重置密码 register-注册 bind_phone-绑定手机',
    expire_at DATETIME NOT NULL COMMENT '过期时间',
    used TINYINT DEFAULT 0 COMMENT '是否已使用: 0-未使用 1-已使用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码记录表';

-- 验证码查询索引
CREATE INDEX idx_code_user_type ON verification_codes(username, type, used);
CREATE INDEX idx_code_expire ON verification_codes(expire_at);

-- =============================================
-- 初始化数据
-- =============================================

-- 注意：默认头像文件需要手动放置到 static/avatar/ 目录

-- =============================================
-- 增量变更脚本 V2（2026-03-24）
-- 功能：支持中文用户名 + 手机号登录校验
-- 执行前提：已执行过上方初始化脚本
-- =============================================

USE fitcheck;

-- 1. username 字段扩容以支持中文（VARCHAR 按字符数计算，中文utf8mb4每字最多4字节）
--    同时去掉 UNIQUE 约束（允许重名，通过手机号后四位区分）
ALTER TABLE users MODIFY COLUMN username VARCHAR(20) NOT NULL COMMENT '用户名（支持中文，允许重名）';

-- 删除旧的 username 唯一索引（如果存在）
-- 注意：不同 MySQL 版本索引名可能不同，以下两条按实际情况选一条执行
ALTER TABLE users DROP INDEX username;
-- 若上一行报错，改用：DROP INDEX idx_users_username ON users;

-- 2. phone 字段添加唯一约束（一个手机号只能注册一个账号）
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) NOT NULL COMMENT '手机号（注册必填，全局唯一）';
ALTER TABLE users ADD UNIQUE INDEX uk_users_phone (phone);

-- 3. 若已有旧数据（phone 为 NULL），可先补充手机号后再添加约束
--    UPDATE users SET phone = CONCAT('1380000', LPAD(id, 4, '0')) WHERE phone IS NULL;

-- 验证变更结果
-- DESC users;

-- =============================================
-- 增量变更脚本 V3（2026-03-24）
-- 功能：users 表新增 birthday（生日）字段
-- 执行前提：已执行过 V2 脚本
-- =============================================

USE fitcheck;

-- 新增 birthday 字段（DATE 类型，可为空）
ALTER TABLE users ADD COLUMN birthday DATE DEFAULT NULL COMMENT '生日';

-- 验证
-- DESC users;

-- =============================================
-- 增量变更脚本 V4（2026-03-25）
-- 功能：users 表新增个人信息修改次数限制字段
-- 执行前提：已执行过 V3 脚本
-- =============================================

USE fitcheck;

-- 新增：本月已修改次数（每月最多3次，跨月自动重置）
ALTER TABLE users ADD COLUMN profile_update_count TINYINT NOT NULL DEFAULT 0 COMMENT '本月已修改资料次数';
-- 新增：计数所属月份（格式 YYYYMM，用于跨月自动重置）
ALTER TABLE users ADD COLUMN profile_update_month INT DEFAULT NULL COMMENT '计数月份 YYYYMM';

-- 验证
-- DESC users;
