<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from './features/auth/authStore'

const authStore = useAuthStore()
const reload = () => window.location.reload()
</script>

<template>
  <div v-if="authStore.initializationError" class="flex min-h-screen items-center justify-center bg-bg p-6">
    <div class="max-w-md space-y-4 rounded-xl border border-border bg-surface p-6 text-center shadow-md">
      <h1 class="text-lg font-semibold text-text-main">Не удалось войти в систему</h1>
      <p class="text-sm text-text-muted">{{ authStore.initializationError }}</p>
      <button
        type="button"
        class="rounded-md bg-primary px-4 py-2 text-sm font-medium text-text-inverse"
        @click="reload"
      >
        Повторить
      </button>
    </div>
  </div>
  <RouterView v-else />
</template>
