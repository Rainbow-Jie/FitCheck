#!/bin/bash
# =============================================
# FitCheck 后端部署脚本（仅后端服务）
# 使用方式：bash deploy.sh [选项]
#   bash deploy.sh          # 构建镜像并启动后端
#   bash deploy.sh --pull   # 拉取最新代码后重新部署
#   bash deploy.sh --restart # 仅重启后端容器（不重新构建）
#   bash deploy.sh --logs   # 查看后端实时日志
#   bash deploy.sh --stop   # 停止后端服务
#   bash deploy.sh --health # 健康检查
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
    if ! command -v docker &>/dev/null; then
        log_error "未找到 docker，请先安装"
        exit 1
    fi
    # 兼容 docker compose（插件版）和 docker-compose（独立版）
    if docker compose version &>/dev/null 2>&1; then
        COMPOSE="docker compose"
    elif command -v docker-compose &>/dev/null; then
        COMPOSE="docker-compose"
    else
        log_error "未找到 docker compose / docker-compose，请先安装"
        exit 1
    fi
    log_info "依赖检查通过 (docker $(docker --version | grep -oP '\d+\.\d+\.\d+' | head -1)，compose: $COMPOSE)"
}

# ---- 创建必要目录 ----
init_dirs() {
    log_step "初始化目录"
    mkdir -p data/uploads/avatar data/logs/archive
    log_info "目录已就绪"
}

# ---- 全量部署（只启后端）----
deploy() {
    check_env
    check_deps
    init_dirs

    log_step "构建后端镜像"
    $COMPOSE build --no-cache backend

    log_step "启动后端服务"
    $COMPOSE up -d backend

    log_step "等待服务启动"
    sleep 10
    check_health
}

# ---- 仅重启后端 ----
restart() {
    check_env
    check_deps
    log_step "重启后端服务"
    $COMPOSE restart backend
    sleep 5
    check_health
}

# ---- 健康检查 ----
check_health() {
    log_step "健康检查"
    local max_wait=60
    local waited=0
    while [ $waited -lt $max_wait ]; do
        if $COMPOSE ps backend 2>/dev/null | grep -q "Up\|running"; then
            log_info "后端容器状态：运行中 ✅"
            break
        fi
        log_warn "等待后端启动... (${waited}s/${max_wait}s)"
        sleep 5
        waited=$((waited + 5))
    done

    if [ $waited -ge $max_wait ]; then
        log_error "后端启动超时，请检查日志：bash deploy.sh --logs"
    fi

    echo ""
    log_info "容器状态："
    $COMPOSE ps backend
}

# ---- 查看日志 ----
show_logs() {
    check_deps
    $COMPOSE logs -f --tail=100 backend
}

# ---- 停止后端服务 ----
stop_services() {
    check_deps
    log_step "停止后端服务"
    $COMPOSE stop backend
    log_info "后端服务已停止"
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
