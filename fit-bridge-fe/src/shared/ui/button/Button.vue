<script setup lang="ts">
import { computed } from 'vue'
import { Primitive, type PrimitiveProps } from 'radix-vue'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/shared/lib/utils'

const buttonVariants = cva(
  'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary disabled:pointer-events-none disabled:opacity-50',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-inverse shadow hover:bg-primary-hover',
        destructive: 'bg-danger text-text-inverse shadow-sm hover:bg-danger/90',
        outline:
          'border border-border bg-transparent shadow-sm hover:bg-surface-2 hover:text-text-main',
        secondary: 'bg-surface-2 text-text-main shadow-sm hover:bg-surface-3',
        ghost: 'hover:bg-surface-2 hover:text-text-main',
        link: 'text-primary underline-offset-4 hover:underline',
      },
      size: {
        default: 'h-9 px-4 py-2',
        sm: 'h-8 rounded-md px-3 text-xs',
        lg: 'h-10 rounded-md px-8',
        icon: 'h-9 w-9',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
)

type ButtonVariants = VariantProps<typeof buttonVariants>

interface Props extends PrimitiveProps {
  variant?: ButtonVariants['variant']
  size?: ButtonVariants['size']
  as?: string
  class?: import('clsx').ClassValue
}

const props = withDefaults(defineProps<Props>(), {
  as: 'button',
})

const computedClass = computed(() => {
  return cn(buttonVariants({ variant: props.variant, size: props.size }), props.class)
})
</script>

<template>
  <Primitive :as="as" :as-child="asChild" :class="computedClass">
    <slot />
  </Primitive>
</template>
