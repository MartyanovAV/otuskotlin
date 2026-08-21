<script setup lang="ts">
import { DialogDescription, type DialogDescriptionProps } from 'radix-vue'
import { cn } from '@/shared/lib/utils'

const props = defineProps<DialogDescriptionProps & { class?: import('clsx').ClassValue }>()
</script>

<template>
  <DialogDescription
    v-bind="props"
    :class="cn('text-sm text-text-muted', props.class)"
  >
    <slot />
  </DialogDescription>
</template>
