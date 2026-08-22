<script setup lang="ts">
import { ref } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { useTrainingPlanComplete } from '@/shared/api/generated/training-plan/training-plan'

const props = defineProps<{
  open: boolean
  planId: string
  planTitle: string
  clientName: string
  planLock?: string
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'completed'): void
}>()

const queryClient = useQueryClient()
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

const difficulty = ref<'EASY' | 'NORMAL' | 'HARD' | 'MAX'>('NORMAL')
const coachComment = ref('')

const difficultyOptions = [
  { value: 'EASY', label: 'Легко' },
  { value: 'NORMAL', label: 'Нормально' },
  { value: 'HARD', label: 'Тяжело (с трудом)' },
  { value: 'MAX', label: 'На пределе возможностей' },
]

const completeMutation = useTrainingPlanComplete()

const handleSubmit = async () => {
  isSubmitting.value = true
  errorMessage.value = null

  try {
    const timestamp = new Date().toISOString()

    await completeMutation.mutateAsync({
      data: {
        requestType: 'trainingPlan.complete',
        requestId: crypto.randomUUID(),
        trainingPlan: {
          id: props.planId,
          lock: props.planLock,
          completedAt: timestamp,
          difficulty: difficulty.value,
          coachComment: coachComment.value.trim(),
        },
      },
    })

    // Обновляем кэш
    await queryClient.invalidateQueries({ queryKey: ['trainingPlans'] })

    emit('completed')
    emit('update:open', false)

    // Сброс формы
    difficulty.value = 'NORMAL'
    coachComment.value = ''
  } catch (err: unknown) {
    errorMessage.value = err instanceof Error ? err.message : 'Произошла ошибка при завершении тренировки'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <Dialog :open="open" @update:open="(val) => emit('update:open', val)">
    <DialogContent class="sm:max-w-[450px]">
      <DialogHeader>
        <DialogTitle>Завершение тренировки</DialogTitle>
        <DialogDescription>
          Отметьте тренировку "{{ planTitle }}" для клиента {{ clientName }} как завершенную.
        </DialogDescription>
      </DialogHeader>

      <div v-if="errorMessage" class="p-3 bg-danger-soft text-danger text-xs rounded-md mb-2">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-4 py-2">
        <div class="space-y-2">
          <label class="text-sm font-medium text-text-main">Сложность тренировки</label>
          <div class="grid grid-cols-2 gap-2">
            <label
              v-for="opt in difficultyOptions"
              :key="opt.value"
              class="flex flex-col items-center justify-center p-3 rounded-md border cursor-pointer transition-all"
              :class="difficulty === opt.value ? 'border-primary bg-primary-soft text-primary' : 'border-border bg-surface-2 text-text-main hover:bg-surface-3'"
            >
              <input
                type="radio"
                name="difficulty"
                :value="opt.value"
                v-model="difficulty"
                class="sr-only"
              />
              <span class="text-xs font-semibold text-center">{{ opt.label }}</span>
            </label>
          </div>
        </div>

        <div class="space-y-1.5">
          <label class="text-sm font-medium text-text-main" for="coach-comment">Комментарий тренера</label>
          <textarea
            id="coach-comment"
            v-model="coachComment"
            rows="3"
            placeholder="Опишите, как прошла тренировка, самочувствие клиента и на что обратить внимание в следующий раз..."
            class="flex w-full rounded-md border border-border bg-surface-2 px-3 py-2 text-sm shadow-sm transition-colors placeholder:text-text-muted focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary resize-none"
          ></textarea>
        </div>

        <DialogFooter class="pt-2">
          <Button type="button" variant="outline" @click="emit('update:open', false)">
            Отмена
          </Button>
          <Button type="submit" :disabled="isSubmitting" id="submit-complete-btn">
            {{ isSubmitting ? 'Сохранение...' : 'Завершить' }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>
