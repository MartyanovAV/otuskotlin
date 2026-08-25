// Изолированный SPA-fixture для навигационных e2e-тестов.
//
// Подключается Vite-дев-сервером по адресу
//   http://localhost:5173/e2e/fixtures/nav-only.html
// и НЕ зависит от Keycloak, backend, БД и Toast-компонента.
//
// Что здесь намеренно заглушено:
//   - useAuthStore: инициализируется Pinia-store, после чего его state
//     «патчится» нужными нам значениями (isAuthenticated=true,
//     roles=['TRAINER'], userProfile=...). Сам keycloak-js конструктор
//     не делает сетевых вызовов, поэтому setup-store отрабатывает.
//     initKeycloak() мы не вызываем.
//   - Views: Dashboard / Clients / Plans заменены стабами, чтобы
//     TanStack Query не уходил в сеть за реальными данными.
//
// Что НЕ заглушено и проверяется в e2e:
//   - Реальный Sidebar и MobileNav (наш рефакторинг).
//   - Реальный vue-router (createWebHistory/createMemoryHistory).
//   - Реальный Pinia.

import { createApp, defineComponent, h } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'

// Импортируем store раньше, чем Sidebar. Сам вызов useAuthStore()
// выполняется в Pinia-плагине, чтобы положить туда нужное нам state.
import { useAuthStore } from '@/features/auth/authStore'
import Sidebar from '@/shared/ui/layout/Sidebar.vue'
import MobileNav from '@/shared/ui/layout/MobileNav.vue'

// View-стабы.
const Dashboard = defineComponent({ name: 'Dashboard', template: '<main data-view="dashboard"><h1>Дашборд</h1></main>' })
const Clients = defineComponent({ name: 'Clients', template: '<main data-view="clients"><h1>Клиенты</h1></main>' })
const Plans = defineComponent({ name: 'Plans', template: '<main data-view="plans"><h1>Планы</h1></main>' })

const pinia = createPinia()
setActivePinia(pinia)

// Запускаем setup реального auth-store, затем $patch'им state.
// keycloak-js конструктор не делает сеть; initKeycloak() не вызываем.
const auth = useAuthStore(pinia)
auth.$patch({
  isAuthenticated: true,
  isInitialized: true,
  userProfile: { firstName: 'Test', lastName: 'User' },
  roles: ['TRAINER'],
})

// router в fixture живёт в памяти (не в history), чтобы можно было
// одновременно проверять и URL, и реальный currentRoute. URL браузера
// обновляется через web-history эмуляцию на window.history.
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    {
      path: '/',
      component: { template: '<router-view />' },
      children: [
        { path: '', name: 'dashboard', component: Dashboard },
        { path: 'clients', name: 'clients', component: Clients },
        { path: 'plans', name: 'plans', component: Plans },
      ],
    },
  ],
})
await router.push('/')
await router.isReady()

// Стилизованный layout, чтобы Sidebar и MobileNav были видны.
const AppShell = defineComponent({
  name: 'FixtureShell',
  setup() {
    return () =>
      h('div', { class: 'fixture-shell', style: { display: 'flex', minHeight: '100vh' } }, [
        h(Sidebar, { mobileOpen: false }),
        h('div', { class: 'fixture-content', style: { flex: '1 1 auto' } }, [h('router-view')]),
        h(MobileNav),
      ])
  },
})

const app = createApp(AppShell)
app.use(pinia)
app.use(router)
app.mount('#app')

// Экспонируем для отладки.
;(window as unknown as { __fixture: unknown }).__fixture = { router, pinia, auth }
