import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../features/auth/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // Корневой маршрут — layout-обёртка для всех защищённых страниц
      path: '/',
      component: () => import('../shared/ui/layout/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../features/dashboard/DashboardView.vue'),
        },
        {
          path: 'clients',
          name: 'clients',
          component: () => import('../features/client-card/ClientListView.vue'),
        },
        {
          path: 'plans',
          name: 'plans',
          component: () => import('../features/training-plan/TrainingPlanListView.vue'),
        },
      ],
    },
    {
      // Страница ошибки доступа — без layout, без сайдбара
      path: '/unauthorized',
      name: 'unauthorized',
      component: () => import('../features/auth/UnauthorizedView.vue'),
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Keycloak инициализирован с onLoad: 'login-required',
      // поэтому пользователь никогда не попадёт сюда неавторизованным.
      // Guard как последний рубеж защиты.
      authStore.login()
      return next(false)
    }

    if (to.meta.role && !authStore.hasRole(to.meta.role as string)) {
      return next({ name: 'unauthorized' })
    }
  }

  next()
})

export default router
