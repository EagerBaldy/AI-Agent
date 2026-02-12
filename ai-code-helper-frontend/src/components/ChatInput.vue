<template>
  <div class="chat-input">
    <div class="input-container">
      <div class="mode-switch">
        <button 
          class="mode-btn" 
          :class="{ active: inputMode === 'chat' }"
          @click="inputMode = 'chat'"
        >Ask</button>
        <div class="mode-divider"></div>
        <button 
          class="mode-btn" 
          :class="{ active: inputMode === 'agent' }"
          @click="inputMode = 'agent'"
        >Agent</button>
      </div>
      <textarea 
        v-model="inputMessage" 
        @keydown.enter.prevent="handleEnter"
        placeholder="输入您的问题..." 
        class="input-textarea"
        rows="1"
        ref="textarea"
      ></textarea>
      <button 
        class="send-btn" 
        @click="sendMessage"
        :disabled="!inputMessage.trim() || isLoading"
      >
        <span v-if="isLoading" class="loading-spinner"></span>
        <span v-else>↑</span>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    isLoading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      inputMessage: '',
      inputMode: 'chat' // 'chat' or 'agent'
    }
  },
  methods: {
    handleEnter(e) {
      if (!e.shiftKey) {
        this.sendMessage()
      }
    },
    sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return
      
      this.$emit('send-message', {
        content: this.inputMessage,
        mode: this.inputMode
      })
      this.inputMessage = ''
      this.$nextTick(() => {
        this.adjustHeight()
      })
    },
    adjustHeight() {
      const textarea = this.$refs.textarea
      if (textarea) {
        textarea.style.height = 'auto'
        textarea.style.height = textarea.scrollHeight + 'px'
      }
    }
  },
  watch: {
    inputMessage() {
      this.$nextTick(() => {
        this.adjustHeight()
      })
    }
  }
}
</script>

<style scoped>
.chat-input {
  padding: 20px;
  background-color: var(--bg-body, white);
  border-top: 1px solid var(--border-color, #e1e5e9);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 800px;
  margin: 0 auto;
}

.mode-switch {
  display: flex;
  align-items: center;
  background-color: var(--input-bg, #f5f5f5);
  border: 1px solid var(--input-border, #ddd);
  border-radius: 20px;
  padding: 4px;
  margin-bottom: 4px;
  height: 36px;
}

.mode-btn {
  background: none;
  border: none;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  color: var(--text-secondary, #666);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

.mode-btn.active {
  background-color: var(--primary-color, #007bff);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.2);
}

.mode-divider {
  width: 1px;
  height: 12px;
  background-color: var(--input-border, #ddd);
  margin: 0 2px;
}

.input-textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid var(--input-border, #ddd);
  border-radius: 24px;
  font-size: 14px;
  line-height: 1.4;
  resize: none;
  outline: none;
  transition: all 0.3s ease;
  min-height: 44px;
  max-height: 120px;
  overflow-y: auto;
  background-color: var(--input-bg, white);
  color: var(--input-text, #333);
}

.input-textarea::placeholder {
  color: var(--text-secondary, #999);
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--primary-color, #007bff);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: background-color 0.2s;
  margin-bottom: 2px;
}

.send-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style> 