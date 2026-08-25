<script setup lang="ts">
import { AvatarRoot } from 'radix-vue'
import { cn } from '@/shared/lib/utils'

const props = defineProps<{ class?: import('clsx').ClassValue }>()
</script>

<template>
  <AvatarRoot
    :class="cn('relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full', props.class)"
  >
    <slot />
  </AvatarRoot>
</template>
