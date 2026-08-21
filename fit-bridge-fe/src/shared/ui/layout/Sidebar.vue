<script setup lang="ts">
import { useAuthStore } from '@/features/auth/authStore'
import { Button } from '@/shared/ui/button'

const authStore = useAuthStore()

const navItems = [
  { to: '/', label: 'Дашборд' },
  { to: '/clients', label: 'Клиенты' },
  { to: '/plans', label: 'Планы' },
]
</script>

<template>
  <aside class="flex w-64 flex-col border-r border-border bg-surface px-4 py-6">
    <div class="mb-8 px-2 text-2xl font-bold tracking-tight text-text-main">
      FitBridge
    </div>

    <nav class="flex-1 space-y-1">
      <router-link
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        v-slot="{ href, navigate, isActive, isExactActive }"
        custom
      >
        <a
          :href="href"
          @click="navigate"
          :class="[
            'flex items-center rounded-md px-3 py-2 text-sm font-medium transition-colors',
            (item.to === '/' ? isExactActive : isActive)
              ? 'bg-primary text-text-inverse'
              : 'text-text-muted hover:bg-surface-2 hover:text-text-main',
          ]"
        >
          {{ item.label }}
        </a>
      </router-link>
    </nav>

    <div class="mt-auto border-t border-border pt-4">
      <div class="mb-4 flex items-center px-2">
        <div class="flex-1 overflow-hidden">
          <p class="truncate text-sm font-medium text-text-main">
            {{ authStore.userProfile?.firstName }} {{ authStore.userProfile?.lastName }}
          </p>
          <p class="truncate text-xs text-text-muted">Тренер</p>
        </div>
      </div>
      <Button variant="outline" class="w-full justify-start" @click="authStore.logout()">
        Выйти
      </Button>
    </div>
  </aside>
</template>
