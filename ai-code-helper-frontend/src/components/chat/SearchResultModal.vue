<template>
  <div v-if="visible" class="modal-overlay" @click.self="closeModal">
    <div class="modal-container">
      <div class="modal-header">
        <h2>搜索结果</h2>
        <button class="close-btn" @click="closeModal">×</button>
      </div>
      
      <div class="modal-body">
        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>正在搜索...</p>
        </div>
        
        <div v-else-if="results.length === 0" class="empty-state">
          <p>未找到相关聊天记录</p>
        </div>
        
        <div v-else class="results-list">
          <div 
            v-for="(item, index) in results" 
            :key="item.id || index" 
            class="result-item"
            @click="handleSelect(item)"
          >
            <div class="result-meta">
              <span class="role-badge" :class="item.role === 'user' ? 'user-role' : 'ai-role'">
                {{ item.role === 'user' ? '我' : 'AI' }}
              </span>
              <span class="time">{{ formatDate(item.createTime) }}</span>
              <span class="session-name" v-if="item.sessionId">· 会话ID: {{ item.sessionId }}</span>
            </div>
            <!-- 如果后端返回高亮文本则使用 v-html，否则显示纯文本内容 -->
            <div class="result-content" v-html="item.content"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  results: {
    type: Array,
    default: () => []
  },
  isLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'select'])

const closeModal = () => {
  emit('update:visible', false)
}

const handleSelect = (item) => {
  emit('select', item)
  closeModal()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-container {
  background: var(--card-bg, white);
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color, #eee);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-primary, #333);
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary, #999);
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #ff6b6b;
}

.modal-body {
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary, #666);
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid var(--border-color, #eee);
  border-top-color: var(--primary-color, #007bff);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  padding: 16px;
  border-radius: 12px;
  background: var(--bg-body, #f8f9fa);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.result-item:hover {
  background: var(--hover-sidebar, #f0f2f5);
  border-color: var(--primary-color, #007bff);
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 0.85rem;
  color: var(--text-secondary, #666);
}

.role-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
  font-size: 0.75rem;
}

.user-role {
  background: rgba(33, 147, 176, 0.1);
  color: #2193b0;
}

.ai-role {
  background: rgba(108, 92, 231, 0.1);
  color: #6c5ce7;
}

.result-content {
  font-size: 0.95rem;
  line-height: 1.5;
  color: var(--text-primary, #333);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 高亮样式 (处理后端返回的 <span style='color: red'>) */
:deep(span[style*='color: red']) {
  color: #ff4757 !important;
  font-style: normal;
  font-weight: bold;
  background: rgba(255, 71, 87, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}
</style>
