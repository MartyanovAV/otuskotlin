<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Button } from '@/shared/ui/button'
import type { TrainingPlanResponseObject } from '@/shared/api/generated/models/trainingPlanResponseObject'
import {
  formatPlanToShareText,
  getVkShareUrl,
  getTelegramShareUrl,
  getWhatsAppShareUrl,
  copyPlanToClipboard,
} from '../lib/sharePlan'

const props = withDefaults(
  defineProps<{
    plan: TrainingPlanResponseObject
    variant?: 'default' | 'outline' | 'secondary' | 'ghost'
    size?: 'sm' | 'default' | 'icon'
    showLabel?: boolean
    placement?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right'
  }>(),
  {
    variant: 'outline',
    size: 'sm',
    showLabel: true,
    placement: 'bottom-right',
  },
)

const isOpen = ref(false)
const isCopied = ref(false)
const containerRef = ref<HTMLElement | null>(null)

const placementClasses = computed(() => {
  switch (props.placement) {
    case 'top-left':
      return 'bottom-full left-0 mb-1.5'
    case 'top-right':
      return 'bottom-full right-0 mb-1.5'
    case 'bottom-left':
      return 'top-full left-0 mt-1.5'
    case 'bottom-right':
    default:
      return 'top-full right-0 mt-1.5'
  }
})

const formattedText = computed(() => {
  return formatPlanToShareText(props.plan)
})

const handleShareVk = (e: MouseEvent) => {
  e.stopPropagation()
  const url = getVkShareUrl(formattedText.value)
  window.open(url, '_blank', 'noopener,noreferrer')
  isOpen.value = false
}

const handleShareTelegram = (e: MouseEvent) => {
  e.stopPropagation()
  const url = getTelegramShareUrl(formattedText.value)
  window.open(url, '_blank', 'noopener,noreferrer')
  isOpen.value = false
}

const handleShareWhatsApp = (e: MouseEvent) => {
  e.stopPropagation()
  const url = getWhatsAppShareUrl(formattedText.value)
  window.open(url, '_blank', 'noopener,noreferrer')
  isOpen.value = false
}

const handleCopy = async (e: MouseEvent) => {
  e.stopPropagation()
  const success = await copyPlanToClipboard(formattedText.value)
  if (success) {
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  }
}

const toggleMenu = (e: MouseEvent) => {
  e.stopPropagation()
  isOpen.value = !isOpen.value
}

const handleClickOutside = (event: MouseEvent) => {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="relative inline-block text-left" ref="containerRef">
    <Button
      :variant="variant"
      :size="size"
      :class="[
        'transition-colors hover:border-primary/50 hover:text-primary',
        showLabel ? 'h-8 px-3 gap-1.5 text-xs' : 'h-8 w-8 p-0'
      ]"
      :title="showLabel ? undefined : 'Поделиться планом'"
      :aria-label="'Поделиться планом'"
      @click="toggleMenu"
      :id="`share-btn-${plan.id}`"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="18" cy="5" r="3"/>
        <circle cx="6" cy="12" r="3"/>
        <circle cx="18" cy="19" r="3"/>
        <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
        <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
      </svg>
      <span v-if="showLabel">Поделиться</span>
    </Button>

    <!-- Выпадающее меню шеринга -->
    <div
      v-if="isOpen"
      :class="['absolute w-64 rounded-xl border border-border bg-surface p-1.5 shadow-xl z-50 animate-in fade-in zoom-in-95 duration-100', placementClasses]"
      @click.stop
    >
      <div class="px-2.5 py-1.5 text-[11px] font-semibold text-text-muted uppercase tracking-wider border-b border-border/50 mb-1">
        Отправить план клиенту
      </div>

      <!-- ВКонтакте -->
      <button
        type="button"
        class="w-full flex items-center gap-2.5 px-2.5 py-2 text-xs font-medium text-text-main rounded-lg hover:bg-[#0077FF]/10 hover:text-[#0077FF] transition-colors text-left group"
        @click="handleShareVk"
        :id="`share-vk-${plan.id}`"
      >
        <div class="w-6 h-6 rounded-md bg-[#0077FF]/15 text-[#0077FF] flex items-center justify-center shrink-0">
          <!-- VK icon -->
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <path d="M13.162 18.994c.609 0 .858-.406.851-.915-.072-1.523.636-2.146 1.406-2.146.541 0 .972.338 1.488.846.777.765 1.353 1.215 2.196 1.215h2.152c1.014 0 1.5-.508 1.205-1.488-.344-1.074-1.544-2.553-2.228-3.328-.508-.576-.711-.846-.034-1.794.067-.093 1.286-1.828 2.378-3.418.473-.677.169-1.285-.812-1.285h-2.183c-.812 0-1.185.44-1.387.914-.541 1.252-1.422 2.707-2.065 2.707-.237 0-.372-.119-.44-.356-.169-.609-.102-1.625-.102-2.336 0-1.895.271-2.098-.711-2.098-.372 0-.745.034-1.083.102-1.083.237-1.557.812-1.151 1.354.508.677.372.914.372 2.302 0 .542-.102.948-.372.948-.44 0-1.083-.982-1.693-2.166-.339-.643-.677-1.185-1.32-1.185H3.61c-.914 0-1.117.44-.914 1.286.745 2.47 3.385 7.649 6.702 7.649h3.764z"/>
          </svg>
        </div>
        <div class="flex-1">
          <div class="font-semibold text-xs text-text-main group-hover:text-[#0077FF]">ВКонтакте (VK)</div>
          <div class="text-[10px] text-text-muted">Личное сообщение клиенту</div>
        </div>
      </button>

      <!-- Telegram -->
      <button
        type="button"
        class="w-full flex items-center gap-2.5 px-2.5 py-2 text-xs font-medium text-text-main rounded-lg hover:bg-[#229ED9]/10 hover:text-[#229ED9] transition-colors text-left group"
        @click="handleShareTelegram"
        :id="`share-tg-${plan.id}`"
      >
        <div class="w-6 h-6 rounded-md bg-[#229ED9]/15 text-[#229ED9] flex items-center justify-center shrink-0">
          <!-- Telegram icon -->
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 00-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.75-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z"/>
          </svg>
        </div>
        <div class="flex-1">
          <div class="font-semibold text-xs text-text-main group-hover:text-[#229ED9]">Telegram</div>
          <div class="text-[10px] text-text-muted">Отправить в чат / диалог</div>
        </div>
      </button>

      <!-- WhatsApp -->
      <button
        type="button"
        class="w-full flex items-center gap-2.5 px-2.5 py-2 text-xs font-medium text-text-main rounded-lg hover:bg-[#25D366]/10 hover:text-[#25D366] transition-colors text-left group"
        @click="handleShareWhatsApp"
        :id="`share-wa-${plan.id}`"
      >
        <div class="w-6 h-6 rounded-md bg-[#25D366]/15 text-[#25D366] flex items-center justify-center shrink-0">
          <!-- WhatsApp icon -->
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12.04 2c-5.46 0-9.91 4.45-9.91 9.91 0 1.75.46 3.45 1.32 4.95L2.05 22l5.25-1.38c1.45.79 3.08 1.21 4.74 1.21 5.46 0 9.91-4.45 9.91-9.91 0-2.65-1.03-5.14-2.9-7.01A9.816 9.816 0 0012.04 2zm.01 1.67c4.54 0 8.24 3.7 8.24 8.24 0 2.2-.86 4.27-2.42 5.82a8.17 8.17 0 01-5.82 2.42c-1.44 0-2.85-.38-4.1-1.1l-.29-.17-3.06.8.82-2.98-.19-.3a8.17 8.17 0 01-1.25-4.48c0-4.54 3.7-8.25 8.24-8.25zm4.72 11.64c-.26-.13-1.53-.75-1.77-.84-.24-.09-.41-.13-.59.13-.17.26-.68.84-.83 1.02-.15.17-.31.2-.57.07-.26-.13-1.09-.4-2.07-1.28-.77-.68-1.28-1.53-1.43-1.79-.15-.26-.02-.4.11-.53.12-.12.26-.31.39-.46.13-.15.17-.26.26-.43.09-.17.04-.33-.02-.46-.07-.13-.59-1.42-.81-1.95-.21-.51-.43-.44-.59-.45h-.51c-.17 0-.46.07-.7.33-.24.26-.92.9-.92 2.2 0 1.3 1.03 2.55 1.17 2.73.15.17 2.03 3.1 4.92 4.35.69.3 1.23.47 1.65.61.69.22 1.32.19 1.81.12.55-.08 1.53-.62 1.74-1.23.22-.6.22-1.12.15-1.23-.06-.11-.23-.17-.49-.3z"/>
          </svg>
        </div>
        <div class="flex-1">
          <div class="font-semibold text-xs text-text-main group-hover:text-[#25D366]">WhatsApp</div>
          <div class="text-[10px] text-text-muted">Отправить в мессенджер</div>
        </div>
      </button>

      <div class="my-1 border-t border-border/50"></div>

      <!-- Скопировать текст -->
      <button
        type="button"
        class="w-full flex items-center gap-2.5 px-2.5 py-2 text-xs font-medium text-text-main rounded-lg hover:bg-surface-2 transition-colors text-left group"
        @click="handleCopy"
        :id="`share-copy-${plan.id}`"
      >
        <div class="w-6 h-6 rounded-md bg-surface-3 text-text-muted flex items-center justify-center shrink-0">
          <svg v-if="!isCopied" xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect width="14" height="14" x="8" y="8" rx="2" ry="2"/>
            <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/>
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="text-success">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <div class="flex-1">
          <div class="font-semibold text-xs" :class="isCopied ? 'text-success' : 'text-text-main'">
            {{ isCopied ? 'Скопировано в буфер!' : 'Скопировать текст' }}
          </div>
          <div class="text-[10px] text-text-muted">Для отправки в SMS или другие чаты</div>
        </div>
      </button>
    </div>
  </div>
</template>
