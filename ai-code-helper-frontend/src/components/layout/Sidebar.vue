<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <div class="logo">AI</div>
    </div>
    
    <!-- 会话列表 -->
    <div class="sessions-container">
      <div class="sessions-header">
        <span>会话列表</span>
        <button class="add-session-btn" @click="chatStore.createNewSession">+</button>
      </div>
      <div class="sessions-list">
        <div 
          v-for="session in chatStore.sessions" 
          :key="session.id"
          class="session-item"
          :class="{ active: chatStore.currentSession && chatStore.currentSession.id === session.id }"
          @click="chatStore.switchSession(session)"
        >
          <div v-if="editingSessionId === session.id" class="session-edit-wrapper" @click.stop>
            <input 
              ref="editInput"
              v-model="editingSessionName"
              class="session-edit-input"
              @blur="saveRename(session)"
              @keyup.enter="saveRename(session)"
              @keyup.esc="cancelRename"
            />
          </div>
          <div v-else-if="deletingSessionId === session.id" class="session-confirm-wrapper" @click.stop>
            <span class="confirm-text">确定删除?</span>
            <button class="action-btn confirm-btn" @click="confirmDelete(session)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
            </button>
            <button class="action-btn cancel-btn" @click="cancelDelete">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
          </div>
          <span v-else class="session-name" :title="session.name">{{ session.name }}</span>
          <div v-if="editingSessionId !== session.id && deletingSessionId !== session.id" class="session-actions">
            <button class="action-btn" @click="startRename(session, $event)" title="重命名">✏️</button>
            <button class="action-btn" @click="startDelete(session, $event)" title="删除">🗑️</button>
          </div>
        </div>
      </div>
    </div>

    <div class="nav-items">
      <div class="nav-item" :class="{ active: currentMode === 'code' }" @click="switchMode('code')">
        <span class="nav-icon">💻</span><span class="nav-text">编程助手</span>
      </div>
      <div class="nav-item" :class="{ active: currentMode === 'travel' }" @click="switchMode('travel')">
        <span class="nav-icon">✈️</span><span class="nav-text">旅游助手</span>
      </div>
      <div class="nav-item" :class="{ active: currentMode === 'essay' }" @click="switchMode('essay')">
        <span class="nav-icon">📝</span><span class="nav-text">作文老师</span>
      </div>
      <div class="nav-item" :class="{ active: currentMode === 'material' }" @click="switchMode('material')">
        <span class="nav-icon">📚</span><span class="nav-text">素材百科</span>
      </div>
      <div class="nav-item" :class="{ active: currentMode === 'medical' }" @click="switchMode('medical')">
        <span class="nav-icon">🏥</span><span class="nav-text">医疗助手</span>
      </div>
      <div class="nav-item" :class="{ active: currentMode === 'college' }" @click="switchMode('college')">
        <span class="nav-icon">🎓</span><span class="nav-text">升学规划</span>
      </div>
    </div>
    
    <div class="sidebar-footer">
      <div class="footer-actions">
        <div class="nav-item back-home" @click="router.push('/')">
          <span class="nav-icon">🏠</span><span class="nav-text">返回首页</span>
        </div>
        <button class="theme-toggle-btn" @click="userStore.toggleTheme" :title="userStore.isDarkMode ? '当前：深色模式 (点击切换)' : '当前：浅色模式 (点击切换)'">
          {{ userStore.isDarkMode ? '🌙' : '☀️' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useChatStore } from '../../stores/chat'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const chatStore = useChatStore()
const userStore = useUserStore()

const currentMode = computed(() => route.params.mode)

const editingSessionId = ref(null)
const editingSessionName = ref('')
const deletingSessionId = ref(null)
const editInput = ref(null)

const switchMode = (mode) => {
  router.push(`/workspace/${mode}`)
}

const startRename = (session, event) => {
  if (event) event.stopPropagation()
  editingSessionId.value = session.id
  editingSessionName.value = session.name
  nextTick(() => {
    // Handle array ref if inside v-for
    if (Array.isArray(editInput.value)) {
      editInput.value[0]?.focus()
    } else {
      editInput.value?.focus()
    }
  })
}

const cancelRename = () => {
  editingSessionId.value = null
  editingSessionName.value = ''
}

const saveRename = async (session) => {
  if (!editingSessionId.value) return
  const newName = editingSessionName.value.trim()
  if (newName && newName !== session.name) {
    await chatStore.renameSession(session, newName)
  }
  cancelRename()
}

const startDelete = (session, event) => {
  if (event) event.stopPropagation()
  deletingSessionId.value = session.id
}

const cancelDelete = () => {
  deletingSessionId.value = null
}

const confirmDelete = async (session) => {
  await chatStore.deleteSession(session)
  cancelDelete()
}
</script>

<style scoped>
.sidebar {
  width: 260px;
  background: var(--bg-sidebar);
  color: var(--text-sidebar);
  display: flex;
  flex-direction: column;
  padding: 20px 16px;
  flex-shrink: 0;
  overflow-y: auto;
  transition: background-color 0.3s ease, color 0.3s ease;
  border-right: 1px solid var(--border-sidebar);
}

.sidebar::-webkit-scrollbar,
.sessions-list::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track,
.sessions-list::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb,
.sessions-list::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.2);
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover,
.sessions-list::-webkit-scrollbar-thumb:hover {
  background-color: rgba(128, 128, 128, 0.4);
}

.sidebar-header {
  padding: 10px 10px;
  margin-bottom: 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(45deg, var(--primary-color, #00b4db), #0083b0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.sessions-container {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-sidebar);
}

.sessions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px 10px;
  color: var(--text-sidebar-secondary);
  font-size: 12px;
}

.add-session-btn {
  background: none;
  border: 1px solid var(--border-sidebar);
  color: var(--text-sidebar-secondary);
  border-radius: 4px;
  width: 20px;
  height: 20px;
  line-height: 18px;
  text-align: center;
  cursor: pointer;
  padding: 0;
}

.add-session-btn:hover {
  border-color: var(--text-sidebar);
  color: var(--text-sidebar);
}

.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-sidebar-secondary);
  font-size: 13px;
  transition: background 0.2s, color 0.2s;
  position: relative;
  height: 36px;
}

.session-item:hover,
.session-item.active {
  background-color: var(--hover-sidebar);
  color: var(--text-sidebar);
}

.session-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  margin-right: 8px;
  transition: opacity 0.2s;
}

.session-item:hover .session-name {
  mask-image: linear-gradient(to right, black 70%, transparent 100%);
}

.session-actions {
  display: none;
  gap: 4px;
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: var(--bg-sidebar);
  padding-left: 8px;
}

.session-item:hover .session-actions {
  display: flex;
}

.session-confirm-wrapper {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: var(--bg-sidebar);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  gap: 8px;
  border-radius: 8px;
  z-index: 2;
}

.confirm-text {
  font-size: 12px;
  color: #ff6b6b;
  margin-right: auto;
  padding-left: 10px;
}

.confirm-btn {
  color: #ff6b6b !important;
  font-weight: bold;
  font-size: 14px !important;
  padding: 0 !important;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 107, 107, 0.1) !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-btn:hover {
  background: rgba(255, 107, 107, 0.2) !important;
}

.cancel-btn {
  color: #ccc !important;
  font-size: 14px !important;
  padding: 0 !important;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1) !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 10px;
  padding: 2px;
  opacity: 0.6;
}

.action-btn:hover {
  opacity: 1;
}

.nav-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-sidebar-secondary);
}

.nav-item:hover {
  background: var(--hover-sidebar);
  color: var(--text-sidebar);
}

.nav-item.active {
  background: linear-gradient(90deg, var(--primary-color), rgba(0,0,0,0));
  color: white;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.nav-icon {
  margin-right: 12px;
  font-size: 1.2rem;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid var(--border-sidebar);
}

.back-home {
  flex: 1;
  margin-top: 0;
  border-top: none;
  padding-top: 0;
}

.theme-toggle-btn {
  background: var(--hover-sidebar);
  border: 1px solid var(--border-sidebar);
  color: var(--text-sidebar);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.2s;
}

.theme-toggle-btn:hover {
  background: var(--border-sidebar);
  transform: rotate(15deg);
}
</style>
