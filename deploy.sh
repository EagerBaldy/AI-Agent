#!/bin/bash
# 停止旧容器
docker-compose down

# 构建并启动新容器
docker-compose up -d --build

# 清理未使用的镜像
docker image prune -f

echo "Deployment completed! Access at http://www.kone.js.cn/proj/ai-agent"
