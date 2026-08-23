<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import TopBar from './TopBar.vue'
import MobileNav from './MobileNav.vue'

const isMobileMenuOpen = ref(false)
const route = useRoute()

const pageTitle = computed(() => (route.meta.title as string | undefined) ?? 'Кабинет тренера')
</script>

<template>
  <div class="flex min-h-screen w-full bg-bg font-sans">
    <Sidebar :mobile-open="isMobileMenuOpen" @close="isMobileMenuOpen = false" />
    <div class="flex flex-1 flex-col overflow-hidden">
      <TopBar :title="pageTitle">
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
      </TopBar>
      <main class="flex-1 overflow-y-auto p-4 pb-24 md:p-6 lg:p-8">
        <slot />
      </main>
      <MobileNav />
    </div>
  </div>
</template>
