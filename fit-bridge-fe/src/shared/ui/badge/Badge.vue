<script setup lang="ts">
import { computed } from 'vue'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/shared/lib/utils'

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary text-primary-inverse hover:bg-primary-hover',
        secondary: 'border-transparent bg-surface-2 text-text-main hover:bg-surface-3',
        destructive: 'border-transparent bg-danger text-text-inverse hover:bg-danger/80',
        outline: 'text-text-main border-border',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
)

type BadgeVariants = VariantProps<typeof badgeVariants>

const props = defineProps<{
  variant?: BadgeVariants['variant']
  class?: import('clsx').ClassValue
}>()

const computedClass = computed(() => {
  return cn(badgeVariants({ variant: props.variant }), props.class)
})
</script>

<template>
  <div :class="computedClass">
    <slot />
  </div>
</template>
