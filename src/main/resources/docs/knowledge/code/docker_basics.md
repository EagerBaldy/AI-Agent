---
title: Docker 基础命令
version: 1.0
tags: [docker, devops]
author: AI Code Helper
create_time: 2023-10-10
---

# Docker 基础命令速查

Docker 是一个开源的应用容器引擎。

## 1. 镜像操作
- `docker pull <image>`: 拉取镜像。
- `docker images`: 列出本地镜像。
- `docker rmi <image_id>`: 删除镜像。
- `docker build -t <tag> .`: 构建镜像。

## 2. 容器操作
- `docker run -d -p 8080:80 <image>`: 后台运行容器并映射端口。
- `docker ps`: 列出运行中的容器。
- `docker exec -it <container_id> /bin/bash`: 进入容器。
- `docker stop <container_id>`: 停止容器。

## 3. Docker Compose
使用 `docker-compose.yml` 定义多容器应用。
- `docker-compose up -d`: 启动服务。
