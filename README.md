# FitCheck - 健身打卡系统

一个简洁好看的健身打卡Web应用，支持移动端访问。

## 功能特性

- 用户注册与登录
- 每日打卡记录
- 日历视图查看历史打卡
- 打卡排行榜（日榜/周榜/月榜/总榜）
- 个人中心管理
- 头像上传

## 技术栈

### 后端
- Spring Boot 2.7.x
- Java 8
- MyBatis Plus
- MySQL
- JWT 认证

### 前端
- Vue 3
- Vite
- Vant 4 (移动端UI组件库)
- Axios
- Day.js

## 项目结构

```
fitcheck/
├── backend/          # Spring Boot 后端
│   ├── src/
│   ├── pom.xml
│   └── start.bat
├── frontend/         # Vue 前端
│   ├── src/
│   ├── package.json
│   └── start.bat
└── SPEC.md          # 项目规范文档
```

## 快速开始

### 1. 准备数据库

```sql
-- 创建数据库
CREATE DATABASE fitcheck DEFAULT CHARACTER SET utf8mb4;
```

### 2. 启动后端

```bash
cd backend
.\start.bat
```

或手动启动：

```bash
cd backend
mvn clean package -DskipTests
java -jar target/fitcheck-backend-1.0.0.jar
```

### 3. 启动前端

```bash
cd frontend
.\start.bat
```

或手动启动：

```bash
cd frontend
npm install
npm run dev
```

### 4. 访问应用

- 前端地址: http://localhost:3000
- 后端地址: http://localhost:8080/api

## 默认配置

### 数据库配置
修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fitcheck?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的密码
```

### 端口配置
- 后端默认端口: 8080
- 前端默认端口: 3000

## 界面预览

### 登录页
- 渐变色背景
- 卡片式表单
- 简洁美观的设计

### 首页
- 顶部用户信息
- 打卡统计
- 大按钮打卡
- 日历视图

### 排行榜
- Tab 切换（总榜/月榜/周榜/日榜）
- 金银铜牌样式
- 我的排名展示

### 个人中心
- 头像上传
- 个人信息编辑
- 统计数据展示
- 退出登录

## API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/auth/register | POST | 用户注册 |
| /api/auth/login | POST | 用户登录 |
| /api/auth/me | GET | 获取当前用户 |
| /api/user/info | GET | 获取用户信息 |
| /api/user/info | PUT | 更新用户信息 |
| /api/user/avatar | POST | 上传头像 |
| /api/user/stats | GET | 获取用户统计 |
| /api/checkin | POST | 打卡 |
| /api/checkin/today | GET | 今日打卡状态 |
| /api/checkin/month | GET | 月度打卡日期 |
| /api/rank/list | GET | 排行榜列表 |
| /api/rank/my-rank | GET | 我的排名 |

## 注意事项

1. 首次使用请先创建数据库
2. 确保 MySQL 服务已启动
3. 前端开发服务器会自动代理 API 请求到后端
4. 生产环境请修改 JWT 密钥

## License

MIT License
