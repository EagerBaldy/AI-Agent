# AI All-in-One Agent (AI Code Helper)

> 您的智能工作与生活伙伴 | Your Intelligent Partner for Work and Life

本项目是一个基于 **Spring Boot** 和 **Vue 3** 的全能 AI 助手平台，集成了多种垂直领域的智能体（Agent），采用 **LangChain4j** 框架实现 ReAct 架构、RAG（检索增强生成）和工具调用功能。

## ✨ 核心功能 (Features)

平台内置了六大智能助手模式：

1.  **💻 编程助手 (Code Agent)**
    *   解答 Java, Python, Vue, Spring 等技术难题。
    *   提供代码生成、调试建议和架构优化方案。
    *   支持 RAG 检索本地技术文档。

2.  **✈️ 旅游助手 (Travel Agent)**
    *   规划旅行路线、推荐景点和美食。
    *   提供签证、交通和住宿建议。

3.  **📝 作文老师 (Essay Teacher)**
    *   辅助写作，提供审题、立意、结构建议。
    *   精批细改，优化文采和逻辑。

4.  **📚 素材百科 (Material Encyclopedia)**
    *   提供丰富的写作素材、名言警句、历史典故。
    *   扩充文章内涵。

5.  **🏥 医疗助手 (Medical Assistant)**
    *   提供健康咨询、症状初筛和养生建议。
    *   *注意：建议仅供参考，不可替代专业医生诊断。*

6.  **🎓 升学规划 (College Planning)**
    *   提供院校排名、专业解读、就业前景分析。
    *   辅助考研和留学规划。

## 🛠️ 技术栈 (Tech Stack)

### Backend (后端)
*   **Java 21**: 最新 LTS 版本。
*   **Spring Boot 3.5.3**: 核心 Web 框架。
*   **LangChain4j 1.0.0-beta1**: Java 版 LLM 应用开发框架。
    *   支持 **ReAct Agent** 架构（思考-行动-观察循环）。
    *   支持 **RAG** (Retrieval-Augmented Generation)。
    *   支持 **MCP** (Model Context Protocol)。
*   **MySQL**: 持久化存储用户、会话和消息数据。
*   **MyBatis-Plus**: ORM 框架。
*   **Alibaba DashScope (Qwen)**: 底座大模型 (通义千问)。

### Frontend (前端)
*   **Vue 3**: 渐进式 JavaScript 框架。
*   **Vite**: 下一代前端构建工具。
*   **Pinia**: 状态管理库。
*   **Vue Router**: 路由管理。
*   **Markdown CSS**: 美化的 Markdown 渲染样式。

## 🚀 快速开始 (Getting Started)

### 1. 环境准备
*   JDK 21+
*   Node.js 18+
*   MySQL 8.0+
*   Maven 3.6+

### 2. 数据库设置
1.  创建数据库 `ai_code_helper`。
2.  执行 `sql/create_table.sql` 脚本初始化表结构。

### 3. 后端启动
1.  进入项目根目录。
2.  修改 `src/main/resources/application.yml` 中的数据库配置（`username`, `password`）和 API Key。
3.  运行启动类：`com.star.aicodehelper.AiCodeHelperApplication`。

```bash
mvn spring-boot:run
```

### 4. 前端启动
1.  进入前端目录：
    ```bash
    cd ai-code-helper-frontend
    ```
2.  安装依赖：
    ```bash
    npm install
    ```
3.  启动开发服务器：
    ```bash
    npm run dev
    ```

## 🏗️ 系统架构 (System Architecture)

```mermaid
graph TD
    User[👤 用户 User] -->|交互 Interaction| Frontend[🖥️ 前端 Frontend (Vue 3)]
    Frontend -->|HTTP / SSE| Backend[⚙️ 后端 Backend (Spring Boot)]
    
    subgraph "Backend Core"
        Backend --> Controller[🎮 Controller Layer]
        Controller --> Service[🔧 Service Layer]
        Service --> Agent[🤖 Agent Core (ReAct)]
        Service --> RAG[📖 RAG Engine]
    end
    
    subgraph "Data & Model"
        Agent <-->|API Call| LLM[🧠 Alibaba Qwen LLM]
        Agent <-->|Search| Tools[🛠️ Tools (Web Search, etc)]
        RAG <-->|Retrieve| VectorDB[🗄️ Vector Store (In-Memory)]
        Backend <-->|CRUD| MySQL[🐬 MySQL Database]
    end
    
    VectorDB <--> Docs[📄 Local Knowledge Base]
```

## 📂 目录结构 (Directory Structure)

```
ai-code-helper/
├── ai-code-helper-frontend/      # 🖥️ 前端项目 (Frontend)
│   ├── src/
│   │   ├── api/                  # API 接口封装 (Chat, User, Session)
│   │   ├── assets/               # 静态资源 (CSS, Images)
│   │   ├── components/           # 公共组件
│   │   │   ├── chat/             # 聊天相关组件 (ChatArea, Message)
│   │   │   └── layout/           # 布局组件 (Sidebar, Footer)
│   │   ├── router/               # 路由配置 (Vue Router)
│   │   ├── stores/               # 状态管理 (Pinia - User, Chat)
│   │   └── views/                # 页面视图 (Home, Login, Workspace)
│   └── vite.config.js            # Vite 配置
│
├── sql/                          # 💾 数据库脚本 (Create Tables)
│
├── src/                          # ⚙️ 后端项目 (Backend)
│   ├── main/
│   │   ├── java/com/star/aicodehelper/
│   │   │   ├── agent/            # 🤖 智能体核心 (Agent Core)
│   │   │   │   ├── core/         # ReAct 引擎, ToolCall 逻辑
│   │   │   │   └── model/        # Agent 上下文与步骤模型
│   │   │   ├── ai/               # 🧠 AI 服务配置
│   │   │   │   ├── guardrail/    # 安全护栏 (Input Guard)
│   │   │   │   ├── model/        # 模型配置 (Qwen)
│   │   │   │   ├── rag/          # RAG 检索增强生成配置
│   │   │   │   └── tools/        # 工具实现 (Search, etc.)
│   │   │   ├── common/           # 通用类 (Result, Response)
│   │   │   ├── config/           # 全局配置 (Cors, WebMvc)
│   │   │   ├── controller/       # Web 接口层
│   │   │   ├── mapper/           # MyBatis Mapper 接口
│   │   │   ├── model/entity/     # 数据库实体类
│   │   │   └── service/          # 业务逻辑实现层
│   │   └── resources/
│   │       ├── docs/             # 📚 RAG 本地知识库 (Markdown)
│   │       ├── system-prompt-*.txt # 系统提示词模板
│   │       └── application.yml   # 项目配置文件
│   └── test/                     # 测试用例
│
└── pom.xml                       # Maven 依赖管理
```


## 📄 许可证 (License)
None，Only me
