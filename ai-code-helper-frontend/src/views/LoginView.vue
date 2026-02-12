<template>
  <div class="login-view">
    <Login @login-success="handleLoginSuccess" />
  </div>
</template>

<script setup>
import Login from '../components/Login.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLoginSuccess = (user) => {
  userStore.loginSuccess(user)
  router.push('/')
}
</script>

<style scoped>
.login-view {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-body, #f8f9fa);
  margin: 0;
  padding: 0;
}
</style>
