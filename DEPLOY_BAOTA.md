# 宝塔面板部署指南

本指南将指导您如何使用宝塔面板部署 ai-code-helper 项目。

## 1. 环境准备

在宝塔面板的 **软件商店** 中安装以下软件：

1.  **Nginx** (任意稳定版本，推荐 1.22+)
2.  **MySQL** (推荐 8.0，至少 5.7)
3.  **Java 环境 (JDK 21)**

    本项目需要 **JDK 21**。如果宝塔的 Java 项目管理器中没有 JDK 21，推荐使用包管理器安装：

    *   **推荐：使用 yum 安装 (OpenCloudOS/CentOS)**：
        ```bash
        # 搜索可用的 jdk 21 包
        yum search java-21
        
        # 安装 JDK 21 开发包 (根据您的系统搜索结果选择)
        sudo yum install java-21-konajdk-devel -y
        
        # 如果遇到冲突错误 (Transaction test error)，请尝试先卸载冲突的旧版本：
        # sudo yum remove java-17-konajdk-headless -y
        # 然后再次运行安装命令
        
        # 验证安装
        java -version
        ```

    *   *备选：手动下载安装*：
        ```bash
        # 下载 JDK 21 (以 OpenJDK 为例)
        wget https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee52ce832ded4d/35/GPL/openjdk-21_linux-x64_bin.tar.gz
        tar -zxvf openjdk-21_linux-x64_bin.tar.gz
        mv jdk-21.0.1 /usr/local/java  # 注意解压后的文件夹名称可能不同
        
        # 配置环境变量
        echo 'export JAVA_HOME=/usr/local/java' >> /etc/profile
        echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile
        source /etc/profile
        ```

## 2. 数据库配置

1.  进入宝塔面板 -> **数据库** -> **添加数据库**。
2.  数据库名：`ai_code_helper`
3.  用户名：`root` (建议修改代码中的配置，或者创建一个新用户)
4.  密码：`1234` (请务必在 `src/main/resources/application.yml` 中修改为您设置的强密码，并在面板中同步修改)
5.  导入数据：点击 **导入**，上传并导入项目根目录下的 `sql/create_table.sql` 文件。

## 3. 后端部署

1.  **上传代码**：将项目文件上传到服务器目录，例如 `/www/wwwroot/ai-code-helper`。
2.  **修改配置**：编辑 `src/main/resources/application.yml`，确保数据库连接信息正确。
3.  **编译打包**：
    *   如果您服务器安装了 Maven，运行 `mvn clean package -DskipTests`。
    *   或者在本地电脑打包好 `target/ai-code-helper-0.0.1-SNAPSHOT.jar` 后上传。
4.  **启动服务**：
    *   进入项目目录：`cd /www/wwwroot/ai-code-helper`
    *   赋予脚本权限：`chmod +x deploy.sh`
    *   运行启动脚本：`./deploy.sh`
    *   查看日志：`tail -f app.log`

## 4. 前端部署

1.  **本地构建**：
    在本地开发环境中运行：
    ```bash
    cd ai-code-helper-frontend
    npm install
    npm run build
    ```
    这将生成 `dist` 目录。

2.  **创建站点**：
    *   宝塔面板 -> **网站** -> **添加站点**。
    *   域名：`ai-agent.kone.js.cn`
    *   根目录：选择 `/www/wwwroot/ai-agent.kone.js.cn` (或者您喜欢的其他路径)
    *   数据库：不创建

3.  **上传前端文件**：
    将本地生成的 `dist` 目录下的**所有文件**上传到刚才创建的站点根目录 (`/www/wwwroot/ai-agent.kone.js.cn`)。

4.  **配置反向代理与路由**：
    *   点击站点设置 -> **配置文件**。
    *   **清空**原内容，复制 `baota_nginx.conf` 文件中的内容粘贴进去。
    *   *或者手动配置*：
        *   设置伪静态（解决路由 404）：
            ```nginx
            location / {
                try_files $uri $uri/ /index.html;
            }
            ```
        *   添加反向代理（连接后端）：
            *   目标 URL: `http://127.0.0.1:8081`
            *   发送域名: `$host`

## 5. 验证

访问 `http://ai-agent.kone.js.cn`，如果能正常显示页面并进行对话，即部署成功。
