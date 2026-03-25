#!/bin/bash
# =============================================
# FitCheck 一键部署脚本
# 使用方式：bash deploy.sh [选项]
#   bash deploy.sh          # 全量部署（构建+启动）
#   bash deploy.sh --pull   # 拉取代码后全量部署
#   bash deploy.sh --restart # 仅重启服务（不重新构建）
#   bash deploy.sh --logs   # 查看后端实时日志
#   bash deploy.sh --stop   # 停止所有服务
# =============================================

set -e

# ---- 颜色输出 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()  { echo -e "\n${BLUE}========== $1 ==========${NC}"; }

# ---- 项目根目录（脚本所在位置）----
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- 检查 .env 文件 ----
check_env() {
    if [ ! -f ".env" ]; then
        log_error ".env 文件不存在！请先执行："
        echo "    cp .env.example .env"
        echo "    vim .env  # 填入真实配置"
        exit 1
    fi
    source .env
    if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "your_db_password_here" ]; then
        log_error ".env 中 DB_PASSWORD 未配置，请先填写真实密码"
        exit 1
    fi
    log_info ".env 配置检查通过"
}

# ---- 检查依赖 ----
check_deps() {
    for cmd in docker docker-compose; do
        if ! command -v $cmd &>/dev/null; then
            log_error "未找到命令：$cmd，请先安装"
            exit 1
        fi
    done
    log_info "依赖检查通过 (docker $(docker --version | grep -oP '\d+\.\d+\.\d+' | head -1))"
}

# ---- 构建前端 ----
build_frontend() {
    if [ ! -d "frontend" ]; then
        log_warn "未找到 frontend 目录，跳过前端构建"
        return
    fi
    log_step "构建前端"
    cd frontend
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi
    log_info "执行 npm run build..."
    npm run build
    cd "$SCRIPT_DIR"
    log_info "前端构建完成 -> frontend/dist/"
}

# ---- 创建必要目录 ----
init_dirs() {
    log_step "初始化目录"
    mkdir -p data/uploads/avatar data/logs/archive data/logs/nginx nginx/ssl
    log_info "目录已就绪"
}

# ---- 部署 Nginx 配置 ----
copy_nginx_conf() {
    if [ ! -f "nginx/nginx.conf" ]; then
        log_info "复制 Nginx 配置..."
        cp nginx.conf nginx/nginx.conf
    fi
}

# ---- 全量部署 ----
deploy() {
    check_env
    check_deps
    init_dirs
    build_frontend
    copy_nginx_conf

    log_step "构建并启动服务"
    docker-compose down --remove-orphans 2>/dev/null || true
    docker-compose build --no-cache backend
    docker-compose up -d

    log_step "等待服务启动"
    sleep 10
    check_health
}

# ---- 仅重启 ----
restart() {
    check_env
    log_step "重启服务"
    docker-compose restart
    sleep 5
    check_health
}

# ---- 健康检查 ----
check_health() {
    log_step "健康检查"
    local max_wait=60
    local waited=0
    while [ $waited -lt $max_wait ]; do
        if docker-compose ps backend | grep -q "Up"; then
            log_info "后端容器状态：运行中 ✅"
            break
        fi
        log_warn "等待后端启动... (${waited}s/${max_wait}s)"
        sleep 5
        waited=$((waited + 5))
    done

    # 尝试 HTTP 健康探测
    if curl -sf "http://localhost:20001/fitcheck-api/health" &>/dev/null; then
        log_info "HTTP 健康检查通过 ✅"
    else
        log_warn "HTTP 健康检查未响应（接口可能不存在，属正常）"
    fi

    echo ""
    log_info "容器状态："
    docker-compose ps
}

# ---- 查看日志 ----
show_logs() {
    docker-compose logs -f --tail=100 backend
}

# ---- 停止服务 ----
stop_services() {
    log_step "停止所有服务"
    docker-compose down
    log_info "服务已停止"
}

# ---- 入口 ----
case "${1:-}" in
    --pull)
        log_step "拉取最新代码"
        git pull
        deploy
        ;;
    --restart)
        restart
        ;;
    --logs)
        show_logs
        ;;
    --stop)
        stop_services
        ;;
    --health)
        check_health
        ;;
    "")
        deploy
        ;;
    *)
        echo "用法: bash deploy.sh [--pull | --restart | --logs | --stop | --health]"
        exit 1
        ;;
esac
