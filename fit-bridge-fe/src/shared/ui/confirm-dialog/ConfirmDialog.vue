<script setup lang="ts">
import { computed } from 'vue'
import { Button } from '@/shared/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { useConfirmState, type ConfirmVariant } from './confirmStore'

const { current, accept, cancel } = useConfirmState()

const isOpen = computed(() => current.current !== null)
const title = computed(() => current.current?.title ?? '')
const description = computed(() => current.current?.description)
const confirmText = computed(() => current.current?.confirmText ?? 'Подтвердить')
const cancelText = computed(() => current.current?.cancelText ?? 'Отмена')
const variant = computed<ConfirmVariant>(() => current.current?.variant ?? 'default')

// Radix Dialog сам обрабатывает ESC и клик по оверлею — шлёт update:open=false.
// Этот же путь отменяет confirm.
const handleOpenChange = (open: boolean) => {
  if (!open) cancel()
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="handleOpenChange">
    <DialogContent class="sm:max-w-[420px]">
      <DialogHeader>
        <DialogTitle>{{ title }}</DialogTitle>
        <DialogDescription v-if="description">
          {{ description }}
        </DialogDescription>
      </DialogHeader>

      <DialogFooter class="pt-2 gap-2">
        <Button variant="outline" @click="cancel" id="confirm-cancel-btn">
          {{ cancelText }}
        </Button>
        <Button
          :variant="variant === 'destructive' ? 'destructive' : 'default'"
          @click="accept"
          id="confirm-accept-btn"
        >
          {{ confirmText }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
