<script setup lang="ts">
import { useNavItems } from '@/shared/lib/nav/useNavItems'

const navItems = useNavItems()

// Базовый стиль пункта нижней навигации. Активные состояния подмешиваются
// RouterLink'ом через active-class/exact-active-class. `!` гарантирует, что
// активный цвет перебьёт базовый text-text-muted.
const baseLinkClass =
  'flex flex-col items-center justify-center gap-1 rounded-md text-xs font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary text-text-muted hover:bg-surface-2 hover:text-text-main'
const activeLinkClass = '!text-primary'
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
      :class="baseLinkClass"
      :active-class="activeLinkClass"
      :exact-active-class="activeLinkClass"
    >
      <component :is="item.icon" :size="20" aria-hidden="true" />
      {{ item.label }}
    </router-link>
  </nav>
</template>
