<script setup lang="ts">
import { AvatarFallback } from 'radix-vue'
import { cn } from '@/shared/lib/utils'

const props = defineProps<{ class?: import('clsx').ClassValue }>()
</script>

<template>
  <AvatarFallback
    :class="
      cn(
        'flex h-full w-full items-center justify-center rounded-full bg-surface-2 text-text-main',
        props.class,
      )
    "
  >
    <slot />
  </AvatarFallback>
</template>
