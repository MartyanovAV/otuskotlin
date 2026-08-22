<script setup lang="ts">
import { DialogTitle, type DialogTitleProps } from 'radix-vue'
import { cn } from '@/shared/lib/utils'

const props = defineProps<DialogTitleProps & { class?: import('clsx').ClassValue }>()
</script>

<template>
  <DialogTitle
    v-bind="props"
    :class="cn('text-lg font-semibold leading-none tracking-tight text-text-main', props.class)"
  >
    <slot />
  </DialogTitle>
</template>
