<template>
  <div class="chat-message" :class="{ 'user-message': isUser, 'ai-message': !isUser }">
    <div class="message-avatar">
      <div class="avatar" :class="{ 'user-avatar': isUser, 'ai-avatar': !isUser }">
        {{ isUser ? '我' : 'AI' }}
      </div>
    </div>
    <div class="message-content">
      <div class="message-bubble">
        <div v-if="!isUser" class="message-header" @click="toggleCollapse">
           <span class="collapse-icon">{{ isCollapsed ? '▶' : '▼' }}</span>
           <span v-if="isCollapsed" class="collapsed-text">已折叠</span>
        </div>
        <!-- 用户消息使用普通文本 -->
        <pre v-if="isUser" class="message-text">{{ message }}</pre>
        <!-- AI回复使用Markdown渲染 -->
        <div v-else v-show="!isCollapsed" class="message-markdown" v-html="renderedMessage"></div>
      </div>
      <div class="message-time">{{ formatTime(timestamp) }}</div>
    </div>
  </div>
</template>

<script>
import { formatTime } from '../utils/index.js'
import { marked } from 'marked'

// 全局配置 marked，只需配置一次
marked.use({
  breaks: true,
  gfm: true
})

export default {
  name: 'ChatMessage',
  props: {
    message: {
      type: String,
      required: true
    },
    isUser: {
      type: Boolean,
      default: false
    },
    timestamp: {
      type: Date,
      default: () => new Date()
    }
  },
  data() {
    return {
      isCollapsed: false
    }
  },
  computed: {
    avatarText() {
      // 如果是用户，显示 'User'，否则显示 'AI'
      // 实际上这里应该根据 assistantType 显示不同的头像文字，但 ChatMessage 组件目前只接收 isUser
      // 父组件可以通过 slot 或新的 prop 传递头像，这里暂时保持简单
      return this.isUser ? 'Me' : 'AI'
    },
    renderedMessage() {
      if (this.isUser) {
        return this.message
      }
      
      let content = this.message;
      
      // 1. 修复列表项缺少空格的问题 (如 "-列表项" -> "- 列表项")
      content = content.replace(/^(\s*[-*+])([^\s])/gm, '$1 $2');
      
      // 2. 修复标题缺少空格的问题 (如 "###标题" -> "### 标题")
      content = content.replace(/^(#+)([^#\s])/gm, '$1 $2');
      
      // 3. 修复Markdown列表/标题前缺少换行的问题
      content = content.replace(/([^\n])\n(\s*[-*+]\s|\s*\d+\.\s|#+\s)/g, '$1\n\n$2');
      
      // 直接解析，不需要在实例上维护状态
      return marked.parse(content)
    }
  },
  methods: {
    formatTime,
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed
    }
  }
}
</script>

<style scoped>
.chat-message {
  display: flex;
  margin-bottom: 20px;
  padding: 0 20px;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  cursor: pointer;
  color: #666;
  font-size: 12px;
  user-select: none;
}

.collapse-icon {
  margin-right: 4px;
  font-size: 10px;
  transition: transform 0.2s;
  display: inline-block;
  width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  border-radius: 4px;
}

.message-header:hover .collapse-icon {
  background: rgba(0,0,0,0.05);
}

.collapsed-text {
  color: #999;
}

.user-message {
  justify-content: flex-end;
  flex-direction: row;
}

.user-message .message-avatar {
  order: 2;
}

.user-message .message-content {
  order: 1;
}

.ai-message {
  justify-content: flex-start;
  flex-direction: row;
}

.ai-message .message-avatar {
  order: 1;
}

.ai-message .message-content {
  order: 2;
}

.message-avatar {
  display: flex;
  align-items: flex-start;
  margin: 0 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.user-avatar {
  background-color: var(--primary-color, #007bff);
}

.ai-avatar {
  background: var(--ai-avatar-bg, #6c757d);
}

.message-content {
  max-width: 65%;
  min-width: 100px;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 12px;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05); /* 增加阴影 */
  min-width: 120px; /* 增加最小宽度防止只有几个字时抖动 */
}

.ai-message .message-bubble {
  background-color: var(--message-ai-bg, rgba(255, 255, 255, 0.95));
  color: var(--message-ai-text, #333);
  border-bottom-left-radius: 2px; /* 调整圆角 */
  border: 1px solid rgba(0,0,0,0.05);
  width: 100%; /* 让AI回复气泡始终占满可用宽度，避免抖动 */
  max-width: 100%; /* 防止溢出 */
}

.message-text {
  font-family: inherit;
  font-size: 14px;
  line-height: 1.4;
  white-space: pre-wrap;
  margin: 0;
}

/* Markdown样式 */
.message-markdown {
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
}

.message-markdown :deep(h1),
.message-markdown :deep(h2),
.message-markdown :deep(h3),
.message-markdown :deep(h4),
.message-markdown :deep(h5),
.message-markdown :deep(h6) {
  margin: 0.5em 0;
  font-weight: bold;
}

.message-markdown :deep(h1) { font-size: 1.5em; }
.message-markdown :deep(h2) { font-size: 1.3em; }
.message-markdown :deep(h3) { font-size: 1.2em; }
.message-markdown :deep(h4) { font-size: 1.1em; }
.message-markdown :deep(h5) { font-size: 1em; }
.message-markdown :deep(h6) { font-size: 0.9em; }

.message-markdown :deep(p) {
  margin: 0.5em 0;
}

.message-markdown :deep(ul),
.message-markdown :deep(ol) {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.message-markdown :deep(li) {
  margin: 0.2em 0;
}

.message-markdown :deep(code) {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.user-message .message-markdown :deep(code) {
  background-color: rgba(255, 255, 255, 0.2);
}

.message-markdown :deep(pre) {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 1em;
  border-radius: 5px;
  overflow-x: auto;
  margin: 0.5em 0;
}

.user-message .message-markdown :deep(pre) {
  background-color: rgba(255, 255, 255, 0.2);
}

.message-markdown :deep(pre code) {
  background-color: transparent;
  padding: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.message-markdown :deep(blockquote) {
  border-left: 4px solid #ccc;
  padding-left: 1em;
  margin: 0.5em 0;
  font-style: italic;
  color: #666;
}

.user-message .message-markdown :deep(blockquote) {
  border-left-color: rgba(255, 255, 255, 0.5);
  color: rgba(255, 255, 255, 0.8);
}

.message-markdown :deep(a) {
  color: #007bff;
  text-decoration: underline;
}

.user-message .message-markdown :deep(a) {
  color: #b3d9ff;
}

.message-markdown :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0.5em 0;
}

.message-markdown :deep(th),
.message-markdown :deep(td) {
  border: 1px solid #ddd;
  padding: 0.5em;
  text-align: left;
}

.message-markdown :deep(th) {
  background-color: #f2f2f2;
  font-weight: bold;
}

.user-message .message-markdown :deep(th) {
  background-color: rgba(255, 255, 255, 0.2);
}

.message-markdown :deep(hr) {
  border: none;
  border-top: 1px solid #ddd;
  margin: 1em 0;
}

/* 思考过程折叠样式 (复用 App.vue 样式) */
.message-markdown :deep(details) {
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 6px;
  margin: 8px 0 16px 0;
  padding: 0;
  border: 1px solid rgba(0,0,0,0.05); /* 添加边框以便更清晰 */
  /* overflow: hidden;  移除 overflow: hidden 以防内容被裁剪 */
}

.message-markdown :deep(summary) {
  padding: 8px 12px;
  cursor: pointer;
  font-weight: normal;
  font-size: 0.9em; /* 稍微调大字体 */
  color: #666; /* 加深颜色 */
  user-select: none;
  outline: none;
  transition: all 0.2s ease;
  background-color: transparent;
  display: flex;
  align-items: center;
  list-style: none;
  min-height: 24px; /* 确保有最小高度 */
}

.message-markdown :deep(summary)::before {
  content: "▶";
  display: inline-block;
  font-size: 0.8em;
  margin-right: 6px;
  transition: transform 0.2s ease;
  opacity: 0.6;
}

.message-markdown :deep(details[open] summary)::before {
  transform: rotate(90deg);
}

.message-markdown :deep(summary)::-webkit-details-marker {
  display: none;
}

.message-markdown :deep(summary):hover {
  color: #666;
  background-color: rgba(0, 0, 0, 0.02);
}

.message-markdown :deep(details[open] summary) {
  border-bottom: 1px dashed rgba(0, 0, 0, 0.05);
  background-color: rgba(0, 0, 0, 0.02);
}

.message-markdown :deep(details > div),
.message-markdown :deep(details > p),
.message-markdown :deep(details > ul),
.message-markdown :deep(details > ol) {
  padding: 4px 12px;
  margin: 0;
  color: #888;
  font-size: 0.85em;
  line-height: 1.5;
  background-color: transparent;
  border-top: none;
}

/* 针对引用块样式的思考过程进行特殊处理 */
.message-markdown :deep(details blockquote) {
  margin: 4px 0;
  padding: 4px 10px;
  border-left: 3px solid #ddd;
  background-color: rgba(0,0,0,0.01);
  color: #777;
}

/* 区分思考 */
.message-markdown :deep(details blockquote p:first-child:contains("🤔")) {
  border-left-color: #ffd700;
  background-color: rgba(255, 215, 0, 0.05);
}

/* 区分工具调用 */
.message-markdown :deep(details blockquote p:first-child:contains("🛠️")) {
  border-left-color: #007bff;
  background-color: rgba(0, 123, 255, 0.05);
}

/* 区分观察结果 */
.message-markdown :deep(details blockquote p:first-child:contains("👀")) {
  border-left-color: #28a745;
  background-color: rgba(40, 167, 69, 0.05);
}

/* 去除第一个元素的 border-top */
.message-markdown :deep(details > *:nth-child(2)) {
    border-top: none;
}

.user-message .message-markdown :deep(hr) {
  border-top-color: rgba(255, 255, 255, 0.3);
}

.message-time {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  padding: 0 4px;
}

.user-message .message-time {
  text-align: right;
}

.ai-message .message-time {
  text-align: left;
}

@media (max-width: 768px) {
  .message-content {
    max-width: 90%;
  }
  
  .chat-message {
    padding: 0 10px;
  }
}
</style> 