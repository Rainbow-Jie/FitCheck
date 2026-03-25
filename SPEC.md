# 健身打卡系统 - 项目规范文档

## 1. 项目概述

**项目名称**: FitCheck - 健身打卡系统  
**项目类型**: 全栈Web应用（Spring Boot + Vue.js）  
**核心功能**: 健身打卡、记录查询、排行榜竞赛  
**目标用户**: 健身爱好者、需要督促自己坚持运动的用户  
**技术栈**: 
- 后端: Spring Boot 2.7.x + Java 8 + MySQL + Redis
- 前端: Vue 3 + Vant 3 (移动端UI框架) + Axios
- 数据库: MySQL 8.0 (本地部署)

---

## 2. UI/UX 设计规范

### 2.1 整体设计风格
- **设计语言**: 运动健身风格，现代简约
- **主题色**: 
  - 主色: `#FF6B35` (活力橙)
  - 辅助色: `#2EC4B6` (清新绿)
  - 强调色: `#E63946` (动感红)
  - 背景色: `#F8F9FA` (浅灰白)
  - 文字色: `#212529` (深灰黑)
- **字体**: 
  - 主字体: "PingFang SC", "Helvetica Neue", Arial, sans-serif
  - 数字字体: "DIN Alternate", "Roboto", sans-serif

### 2.2 页面结构

#### 2.2.1 登录/注册页面
- 居中卡片式设计
- 背景: 渐变色或运动场景图
- 表单: 用户名/密码输入框，圆角按钮
- 切换: 登录/注册Tab切换

#### 2.2.2 首页（打卡日历）
- 顶部: 用户头像 + 欢迎语 + 签到按钮
- 中部: 月份日历视图（可切换上月/下月）
- 日历样式:
  - 已打卡日期: 橙色圆点标记
  - 今日: 高亮边框
  - 不可打卡日期: 灰色
- 底部: 统计数据卡片（本月打卡天数、连续打卡天数）

#### 2.2.3 排行榜页面
- Tab切换: 日榜 / 周榜 / 月榜 / 总榜
- 列表项: 排名 + 头像 + 用户名 + 打卡次数
- 样式: 卡片式，排名用特殊样式（前三名金色/银色/铜色）

#### 2.2.4 个人中心页面
- 头像上传区（可编辑）
- 基本信息展示:
  - 用户名
  - 性别
  - 身高/体重
  - 注册时间
- 打卡统计概览
- 设置按钮（修改密码、退出登录）

### 2.3 响应式设计
- 移动端优先设计
- 最大宽度: 480px（居中显示）
- 触摸友好的按钮尺寸（最小44px）
- 底部固定导航栏（首页/排行/我的）

---

## 3. 功能模块详细设计

### 3.1 用户认证模块

#### 注册功能
- 输入: 用户名（唯一）、密码、确认密码
- 验证: 用户名长度4-20位，密码长度6-20位
- 响应: 注册成功自动登录，或返回错误信息

#### 登录功能
- 输入: 用户名、密码
- 验证: 登录成功返回JWT Token
- Token有效期: 7天
- 记住我: 延长至30天

### 3.2 打卡模块

#### 打卡操作
- 位置: 首页签到按钮
- 限制: 每日仅可打卡一次
- 时间: 当日00:00 - 23:59
- 反馈: 打卡成功动画 + 音效提示

#### 历史记录
- 显示: 近三个月日历视图
- 可切换: 上月、当前月、下月
- 标记: 已打卡日期用特殊样式

### 3.3 排行榜模块

#### 排名规则
- 按: 打卡次数降序
- 并列: 按最后打卡时间升序
- 分页: 每页20条

#### 榜单类型
- 日榜: 当日打卡用户
- 周榜: 本周打卡用户
- 月榜: 本月打卡用户
- 总榜: 累计打卡用户

### 3.4 个人中心模块

#### 头像上传
- 支持: JPG、PNG、GIF
- 大小限制: 2MB
- 自动裁剪: 1:1正方形
- 存储: 本地文件系统

#### 信息编辑
- 可修改: 用户名、性别、身高、体重
- 不可修改: 注册时间、累计打卡次数

---

## 4. 数据库设计

### 4.1 用户表 (users)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    avatar VARCHAR(255) DEFAULT '/uploads/default-avatar.png',
    gender TINYINT DEFAULT 0 COMMENT '0-未知 1-男 2-女',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 4.2 打卡记录表 (check_ins)
```sql
CREATE TABLE check_ins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    check_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, check_date),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.3 索引
- users: username (唯一索引)
- check_ins: user_id + check_date (唯一索引)
- check_ins: check_date (普通索引)

---

## 5. API接口设计

### 5.1 认证接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户登出 |

### 5.2 用户接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/user/info | 获取用户信息 |
| PUT | /api/user/info | 更新用户信息 |
| POST | /api/user/avatar | 上传头像 |
| GET | /api/user/stats | 获取用户打卡统计 |

### 5.3 打卡接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/checkin | 打卡 |
| GET | /api/checkin/history | 获取打卡历史 |
| GET | /api/checkin/today | 今日是否已打卡 |

### 5.4 排行榜接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/rank/list | 获取排行榜 |
| GET | /api/rank/my-rank | 获取我的排名 |

---

## 6. 项目结构

### 6.1 后端结构
```
backend/
├── src/main/java/com/fitcheck/
│   ├── FitCheckApplication.java
│   ├── config/
│   │   ├── WebConfig.java
│   │   ├── CorsConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── CheckInController.java
│   │   └── RankController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── CheckInService.java
│   │   └── RankService.java
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   └── CheckInMapper.java
│   ├── entity/
│   │   ├── User.java
│   │   └── CheckIn.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── ...
│   ├── common/
│   │   ├── Result.java
│   │   └── Constants.java
│   └── util/
│       └── JwtUtil.java
├── src/main/resources/
│   ├── application.yml
│   └── mapper/
├── pom.xml
└── uploads/  (头像存储目录)
```

### 6.2 前端结构
```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── api/
│   │   ├── index.js
│   │   ├── auth.js
│   │   ├── user.js
│   │   ├── checkin.js
│   │   └── rank.js
│   ├── router/
│   │   └── index.js
│   ├── views/
│   │   ├── Login.vue
│   │   ├── Home.vue
│   │   ├── Rank.vue
│   │   └── Profile.vue
│   ├── components/
│   │   ├── Calendar.vue
│   │   ├── CheckInButton.vue
│   │   ├── RankItem.vue
│   │   └── BottomNav.vue
│   ├── store/
│   │   └── index.js
│   └── assets/
│       └── styles/
│           └── main.css
├── package.json
└── vite.config.js
```

---

## 7. 安全考虑

### 7.1 密码安全
- 密码加密存储（BCrypt）
- 登录失败锁定（5次后锁定15分钟）

### 7.2 接口安全
- JWT Token认证
- Token刷新机制
- 请求频率限制

### 7.3 文件上传安全
- 文件类型白名单
- 文件大小限制
- 文件名随机化

---

## 8. 验收标准

### 8.1 功能验收
- [ ] 用户可以注册新账号
- [ ] 用户可以登录/登出
- [ ] 用户可以查看/修改个人信息
- [ ] 用户可以上传头像
- [ ] 用户可以在当日打卡
- [ ] 用户可以查看近三月打卡记录（日历形式）
- [ ] 用户可以查看打卡排行榜
- [ ] 用户可以查看自己的排名

### 8.2 UI验收
- [ ] 移动端适配良好
- [ ] 页面切换流畅
- [ ] 打卡有视觉反馈
- [ ] 排行榜样式美观
- [ ] 响应式布局正常

### 8.3 技术验收
- [ ] 后端API正常响应
- [ ] 数据库读写正常
- [ ] 头像上传功能正常
- [ ] JWT认证正常
