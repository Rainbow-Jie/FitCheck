# =============================================
# 阶段一：Maven 构建
# =============================================
FROM maven:3.8.6-eclipse-temurin-8 AS builder

WORKDIR /build

# 先只复制 pom.xml，利用 Docker 层缓存下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再复制源码并打包（跳过测试）
COPY src ./src
RUN mvn clean package -DskipTests -B

# =============================================
# 阶段二：运行时镜像（精简版 JRE）
# =============================================
FROM eclipse-temurin:8-jre-alpine

LABEL maintainer="FitCheck"
LABEL description="FitCheck 健身打卡系统 - 后端服务"

# 创建非 root 用户运行，提高安全性
RUN addgroup -S fitcheck && adduser -S fitcheck -G fitcheck

WORKDIR /app

# 从构建阶段复制 jar
COPY --from=builder /build/target/fitcheck-backend-*.jar app.jar

# 创建必要目录并授权
RUN mkdir -p /app/uploads/avatar /app/logs/archive \
    && chown -R fitcheck:fitcheck /app

USER fitcheck

# 暴露端口
EXPOSE 20001

# JVM 调优参数（适合 1-2 核云服务器）
ENV JAVA_OPTS="-Xms256m -Xmx512m \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=Asia/Shanghai"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
