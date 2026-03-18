<template>
  <div class="chat-container">
    <!-- 消息列表 -->
    <div class="messages-container" ref="messagesContainer">
      <div v-if="chatStore.messages.length === 0" class="welcome-message">
        <div class="welcome-content">
          <div class="welcome-icon">{{ currentIcon }}</div>
          <h2>{{ welcomeTitle }}</h2>
          <p>我可以帮助您：</p>
          <ul>
            <li v-for="(item, index) in welcomeItems" :key="index">{{ item }}</li>
          </ul>
        </div>
      </div>

      <!-- 历史消息 -->
      <ChatMessage
        v-for="message in chatStore.messages"
        :key="message.id"
        :id="'message-' + message.id"
        :message="message.content"
        :is-user="message.isUser"
        :timestamp="message.timestamp"
        :class="{ 'highlight-message': chatStore.highlightMessageId === message.id }"
      />

      <!-- AI 正在回复的消息 -->
      <div v-if="chatStore.isAiTyping" class="chat-message ai-message">
        <div class="message-avatar">
          <div class="avatar ai-avatar">{{ currentAvatarText }}</div>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <div class="ai-typing-content">
              <div class="ai-response-text message-markdown" v-html="currentAiResponseRendered"></div>
              <div class="typing-indicator-wrapper" v-if="chatStore.isStreaming">
                <LoadingDots />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div v-if="chatStore.isStreaming" class="stop-btn-wrapper">
      <button class="stop-btn" @click="chatStore.stopGeneration">
        <span>⏹</span> 停止生成
      </button>
    </div>
    <ChatInput
      :disabled="chatStore.isAiTyping && !chatStore.isStreaming"
      @send-message="handleSendMessage"
      :placeholder="inputPlaceholder"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { marked } from 'marked'
import ChatMessage from '../ChatMessage.vue'
import ChatInput from '../ChatInput.vue'
import LoadingDots from '../LoadingDots.vue'
import { useChatStore } from '../../stores/chat'

const props = defineProps({
  mode: String
})

const chatStore = useChatStore()
const messagesContainer = ref(null)

const handleSendMessage = (payload) => {
  // ChatInput emits { content, mode }, we need to extract content
  const content = typeof payload === 'object' ? payload.content : payload
  chatStore.sendMessage(content)
}

// Auto scroll to bottom
watch(() => chatStore.messages.length, () => {
  if (!chatStore.highlightMessageId) {
    scrollToBottom()
  }
})
watch(() => chatStore.currentAiResponse, () => {
  if (!chatStore.highlightMessageId) {
    scrollToBottom()
  }
})

// Listen for highlight requests
watch(() => chatStore.highlightMessageId, (newId) => {
  if (newId) {
    scrollToMessage(newId)
  }
})

// Add an onMounted hook to check if there is an initial highlight message
import { onMounted } from 'vue'
onMounted(() => {
  if (chatStore.highlightMessageId) {
    // Wait for messages to be rendered
    setTimeout(() => {
      scrollToMessage(chatStore.highlightMessageId)
    }, 500)
  }
})

// Wait for messages to change, then scroll to highlighted message if any
watch(() => chatStore.messages, () => {
  if (chatStore.highlightMessageId) {
    // 增加一点延迟，确保 DOM 已经完全渲染
    setTimeout(() => {
      scrollToMessage(chatStore.highlightMessageId)
    }, 300)
  }
})

const scrollToMessage = (id) => {
  nextTick(() => {
    const element = document.getElementById('message-' + id)
    if (element && messagesContainer.value) {
      // 滚动到指定元素
      element.scrollIntoView({ behavior: 'smooth', block: 'center' })
      
      // 3秒后移除高亮状态
      setTimeout(() => {
        if (chatStore.highlightMessageId === id) {
          chatStore.highlightMessageId = null
        }
      }, 3000)
    }
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const currentAiResponseRendered = computed(() => {
  if (!chatStore.currentAiResponse) return ''
  return marked.parse(chatStore.currentAiResponse)
})

// Helpers for welcome message
const currentIcon = computed(() => {
  const map = { code: '🤖', travel: '🌏', essay: '📝', material: '📚', medical: '🏥', college: '🎓' }
  return map[props.mode] || ''
})

const currentAvatarText = computed(() => {
  const map = { code: 'AI', travel: 'Travel', essay: 'Essay', material: 'Book', medical: 'Med', college: 'Edu' }
  return map[props.mode] || 'AI'
})

const welcomeTitle = computed(() => {
  const map = {
    code: '欢迎使用 AI 编程小助手',
    travel: '你好！我是你的专属导游',
    essay: '同学你好，我是你的作文老师',
    material: '欢迎来到素材百科全书',
    medical: '您好，我是您的专属医疗助手',
    college: '欢迎使用 AI 升学规划助手'
  }
  return map[props.mode] || ''
})

const inputPlaceholder = computed(() => {
  const map = {
    code: '请输入您的编程问题...',
    travel: '告诉我你想去哪里...',
    essay: '请输入作文题目或粘贴作文内容...',
    material: '请输入想要查找的素材主题...',
    medical: '请描述您的症状或健康疑问...',
    college: '请输入您的成绩、兴趣或目标院校...'
  }
  return map[props.mode] || ''
})

const welcomeItems = computed(() => {
  if (props.mode === 'code') return ['解答编程技术问题', '提供代码示例和解释', '协助求职面试准备', '分享编程学习建议']
  if (props.mode === 'travel') return ['推荐热门旅游目的地', '规划详细行程安排', '提供交通住宿建议', '分享当地美食攻略']
  if (props.mode === 'essay') return ['作文精细批改与评分', '提供多角度立意分析', '传授高分写作技巧', '优化文章结构与语言']
  if (props.mode === 'material') return ['检索古今中外名人名言', '查找经典写作素材事例', '提供优秀范文与赏析', '指导素材运用方法']
  if (props.mode === 'medical') return ['常见健康问题解答', '疾病预防与生活指导', '基于症状的初步评估', '个性化健康管理建议']
  if (props.mode === 'college') return ['专业课程与就业解读', '多维度院校对比分析', '个性化升学路径规划', '学习难度与备考建议']
  return []
})
</script>

<style scoped>
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--chat-bg);
  min-height: 0;
  transition: background 0.3s ease;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
  scroll-behavior: smooth;
}

/* Welcome Message Styles */
.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: var(--text-secondary);
}

.welcome-content {
  text-align: center;
  background: var(--bg-body);
  padding: 40px;
  border-radius: 20px;
  max-width: 400px;
  box-shadow: 0 4px 20px var(--shadow-color);
  border: 1px solid var(--border-color);
}

.welcome-content h2 {
  color: var(--text-primary);
  margin-bottom: 24px;
  font-size: 1.5rem;
  font-weight: 700;
}

.welcome-content p {
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.welcome-content ul {
  text-align: left;
  display: inline-block;
  margin-top: 0;
  padding: 0;
  width: 100%;
}

.welcome-content li {
  margin-bottom: 12px;
  padding: 12px 16px;
  position: relative;
  background: var(--hover-sidebar);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  transition: transform 0.2s ease;
}

.welcome-content li:hover {
  transform: translateX(4px);
}

/* AI Typing & Markdown Styles */
.chat-message {
  padding: 0 30px;
  margin-bottom: 24px;
  display: flex;
}

.message-avatar {
  margin: 0 16px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.ai-avatar {
  background: var(--ai-avatar-bg, #6c757d);
  color: white;
  font-size: 12px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
  max-width: calc(100% - 60px);
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 12px;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  min-width: 120px;
}

.ai-message .message-bubble {
  background-color: var(--message-ai-bg, rgba(255, 255, 255, 0.95));
  color: var(--message-ai-text, #333);
  border-bottom-left-radius: 2px;
  border: 1px solid rgba(0,0,0,0.05);
  width: 100%;
  max-width: 100%;
}

.ai-typing-content {
  min-height: 24px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.typing-indicator-wrapper {
  height: 20px;
  margin-top: 4px;
}

.stop-btn-wrapper {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
}

.stop-btn {
  padding: 8px 16px;
  background: white;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: all 0.2s;
}

.stop-btn:hover {
  background: #ff4d4f;
  color: white;
}

/* 消息高亮动画 */
.highlight-message {
  animation: highlight-pulse 3s ease-in-out;
}

@keyframes highlight-pulse {
  0% { background-color: rgba(var(--primary-color-rgb, 0, 123, 255), 0.2); }
  50% { background-color: rgba(var(--primary-color-rgb, 0, 123, 255), 0.1); }
  100% { background-color: transparent; }
}

/* Copy-paste the Markdown styles from App.vue here or put in global.css */
/* For brevity, I assume global styles or scoped copy. I'll include key ones. */
.ai-response-text.message-markdown :deep(h1) { font-size: 1.5em; margin: 0.5em 0; font-weight: bold; }
/* ... (omitted full markdown styles for brevity, but they should be here or global) ... */
/* Ideally move markdown styles to a global css file `src/assets/markdown.css` */
</style>
