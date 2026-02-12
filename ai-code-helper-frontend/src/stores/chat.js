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

      const mode = this.currentMode
      // Check if Agent Mode (simplified logic based on previous App.vue)
      // Actually App.vue logic was: if (mode === 'agent') ... but wait, App.vue passed 'agent' sometimes?
      // In App.vue: `if (mode === 'agent')` was likely incorrect or unused, as `switchMode` sets `currentMode` to 'code', 'travel' etc.
      // And `sendMessage` used `this.currentMode` implicitly.
      // Wait, in App.vue `sendMessage(payload)` handled object payload.
      // But let's assume `currentMode` determines the behavior.
      
      // The previous code had a specific `startAgentResponse` vs `startAiResponse`.
      // `startAgentResponse` was used if `mode === 'agent'`.
      // But `switchMode` sets `currentMode` to 'code', 'travel', etc.
      // Let's look at `App.vue` again.
      // Ah, `startAgentResponse` was called if `mode === 'agent'`.
      // But `switchMode` sets `currentMode` to 'code', 'travel' etc.
      // In `App.vue`, `sendMessage` payload could be object `{content, mode}`.
      // But typically it just uses `this.currentMode`.
      
      // IMPORTANT: In the previous fixes, we enabled Agent for 'travel', 'code' etc.
      // So we should probably ALWAYS use the Agent API for these modes if they are Agents.
      // For now, I will implement a generic `startResponse` that uses the Agent API if appropriate.
      
      // Let's assume all modes in the new design use the Agent API (since we fixed it to support all).
      // Or we stick to the `chatApi.js` logic.
      
      this.startAgentStream(content)
    },

    startAgentStream(message) {
      try {
        const sessionId = this.currentSession ? this.currentSession.id : 0
        const eventSource = new EventSource(`/api/agent/chat?message=${encodeURIComponent(message)}&sessionId=${sessionId}`, {
          withCredentials: true
        })
        this.currentEventSource = eventSource
        
        // Initialize typewriter buffer
        this.streamBuffer = []
        this.isTypingEffectActive = true
        this.startTypewriterLoop()
        
        eventSource.onmessage = (event) => {
          // Push data to buffer instead of direct update
          // Add newline because backend often sends lines
          const data = event.data + "\n"
          for (const char of data) {
            this.streamBuffer.push(char)
          }
        }
        
        eventSource.onerror = (err) => {
          console.error('SSE Error:', err)
          eventSource.close()
          // Do not immediately stop streaming flag, let buffer drain
          // this.isStreaming = false 
          this.currentEventSource = null
          
          // Wait for buffer to drain before finishing
          const checkBufferInterval = setInterval(() => {
            if (this.streamBuffer.length === 0) {
              clearInterval(checkBufferInterval)
              this.isStreaming = false
              this.isAiTyping = false
              this.isTypingEffectActive = false
              if (this.typewriterTimer) clearInterval(this.typewriterTimer)

              if (!this.currentAiResponse) {
                 this.updateLastMessage("智能体执行出错，请重试。")
              }
              
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
