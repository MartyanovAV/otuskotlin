<script setup lang="ts">
import { ref, onMounted } from 'vue'

const isDark = ref(false)

// Восстанавливаем тему из localStorage при монтировании
onMounted(() => {
  const saved = localStorage.getItem('theme')
  if (saved === 'dark') {
    isDark.value = true
    document.documentElement.setAttribute('data-theme', 'dark')
  }
})

const toggleTheme = () => {
  isDark.value = !isDark.value
  if (isDark.value) {
    document.documentElement.setAttribute('data-theme', 'dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.removeAttribute('data-theme')
    localStorage.setItem('theme', 'light')
  }
}
</script>

<template>
  <header class="flex h-14 items-center gap-4 border-b border-border bg-surface px-6 lg:h-[60px]">
    <slot name="leading" />
    <div class="flex-1">
      <h1 class="text-lg font-semibold md:text-2xl text-text-main">
        <slot name="title">Кабинет</slot>
      </h1>
    </div>
    <div class="flex items-center gap-4">
      <button
        @click="toggleTheme"
        :title="isDark ? 'Переключить на светлую тему' : 'Переключить на тёмную тему'"
        class="rounded-full bg-surface-2 p-2 text-text-main hover:bg-surface-3 transition-colors"
      >
        <!-- Луна (тёмная тема выключена) / Солнце (тёмная тема включена) -->
        <svg v-if="!isDark" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="4"/>
          <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/>
        </svg>
      </button>
    </div>
  </header>
</template>
