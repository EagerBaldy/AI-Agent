import { defineStore } from 'pinia'
import { getCurrentUser, logout } from '../api/userApi'

export const useUserStore = defineStore('user', {
  state: () => ({
    isLoggedIn: false,
    currentUser: null,
    isDarkMode: false,
  }),
  actions: {
    async checkLogin() {
      try {
        const user = await getCurrentUser()
        if (user) {
          this.isLoggedIn = true
          this.currentUser = user
          return true
        }
      } catch (e) {
        console.error("Check login failed", e)
      }
      return false
    },
    loginSuccess(user) {
      this.isLoggedIn = true
      this.currentUser = user
    },
    async logout() {
      await logout()
      this.isLoggedIn = false
      this.currentUser = null
    },
    toggleTheme() {
      this.isDarkMode = !this.isDarkMode
      // Persist to localStorage if needed
    }
  }
})
