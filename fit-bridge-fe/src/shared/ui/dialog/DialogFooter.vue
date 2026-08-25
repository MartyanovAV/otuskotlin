<script setup lang="ts">
import { cn } from '@/shared/lib/utils'

const props = defineProps<{
  class?: import('clsx').ClassValue
}>()
</script>

<template>
  <div
    :class="
      cn('flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2 gap-2 mt-4', props.class)
    "
  >
    <slot />
  </div>
</template>
