<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import TopBar from './TopBar.vue'
</script>

<template>
  <div class="flex h-screen w-full bg-bg font-sans">
    <Sidebar class="hidden md:flex" />
    <div class="flex flex-1 flex-col overflow-hidden">
      <TopBar>
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
