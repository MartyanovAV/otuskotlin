<script setup lang="ts">
import { cn } from '@/shared/lib/utils'

const props = defineProps<{
  class?: import('clsx').ClassValue
}>()
</script>

<template>
  <div :class="cn('flex flex-col space-y-1.5 text-center sm:text-left', props.class)">
    <slot />
  </div>
</template>
