import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './features/auth/authStore'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(VueQueryPlugin)

// Инициализируем Keycloak перед монтированием приложения
const authStore = useAuthStore(pinia)
authStore.initKeycloak().then(() => {
  app.use(router)
  app.mount('#app')
})
