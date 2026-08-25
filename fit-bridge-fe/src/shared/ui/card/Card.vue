<script setup lang="ts">
import { cn } from '@/shared/lib/utils'

const props = defineProps<{
  class?: import('clsx').ClassValue
}>()
</script>

<template>
  <div
    :class="cn('rounded-xl border border-border bg-surface text-text-main shadow-sm', props.class)"
  >
    <slot />
  </div>
</template>
