<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/shared/lib/utils'
import type { ToastItem, ToastVariant } from './toastStore'

const props = defineProps<{
  toast: ToastItem
}>()

const emit = defineEmits<{
  (e: 'dismiss', id: string): void
}>()

const variantStyles: Record<ToastVariant, { bg: string; border: string; iconColor: string }> = {
  success: {
    bg: 'bg-success-soft',
    border: 'border-success/30',
    iconColor: 'text-success',
  },
  error: {
    bg: 'bg-danger-soft',
    border: 'border-danger/30',
    iconColor: 'text-danger',
  },
  warning: {
    bg: 'bg-warning-soft',
    border: 'border-warning/30',
    iconColor: 'text-warning',
  },
  info: {
    bg: 'bg-info-soft',
    border: 'border-info/30',
    iconColor: 'text-info',
  },
}

const styles = computed(() => variantStyles[props.toast.variant])

const close = () => emit('dismiss', props.toast.id)
</script>

<template>
  <div
    role="status"
    :aria-live="toast.variant === 'error' ? 'assertive' : 'polite'"
    :class="cn(
      'pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-lg border bg-surface p-3.5 shadow-lg motion-safe:animate-in motion-safe:slide-in-from-right-full motion-safe:fade-in',
      styles.bg,
      styles.border,
    )"
  >
    <!-- Иконка варианта -->
    <div :class="cn('mt-0.5 shrink-0', styles.iconColor)">
      <svg v-if="toast.variant === 'success'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12" />
      </svg>
      <svg v-else-if="toast.variant === 'error'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      <svg v-else-if="toast.variant === 'warning'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z" />
        <line x1="12" y1="9" x2="12" y2="13" />
        <line x1="12" y1="17" x2="12.01" y2="17" />
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="16" x2="12" y2="12" />
        <line x1="12" y1="8" x2="12.01" y2="8" />
      </svg>
    </div>

    <div class="flex-1 min-w-0">
      <p class="text-sm font-semibold text-text-main">{{ toast.title }}</p>
      <p v-if="toast.description" class="mt-0.5 text-xs text-text-muted break-words">
        {{ toast.description }}
      </p>
      <button
        v-if="toast.action"
        type="button"
        class="mt-1.5 text-xs font-semibold text-primary hover:underline focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary rounded"
        @click="toast.action.onClick(); close()"
      >
        {{ toast.action.label }}
      </button>
    </div>

    <button
      type="button"
      class="shrink-0 rounded p-1 text-text-muted hover:bg-surface-2 hover:text-text-main transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary"
      :aria-label="`Закрыть уведомление: ${toast.title}`"
      @click="close"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 6 6 18" />
        <path d="m6 6 12 12" />
      </svg>
    </button>
  </div>
</template>
