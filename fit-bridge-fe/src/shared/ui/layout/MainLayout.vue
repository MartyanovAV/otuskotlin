<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import TopBar from './TopBar.vue'

const isMobileMenuOpen = ref(false)
</script>

<template>
  <div class="flex h-screen w-full bg-bg font-sans">
    <Sidebar :mobile-open="isMobileMenuOpen" @close="isMobileMenuOpen = false" />
    <div class="flex flex-1 flex-col overflow-hidden">
      <TopBar>
        <template #leading>
          <button
            type="button"
            class="rounded-md p-2 text-text-main hover:bg-surface-2 md:hidden"
            aria-label="Открыть меню"
            @click="isMobileMenuOpen = true"
          >
            ☰
          </button>
        </template>
        <template #title>
          <slot name="title" />
        </template>
      </TopBar>
      <main class="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8">
        <slot />
      </main>
    </div>
  </div>
</template>
