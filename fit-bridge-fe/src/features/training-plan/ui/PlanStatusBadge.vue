<script setup lang="ts">
import { computed } from 'vue'
import { Badge } from '@/shared/ui/badge'
import type { TrainingPlanStatus } from '@/shared/api/generated/models/trainingPlanStatus'

const props = defineProps<{
  status?: TrainingPlanStatus
}>()

interface StatusConfig {
  label: string
  variant: 'default' | 'secondary' | 'outline' | 'destructive'
  icon: string
}

const STATUS_CONFIG: Record<TrainingPlanStatus, StatusConfig> = {
  DRAFT: {
    label: 'Черновик',
    variant: 'secondary',
    icon: 'M12 8v4l3 2 M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z',
  },
  ACTIVE: {
    label: 'Активен',
    variant: 'default',
    icon: 'M5 13l4 4L19 7',
  },
  COMPLETED: {
    label: 'Завершён',
    variant: 'outline',
    icon: 'M9 12l2 2 4-4 M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Z',
  },
  ARCHIVED: {
    label: 'В архиве',
    variant: 'outline',
    icon: 'M3 7h18M5 7v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2',
  },
}

const FALLBACK: StatusConfig = { label: '—', variant: 'outline', icon: '' }

const config = computed<StatusConfig>(() => (props.status ? STATUS_CONFIG[props.status] : FALLBACK))
</script>

<template>
  <Badge :variant="config.variant" class="gap-1">
    <svg
      v-if="config.icon"
      xmlns="http://www.w3.org/2000/svg"
      width="12"
      height="12"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2.5"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      <path :d="config.icon" />
    </svg>
    {{ config.label }}
  </Badge>
</template>
