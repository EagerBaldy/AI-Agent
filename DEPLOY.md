# 部署指南

## 1. 准备工作

确保您的服务器上已经安装了：
- Docker
- Docker Compose

## 2. 部署步骤

1. 将项目代码上传到服务器。
2. 在项目根目录下运行以下命令启动服务：

```bash
docker-compose up -d --build
```

此命令将自动构建后端和前端镜像，并启动数据库。

## 3. 防火墙与端口设置（重要）

关于是否需要开放 **3001** 端口，取决于您访问网站的方式：

### 情况 A：使用域名访问（推荐）
如果您配置了 Nginx 反向代理（如步骤 4 所示），通过 `http://ai-agent.kone.js.cn` 访问：
- **不需要** 对外开放 3001 端口。
- 只需要确保服务器开放了 **80** (HTTP) 和 **443** (HTTPS) 端口。
- Nginx 会在服务器内部通过 `localhost:3001` 与服务通信，这不需要防火墙放行。

### 情况 B：使用 IP 直接访问
如果您想通过 `http://服务器IP:3001` 直接访问：
- **需要** 在服务器防火墙（如阿里云安全组、腾讯云防火墙、iptables）中放行 **3001** 端口。

## 4. 配置 Nginx 反向代理

> **注意**：如果不确定 Nginx 配置文件在哪里，可以使用 `nginx -t` 命令查找，或者使用 `find / -name nginx.conf` 搜索。如果是使用宝塔面板等工具，请在面板中添加网站并配置反向代理。

在您的宿主机 Nginx 配置文件中（通常位于 `/etc/nginx/nginx.conf`、`/usr/local/nginx/conf/nginx.conf` 或 `/etc/nginx/conf.d/` 下），添加以下 `server` 块以支持域名 `ai-agent.kone.js.cn`：

```nginx
server {
    listen 80;
    server_name ai-agent.kone.js.cn;

    location / {
        # 转发到 Docker 容器映射的 3001 端口
        proxy_pass http://localhost:3001;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 支持 WebSocket (如果需要)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

添加完成后，重新加载 Nginx 配置：

```bash
sudo nginx -s reload
```

### 找不到 Nginx 配置文件？

如果您的服务器上没有安装 Nginx，或者不知道如何配置，您可以：

1.  **直接通过端口访问**：确保服务器防火墙开放了 `3001` 端口，然后通过 `http://您的服务器IP:3001` 访问。
2.  **查看占用 80 端口的服务**：运行 `netstat -tulpn | grep :80` 查看当前运行的 Web 服务，可能是 Apache、Caddy 或其他服务。

## 5. 验证部署

访问 [http://ai-agent.kone.js.cn](http://ai-agent.kone.js.cn) 即可看到部署的新网站。

## 6. 常用维护命令

- 查看日志：`docker-compose logs -f`
- 停止服务：`docker-compose down`
- 重启服务：`docker-compose restart`
