<script setup lang="ts">
import { Dumbbell, LayoutDashboard, UsersRound } from 'lucide-vue-next'

const navItems = [
  { to: '/', label: 'Главная', icon: LayoutDashboard },
  { to: '/clients', label: 'Клиенты', icon: UsersRound },
  { to: '/plans', label: 'Планы', icon: Dumbbell },
]
</script>

<template>
  <nav
    class="fixed inset-x-0 bottom-0 z-app-chrome grid h-[68px] grid-cols-3 border-t border-border bg-surface/95 px-2 pb-[env(safe-area-inset-bottom)] backdrop-blur-md md:hidden"
    aria-label="Основная навигация"
  >
    <router-link
      v-for="item in navItems"
      :key="item.to"
      :to="item.to"
      v-slot="{ href, navigate, isActive, isExactActive }"
      custom
    >
      <a
        :href="href"
        :aria-current="(item.to === '/' ? isExactActive : isActive) ? 'page' : undefined"
        :class="[
          'flex flex-col items-center justify-center gap-1 rounded-md text-xs font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary',
          (item.to === '/' ? isExactActive : isActive)
            ? 'text-primary'
            : 'text-text-muted hover:bg-surface-2 hover:text-text-main',
        ]"
        @click="navigate"
      >
        <component :is="item.icon" :size="20" aria-hidden="true" />
        {{ item.label }}
      </a>
    </router-link>
  </nav>
</template>
