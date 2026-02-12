<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">AI</div>
        <h2>{{ isRegister ? '注册账号' : '欢迎登录' }}</h2>
        <p>AI 全能 Agent - 您的智能工作与生活伙伴</p>
      </div>
      
      <div class="form-group">
        <input 
          type="text" 
          v-model="userAccount" 
          placeholder="请输入账号"
          @keyup.enter="handleSubmit"
        />
      </div>
      
      <div class="form-group">
        <input 
          type="password" 
          v-model="userPassword" 
          placeholder="请输入密码"
          @keyup.enter="handleSubmit"
        />
      </div>
      
      <div v-if="isRegister" class="form-group">
        <input 
          type="password" 
          v-model="checkPassword" 
          placeholder="请确认密码"
          @keyup.enter="handleSubmit"
        />
      </div>
      
      <div class="error-message" v-if="errorMessage">{{ errorMessage }}</div>
      
      <button class="submit-btn" @click="handleSubmit" :disabled="isLoading">
        {{ isLoading ? '处理中...' : (isRegister ? '注册' : '登录') }}
      </button>
      
      <div class="toggle-mode">
        <span @click="toggleMode">
          {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import { login, register } from '../api/userApi'

export default {
  name: 'Login',
  data() {
    return {
      isRegister: false,
      userAccount: '',
      userPassword: '',
      checkPassword: '',
      errorMessage: '',
      isLoading: false
    }
  },
  methods: {
    toggleMode() {
      this.isRegister = !this.isRegister
      this.errorMessage = ''
      this.userAccount = ''
      this.userPassword = ''
      this.checkPassword = ''
    },
    async handleSubmit() {
      if (!this.userAccount || !this.userPassword) {
        this.errorMessage = '请输入账号和密码'
        return
      }
      
      if (this.isRegister && this.userPassword !== this.checkPassword) {
        this.errorMessage = '两次输入的密码不一致'
        return
      }
      
      this.isLoading = true
      this.errorMessage = ''
      
      try {
        if (this.isRegister) {
          await register(this.userAccount, this.userPassword)
          // 注册成功后自动切换到登录
          this.isRegister = false
          this.errorMessage = '注册成功，请登录'
          this.userPassword = ''
          this.checkPassword = ''
        } else {
          const user = await login(this.userAccount, this.userPassword)
          this.$emit('login-success', user)
        }
      } catch (error) {
        this.errorMessage = error.message || '操作失败，请重试'
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100%; /* 确保占满宽度 */
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 40px;
  font-weight: bold;
  background: linear-gradient(45deg, #00b4db, #0083b0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10px;
}

.login-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.login-header p {
  color: #7f8c8d;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
  box-sizing: border-box; /* 确保 padding 不会撑大宽度 */
}

input:focus {
  border-color: #00b4db;
  outline: none;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(90deg, #00b4db, #0083b0);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.submit-btn:hover {
  opacity: 0.9;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.toggle-mode {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.toggle-mode span {
  color: #00b4db;
  cursor: pointer;
}

.toggle-mode span:hover {
  text-decoration: underline;
}

.error-message {
  color: #ff6b6b;
  font-size: 14px;
  margin-bottom: 15px;
  text-align: center;
}
</style>
