# AI Studio - 智能对话助手

基于 Spring Boot 3.x + Vue 3 + Spring AI 的多模型 AI 对话平台，支持动态切换模型、个性化人设配置。

## 功能特性

- **多模型支持**：Deepseek、OpenAI、智谱、通义、Moonshot、Anthropic (Claude) 等
- **动态切换**：聊天时实时切换不同模型，无需重启服务
- **个性化对话**：根据用户个人信息提供定制化回答
- **AI 人设配置**：自定义角色、性格、说话风格、系统提示词
- **会话管理**：多会话支持，历史消息保存
- **流式输出**：SSE 实时流式响应
- **亮暗双主题**：支持主题切换，自动保存偏好
- **安全存储**：API Key 存储在数据库，不在代码中明文显示

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.5 + Spring AI 1.0.0-M6 + MyBatis-Plus 3.5.7 |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia |
| 数据库 | MySQL 8.x |
| AI 模型 | Deepseek / OpenAI / Anthropic 等 |

## 项目结构

```
SpringAI_demo/
├── src/main/java/com/zyp/springai/
│   ├── SpringAiDemoApplication.java      # 启动类
│   ├── config/
│   │   ├── DynamicChatClientFactory.java # 动态模型工厂
│   │   ├── CorsConfig.java              # 跨域配置
│   │   └── MyMetaObjectHandler.java     # 自动填充
│   ├── controller/
│   │   ├── ChatController.java          # 对话接口
│   │   ├── ChatSessionController.java   # 会话管理
│   │   ├── UserProfileController.java   # 用户信息
│   │   ├── AiConfigController.java      # AI 人设配置
│   │   └── AiModelConfigController.java # 模型配置
│   ├── entity/                          # 实体类
│   ├── mapper/                          # MyBatis Mapper
│   └── service/                         # 业务逻辑
├── src/main/resources/
│   └── application.yml                  # 配置文件
├── frontend/                            # Vue 前端
│   └── src/
│       ├── views/                       # 页面组件
│       │   ├── ChatView.vue            # 主对话界面
│       │   ├── UserProfile.vue         # 个人信息
│       │   ├── AiConfigView.vue        # AI 人设配置
│       │   └── AiModelConfigView.vue   # 模型配置
│       ├── stores/chat.ts              # Pinia 状态管理
│       └── api/index.ts                # API 封装
└── sql/init.sql                         # 数据库初始化脚本
```

## 快速开始

### 1. 环境要求

- Java 21+
- Node.js 18+
- MySQL 8.x

### 2. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 3. 启动后端

```bash
# 设置 MySQL 密码（如果不是 root）
export MYSQL_PASSWORD=your_password

# 编译并运行
mvn clean package
mvn spring-boot:run

# 或直接运行 jar
java -jar target/SpringAI_demo-1.0-SNAPSHOT.jar
```

后端默认运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 5. 配置 AI 模型

1. 访问 `http://localhost:5173/ai-model-config`
2. 点击"添加模型"
3. 填入模型信息：
   - **模型名称**：显示名称（如 "Deepseek Chat"）
   - **提供商**：OpenAI 兼容 或 Anthropic
   - **API Key**：你的 API 密钥
   - **Base URL**：API 地址
   - **模型 ID**：实际模型标识（如 `deepseek-chat`）
4. 保存并设为默认

## 支持的模型

| 提供商 | Base URL | 模型示例 |
|--------|----------|----------|
| Deepseek | `https://api.deepseek.com` | deepseek-chat, deepseek-coder |
| OpenAI | `https://api.openai.com` | gpt-4, gpt-3.5-turbo |
| 智谱 | `https://open.bigmodel.cn/api/paas/v4` | glm-4, glm-4-flash |
| 通义 | `https://dashscope.aliyuncs.com/compatible-mode/v1` | qwen-turbo, qwen-max |
| Moonshot | `https://api.moonshot.cn/v1` | moonshot-v1-8k |
| Anthropic | `https://api.anthropic.com` | claude-3-opus, claude-3-sonnet |

所有兼容 OpenAI API 的模型均可通过配置接入。

## 页面说明

| 页面 | 路径 | 功能 |
|------|------|------|
| 对话界面 | `/chat` | 主聊天界面，支持模型切换 |
| 个人信息 | `/profile` | 编辑用户资料 |
| AI 人设 | `/ai-config` | 配置 AI 角色、性格、提示词 |
| 模型配置 | `/ai-model-config` | 管理 AI 模型（增删改查） |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/chat/stream` | 流式对话 (SSE) |
| POST | `/api/chat/send` | 普通对话 |
| GET | `/api/chat/messages/{sessionId}` | 获取历史消息 |
| POST | `/api/session` | 创建会话 |
| GET | `/api/session/list/{userId}` | 获取会话列表 |
| DELETE | `/api/session/{id}` | 删除会话 |
| POST | `/api/user/profile` | 创建/更新用户 |
| GET | `/api/user/profile/{id}` | 获取用户信息 |
| GET | `/api/ai-config` | 获取 AI 人设配置 |
| PUT | `/api/ai-config` | 更新 AI 人设配置 |
| GET | `/api/ai-model-config` | 获取所有模型配置 |
| GET | `/api/ai-model-config/enabled` | 获取启用的模型 |
| POST | `/api/ai-model-config` | 新增模型配置 |
| PUT | `/api/ai-model-config/{id}` | 更新模型配置 |
| DELETE | `/api/ai-model-config/{id}` | 删除模型配置 |
| POST | `/api/ai-model-config/{id}/set-default` | 设为默认模型 |
| POST | `/api/ai-model-config/{id}/test` | 测试连接 |

## 配置说明

### application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_ai_demo
    username: root
    password: ${MYSQL_PASSWORD:root}
```

所有 AI 配置（API Key、Base URL、Model）均存储在数据库中，通过网页管理。

## 开发说明

### 后端开发

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package
```

### 前端开发

```bash
cd frontend

# 开发模式
npm run dev

# 类型检查
npm run build

# 预览构建结果
npm run preview
```

## 许可证

MIT
