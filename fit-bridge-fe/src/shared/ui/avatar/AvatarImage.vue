<script setup lang="ts">
import { AvatarImage, type AvatarImageProps } from 'radix-vue'
import { cn } from '@/shared/lib/utils'

interface Props extends AvatarImageProps {
  class?: import('clsx').ClassValue
  alt?: string
}

const props = defineProps<Props>()
</script>

<template>
  <AvatarImage
    :src="props.src"
    :alt="alt"
    :as="props.as"
    :as-child="props.asChild"
    :class="cn('aspect-square h-full w-full object-cover', props.class)"
  />
</template>
