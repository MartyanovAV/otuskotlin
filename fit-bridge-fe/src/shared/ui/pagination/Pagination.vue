<script setup lang="ts">
import { computed } from 'vue'
import { Button } from '@/shared/ui/button'

interface Props {
  pageNumber: number
  pageSize: number
  totalSize: number
  pageSizeOptions?: number[]
}

const props = withDefaults(defineProps<Props>(), {
  pageSizeOptions: () => [10, 20, 50],
})

const emit = defineEmits<{
  (e: 'update:pageNumber', page: number): void
  (e: 'update:pageSize', size: number): void
}>()

const totalPages = computed(() => {
  if (props.totalSize <= 0) return 1
  return Math.max(1, Math.ceil(props.totalSize / props.pageSize))
})

const fromItem = computed(() => {
  if (props.totalSize <= 0) return 0
  return (props.pageNumber - 1) * props.pageSize + 1
})

const toItem = computed(() => {
  if (props.totalSize <= 0) return 0
  return Math.min(props.totalSize, props.pageNumber * props.pageSize)
})

const canPrev = computed(() => props.pageNumber > 1)
const canNext = computed(() => props.pageNumber < totalPages.value)

const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value || page === props.pageNumber) return
  emit('update:pageNumber', page)
}

const onPageSizeChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  const newSize = Number(target.value)
  if (newSize > 0 && newSize !== props.pageSize) {
    emit('update:pageSize', newSize)
    emit('update:pageNumber', 1)
  }
}

// Вычисление диапазона страниц для умного отображения (1, 2, '...', 7, 8)
const visiblePages = computed(() => {
  const current = props.pageNumber
  const total = totalPages.value
  const delta = 1

  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  const pages: (number | string)[] = []
  const left = current - delta
  const right = current + delta + 1
  let prev: number | null = null

  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= left && i < right)) {
      if (prev !== null) {
        if (i - prev === 2) {
          pages.push(prev + 1)
        } else if (i - prev !== 1) {
          pages.push('...')
        }
      }
      pages.push(i)
      prev = i
    }
  }

  return pages
})
</script>

<template>
  <div class="flex flex-col sm:flex-row items-center justify-between gap-4 py-3 px-2 text-sm text-text-muted">
    <!-- Информация о диапазоне и выбор размера страницы -->
    <div class="flex items-center gap-4 text-xs sm:text-sm">
      <span>
        Показано <strong class="font-medium text-text-main">{{ fromItem }}–{{ toItem }}</strong> из <strong class="font-medium text-text-main">{{ totalSize }}</strong>
      </span>

      <div class="flex items-center gap-1.5">
        <span class="text-xs">Показывать по:</span>
        <select
          :value="pageSize"
          @change="onPageSizeChange"
          class="h-8 rounded-md border border-border bg-surface-2 px-2 py-0.5 text-xs text-text-main focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary cursor-pointer"
          id="pagination-page-size"
        >
          <option v-for="option in pageSizeOptions" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </div>
    </div>

    <!-- Кнопки навигации по страницам -->
    <div class="flex items-center gap-1">
      <!-- Кнопка Назад -->
      <Button
        variant="outline"
        size="sm"
        class="h-8 px-2.5 text-xs"
        :disabled="!canPrev"
        @click="goToPage(pageNumber - 1)"
        id="pagination-prev-btn"
        title="Предыдущая страница"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
        <span class="hidden sm:inline ml-1">Назад</span>
      </Button>

      <!-- Номера страниц -->
      <template v-for="(item, idx) in visiblePages" :key="idx">
        <span v-if="item === '...'" class="px-2 text-xs text-text-faint select-none">
          …
        </span>
        <Button
          v-else
          :variant="item === pageNumber ? 'default' : 'outline'"
          size="sm"
          class="h-8 w-8 p-0 text-xs"
          @click="goToPage(Number(item))"
          :id="`pagination-page-${item}`"
        >
          {{ item }}
        </Button>
      </template>

      <!-- Кнопка Вперед -->
      <Button
        variant="outline"
        size="sm"
        class="h-8 px-2.5 text-xs"
        :disabled="!canNext"
        @click="goToPage(pageNumber + 1)"
        id="pagination-next-btn"
        title="Следующая страница"
      >
        <span class="hidden sm:inline mr-1">Вперед</span>
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
      </Button>
    </div>
  </div>
</template>
