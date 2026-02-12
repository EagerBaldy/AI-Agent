import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// Lazy load views
const LoginView = () => import('../views/LoginView.vue')
const HomeView = () => import('../views/HomeView.vue')
const WorkspaceView = () => import('../views/WorkspaceView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/workspace/:mode',
      name: 'workspace',
      component: WorkspaceView,
      meta: { requiresAuth: true },
      props: true
    }
  ]
})

// Navigation Guard
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // Check login status if not already checked
  if (!userStore.isLoggedIn && !userStore.currentUser) {
    await userStore.checkLogin()
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
