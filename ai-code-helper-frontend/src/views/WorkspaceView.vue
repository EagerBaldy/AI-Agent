<template>
  <div class="workspace-view" :style="cssVars">
    <Sidebar />
    
    <div class="main-content">
      <div class="app-header">
        <div class="header-left">
          <h1 class="app-title">{{ currentTitle }}</h1>
          <div class="app-subtitle">{{ currentSubtitle }}</div>
        </div>
        <div class="header-right">
          <div class="header-search">
            <span class="search-icon">🔍</span>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索当前对话..." 
              @keyup.enter="handleSearch"
            />
          </div>
        </div>
      </div>
      
      <ChatArea :mode="mode" />
    </div>

    <!-- Connection Error Toast -->
    <div v-if="chatStore.connectionError" class="connection-error">
      <div class="error-content">
        <span class="error-icon">⚠️</span>
        <span>连接服务器失败，请检查后端服务是否启动</span>
      </div>
    </div>
    
    <!-- 会话内搜索结果弹窗 -->
    <SearchResultModal 
      v-model:visible="isSearchModalVisible"
      :results="searchResults"
      :isLoading="isSearching"
      @select="onSearchResultSelect"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from '../components/layout/Sidebar.vue'
import ChatArea from '../components/chat/ChatArea.vue'
import SearchResultModal from '../components/chat/SearchResultModal.vue'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'
import { themes } from '../utils/themes'
import { searchSessionChat } from '../api/chatApi'

const props = defineProps({
  mode: String
})

const router = useRouter()
const route = useRoute()
const chatStore = useChatStore()
const userStore = useUserStore()
const searchQuery = ref('')
const isSearchModalVisible = ref(false)
const isSearching = ref(false)
const searchResults = ref([])

const handleSearch = async () => {
  if (!searchQuery.value.trim() || !chatStore.currentSession) return
  
  isSearchModalVisible.value = true
  isSearching.value = true
  searchResults.value = []
  
  try {
    const results = await searchSessionChat(chatStore.currentSession.id, searchQuery.value)
    searchResults.value = results
  } catch (error) {
    console.error('会话内搜索失败', error)
  } finally {
    isSearching.value = false
  }
}

const onSearchResultSelect = (item) => {
  // 会话内搜索点击，设置高亮消息ID，并让 ChatArea 组件响应
  if (item.id) {
    chatStore.highlightMessageId = item.id
  }
}

// Initialize
const initWorkspace = async () => {
  if (props.mode) {
    await chatStore.loadSessionsForMode(props.mode)
    
    // 检查是否有目标会话 ID
    const targetSessionId = route.query.targetSessionId
    
    if (targetSessionId) {
      // 如果目标 session 就是当前的 session，直接清理 URL 返回
      if (chatStore.currentSession && String(chatStore.currentSession.id) === String(targetSessionId)) {
        // 清理 url 上的 query，保持整洁
        const query = { ...route.query }
        delete query.targetSessionId
        router.replace({ query })
        return
      }

      // 尝试在当前模式的会话列表中查找该会话
      const targetSession = chatStore.sessions.find(s => String(s.id) === String(targetSessionId))
      
      if (targetSession) {
        await chatStore.switchSession(targetSession)
        
        // 滚动和高亮处理交由 ChatArea 监听 highlightMessageId 实现
        
        // 清理 url 上的 query，保持整洁
        const query = { ...route.query }
        delete query.targetSessionId
        router.replace({ query })
        
        return
      } else {
        // 如果在当前模式的会话列表中没找到该 session：
        // 尝试强行将其加入列表并加载
        console.warn('未在当前模式找到目标会话，尝试强制加载该历史记录', targetSessionId)
        
        // 从全局搜索跳转过来时，可能只携带了 targetSessionId，我们可以创建一个带 ID 的对象传给 switchSession
        // switchSession 内部会去加载它的历史记录，如果它不在当前侧边栏列表中，也会被临时加进去
        await chatStore.switchSession({ id: targetSessionId, name: `会话 ${targetSessionId}` })
        
        const query = { ...route.query }
        delete query.targetSessionId
        router.replace({ query })
        return
      }
    }
    
    // 如果没有目标会话，执行正常的初始化逻辑
    if (chatStore.sessions.length > 0) {
      if (!chatStore.currentSession || !chatStore.sessions.find(s => s.id === chatStore.currentSession.id)) {
        await chatStore.switchSession(chatStore.sessions[0])
      }
    } else {
      await chatStore.createNewSession()
    }
  }
}

onMounted(() => {
  initWorkspace()
})

watch(() => props.mode, (newMode) => {
  if (newMode) {
    initWorkspace()
  }
})

watch(() => route.query.targetSessionId, (newId) => {
  if (newId) {
    initWorkspace()
  }
})

// UI Helpers
const currentTitle = computed(() => {
  const map = {
    code: 'AI 编程小助手',
    travel: 'AI 旅游规划师',
    essay: 'AI 作文老师',
    material: 'AI 素材百科',
    medical: 'AI 医疗助手',
    college: 'AI 升学规划助手'
  }
  return map[props.mode] || ''
})

const currentSubtitle = computed(() => {
  const map = {
    code: '帮助您解答编程学习和求职面试相关问题',
    travel: '为您量身定制完美旅程',
    essay: '作文批改 · 思路拓展 · 提分技巧',
    material: '名人名言 · 经典素材 · 范文检索',
    medical: '健康咨询 · 症状初筛 · 预防建议',
    college: '专业解读 · 院校对比 · 路径规划'
  }
  return map[props.mode] || ''
})

const cssVars = computed(() => {
  const theme = themes[props.mode] || themes.code
  return {
    '--primary-color': theme.primary,
    '--ai-avatar-bg': theme.avatarBg,
    '--header-bg': userStore.isDarkMode ? '#2d2d2d' : theme.headerBg,
    '--chat-bg': userStore.isDarkMode ? '#1a1a1a' : theme.chatBg,
    
    // Global overrides based on dark mode
    '--bg-body': userStore.isDarkMode ? '#1a1a1a' : '#f8f9fa',
    '--bg-sidebar': userStore.isDarkMode ? '#000000' : '#f0f2f5',
    '--text-primary': userStore.isDarkMode ? '#e0e0e0' : '#2c3e50',
    '--text-secondary': userStore.isDarkMode ? '#a0a0a0' : '#666',
    '--text-sidebar': userStore.isDarkMode ? '#ffffff' : '#333333',
    '--text-sidebar-secondary': userStore.isDarkMode ? '#a0a0a0' : '#666666',
    '--border-color': userStore.isDarkMode ? '#444' : 'rgba(0,0,0,0.05)',
    '--border-sidebar': userStore.isDarkMode ? '#333' : '#e0e0e0',
    '--hover-sidebar': userStore.isDarkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)',
    '--card-bg': userStore.isDarkMode ? '#2d2d2d' : 'white',
    '--input-bg': userStore.isDarkMode ? '#2d2d2d' : 'white',
    '--input-text': userStore.isDarkMode ? '#e0e0e0' : '#333',
    '--input-border': userStore.isDarkMode ? '#444' : '#ddd',
    '--message-user-bg': theme.primary,
    '--message-ai-bg': userStore.isDarkMode ? '#2d2d2d' : '#f1f3f5',
    '--message-ai-text': userStore.isDarkMode ? '#e0e0e0' : '#333',
    '--shadow-color': userStore.isDarkMode ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.05)'
  }
})
</script>

<style scoped>
.workspace-view {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: var(--bg-body);
  color: var(--text-primary);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  overflow: hidden;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-body);
  position: relative;
  min-height: 0;
}

.app-header {
  padding: 20px 30px;
  border-bottom: 1px solid var(--border-color);
  background: var(--header-bg);
  z-index: 10;
  position: relative;
  transition: background 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-search {
  display: flex;
  align-items: center;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 6px 16px;
  transition: all 0.3s ease;
}

.header-search:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 1px var(--primary-color);
}

.header-search .search-icon {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-right: 8px;
}

.header-search input {
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 0.9rem;
  width: 200px;
  transition: width 0.3s ease;
}

.header-search input:focus {
  width: 250px;
}

.header-search input::placeholder {
  color: var(--text-secondary);
  opacity: 0.7;
}

.app-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  text-shadow: 0 1px 2px rgba(255,255,255,0.1);
}

.app-subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 4px;
  text-shadow: 0 1px 2px rgba(255,255,255,0.1);
}

.connection-error {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #ff6b6b;
  color: white;
  padding: 12px 24px;
  border-radius: 50px;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  z-index: 100;
  font-weight: 500;
}

@media (max-width: 768px) {
  .workspace-view {
    flex-direction: column;
  }
}
</style>
