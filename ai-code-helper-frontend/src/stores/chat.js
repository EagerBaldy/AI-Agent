import { defineStore } from 'pinia'
import { createSession, listSessions, deleteSession, renameSession } from '../api/sessionApi'
import { chatWithSSE, getChatHistory, saveMessage } from '../api/chatApi'

export const useChatStore = defineStore('chat', {
  state: () => ({
    messages: [],
    sessions: [],
    currentSession: null,
    currentMode: 'home',
    isAiTyping: false,
    isStreaming: false,
    currentAiResponse: '',
    connectionError: false,
    currentEventSource: null,
  }),
  
  actions: {
    setMode(mode) {
      this.currentMode = mode
    },

    async loadSessionsForMode(mode) {
      this.currentMode = mode
      this.sessions = await listSessions(mode)
      return this.sessions
    },

    async createNewSession() {
      const session = await createSession(this.currentMode, '新会话')
      if (session) {
        this.sessions.unshift(session)
        await this.switchSession(session)
      }
    },

    async switchSession(session) {
      if (this.currentSession && this.currentSession.id === session.id) return
      
      this.stopGeneration()

      this.currentSession = session
      this.messages = []
      this.isAiTyping = false
      this.isStreaming = false
      this.currentAiResponse = ''
      
      // Load history
      await this.loadHistory()
    },

    async loadHistory() {
      if (!this.currentSession) return
      try {
        const history = await getChatHistory(this.currentSession.id, this.currentMode)
        this.messages = history.map(msg => ({
          id: msg.id,
          content: msg.content,
          isUser: msg.role === 'user',
          timestamp: new Date(msg.createTime)
        }))
        this.connectionError = false
      } catch (error) {
        console.error('Failed to load history:', error)
        this.connectionError = true
      }
    },

    async renameSession(session, newName) {
      const success = await renameSession(session.id, newName)
      if (success) {
        session.name = newName
        const s = this.sessions.find(i => i.id === session.id)
        if (s) s.name = newName
      }
      return success
    },

    async deleteSession(session) {
      const success = await deleteSession(session.id)
      if (success) {
        this.sessions = this.sessions.filter(s => s.id !== session.id)
        if (this.currentSession && this.currentSession.id === session.id) {
          if (this.sessions.length > 0) {
            await this.switchSession(this.sessions[0])
          } else {
            await this.createNewSession()
          }
        }
      }
    },

    addMessage(content, isUser = false) {
      const message = {
        id: Date.now() + Math.random(),
        content,
        isUser,
        timestamp: new Date()
      }
      this.messages.push(message)
    },

    updateLastMessage(content) {
      if (this.messages.length > 0) {
        this.messages[this.messages.length - 1].content = content
      }
    },

    stopGeneration() {
      if (this.currentEventSource) {
        this.currentEventSource.close()
        this.currentEventSource = null
      }
      this.isStreaming = false
      this.isAiTyping = false
      this.isTypingEffectActive = false
      if (this.typewriterTimer) clearInterval(this.typewriterTimer)
      
      // Flush remaining buffer
      if (this.streamBuffer && this.streamBuffer.length > 0) {
          this.currentAiResponse += this.streamBuffer.join('')
          this.streamBuffer = []
          this.updateLastMessage(this.currentAiResponse)
      }
      
      if (this.currentAiResponse.trim()) {
         // It's already in the messages array via updateLastMessage
      }
      this.currentAiResponse = ''
    },

    async sendMessage(content) {
      // Intelligent Naming
      if (this.currentSession && 
          this.messages.length === 0 && 
          (this.currentSession.name === '新会话' || this.currentSession.name.startsWith('新会话 '))) {
        this.generateSessionName(content)
      }

      this.addMessage(content, true)
      
      // Start Response
      this.isAiTyping = true
      this.isStreaming = true
      this.currentAiResponse = ''
      
      // Placeholder for AI message
      this.addMessage('', false)

      // Use generic chatWithSSE from chatApi.js
      // But wait, chatApi.js logic handles endpoints.
      // We should use that instead of re-implementing EventSource here if possible
      // However, chatWithSSE in chatApi.js uses `/ai/chat` etc.
      // The Agent API is `/agent/chat`.
      // Let's modify chatApi.js to support agent mode properly or adapt here.
      
      // Actually, let's look at `chatApi.js` again. It has `chatWithSSE`.
      // It selects endpoint based on type.
      // We should update `chatApi.js` to handle agent streaming or do it here.
      // Doing it here gives us more control over the buffer.
      
      this.startAgentStream(content)
    },

    startAgentStream(message) {
      try {
        const sessionId = this.currentSession ? this.currentSession.id : 0
        // Use the generic Agent endpoint
        const eventSource = new EventSource(`/api/agent/chat?message=${encodeURIComponent(message)}&sessionId=${sessionId}`, {
          withCredentials: true
        })
        this.currentEventSource = eventSource
        
        // Initialize typewriter buffer
        this.streamBuffer = []
        this.isTypingEffectActive = true
        this.startTypewriterLoop()
        
        // Handle incoming messages
        eventSource.onmessage = (event) => {
          let data = event.data
          
          // --- 核心优化逻辑 ---
          // 之前的逻辑过滤掉了思考过程，导致用户看不到中间步骤。
          // 现在取消过滤，让用户能看到 Agent 的思考和工具调用。
          
          /* 
          // 检查是否是思考过程的片段
          const isThoughtProcess = 
              data.startsWith('#### 第') || 
              data.startsWith('> **') || 
              data.startsWith('> 🛠️') || 
              data.startsWith('> 👀') || 
              data.includes('<details') || 
              data.includes('</details>') ||
              data.includes('<summary>');
              
          if (isThoughtProcess) {
              // 忽略思考过程，不推入 buffer
              return;
          }
          */
          
          // 如果不是思考过程，推入 buffer
          // 注意：需要处理换行。后端通常发出的块可能不带换行，或者带了。
          // ReActAgent 中：emitter.next("Final Answer")
          
          // 还有一个问题：如果不显示思考，用户会看到一段空白等待期。
          // 可以在 UI 上显示 "正在思考..." 或 "正在调用工具..." 的状态（isAiTyping 已经有了）。
          
          // 修正：EventSource 的 data 字段会自动去掉换行吗？
          // 通常 SSE data: some text\n\n -> event.data = "some text"
          // 如果我们要还原换行，通常约定用特殊字符，或者后端发送 JSON。
          // 这里后端发送的是纯文本。
          
          // 简单处理：加上换行符，因为 Agent 输出通常是分段的
          // data += "\n" 
          
          // 但如果是流式输出 Final Answer（如果有的话），加换行可能会断开句子。
          // ReActAgent 目前是一次性输出 Final Answer。
          
          for (const char of data) {
            this.streamBuffer.push(char)
          }
          // 手动补充换行，保持段落感
          this.streamBuffer.push('\n')
        }
        
        eventSource.onerror = (err) => {
          // ... same error handling ...
          console.error('SSE Error:', err)
          eventSource.close()
          this.currentEventSource = null
          
          // Wait for buffer to drain
          const checkBufferInterval = setInterval(() => {
            if (this.streamBuffer.length === 0) {
              clearInterval(checkBufferInterval)
              this.isStreaming = false
              this.isAiTyping = false
              this.isTypingEffectActive = false
              if (this.typewriterTimer) clearInterval(this.typewriterTimer)

              // Save to history
              if (this.currentSession && this.currentAiResponse) {
                 saveMessage(this.currentSession.id, this.currentAiResponse, false, this.currentMode)
              }
            }
          }, 100)
        }
      } catch (error) {
        console.error('Agent API Error:', error)
        this.isStreaming = false
        this.isAiTyping = false
      }
    },

    startTypewriterLoop() {
        if (this.typewriterTimer) clearInterval(this.typewriterTimer)
        
        this.typewriterTimer = setInterval(() => {
            if (!this.isTypingEffectActive && this.streamBuffer.length === 0) {
                clearInterval(this.typewriterTimer)
                return
            }
            
            if (this.streamBuffer.length > 0) {
                // Adaptive speed: if buffer is huge, type faster
                const speed = this.streamBuffer.length > 50 ? 5 : (this.streamBuffer.length > 20 ? 2 : 1)
                const chunk = this.streamBuffer.splice(0, speed).join('')
                this.currentAiResponse += chunk
                this.updateLastMessage(this.currentAiResponse)
            }
        }, 20) // 20ms per update ~ 50 chars/sec base speed
    },

    stopGeneration() {
      if (this.currentEventSource) {
        this.currentEventSource.close()
        this.currentEventSource = null
      }
      this.isStreaming = false
      this.isAiTyping = false
      this.isTypingEffectActive = false
      if (this.typewriterTimer) clearInterval(this.typewriterTimer)
      
      // Flush remaining buffer
      if (this.streamBuffer && this.streamBuffer.length > 0) {
          this.currentAiResponse += this.streamBuffer.join('')
          this.streamBuffer = []
          this.updateLastMessage(this.currentAiResponse)
      }
      
      if (this.currentAiResponse.trim()) {
         // It's already in the messages array via updateLastMessage
      }
      this.currentAiResponse = ''
    },

    async generateSessionName(message) {
       // ... logic ...
       let cleanText = message.replace(/<[^>]+>/g, '')
          .replace(/[^\w\u4e00-\u9fa5\s,，.。?？!！]/g, '')
          .replace(/\s+/g, ' ')
          .trim()
        
        if (cleanText.length > 20) {
          cleanText = cleanText.substring(0, 20)
        }
        
        if (cleanText.length > 0) {
          const success = await this.renameSession(this.currentSession, cleanText)
        }
    }
  }
})
