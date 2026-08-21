<script setup lang="ts">
import { useAuthStore } from './authStore'

const authStore = useAuthStore()
</script>

<template>
  <div class="flex min-h-screen w-full items-center justify-center">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-text-main">403</h1>
      <p class="mt-4 text-text-muted">Доступ запрещен. У вас нет необходимых прав.</p>
      <button
        type="button"
        class="mt-6 rounded-md border border-border px-4 py-2 text-sm text-text-main hover:bg-surface-2"
        @click="authStore.logout()"
      >
        Выйти
      </button>
    </div>
  </div>
</template>
