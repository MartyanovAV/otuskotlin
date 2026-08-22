<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { useDebounce } from '@vueuse/core'
import { Card, CardContent } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { trainingPlanSearch, useTrainingPlanCreate } from '@/shared/api/generated/training-plan/training-plan'
import { clientCardSearch } from '@/shared/api/generated/client-card/client-card'
import type { TrainingPlanResponseObject } from '@/shared/api/generated/models/trainingPlanResponseObject'
import type { ClientCardResponseObject } from '@/shared/api/generated/models/clientCardResponseObject'

const queryClient = useQueryClient()
const searchQuery = ref('')
const debouncedSearchQuery = useDebounce(searchQuery, 300)
const isCreateOpen = ref(false)
const selectedPlan = ref<TrainingPlanResponseObject | null>(null)
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

// Загружаем список клиентов для привязки к плану
const { data: clientsRawData } = useQuery({
  queryKey: ['clientCardsForPlans'],
  queryFn: () =>
    clientCardSearch({
      requestType: 'clientCard.search',
      requestId: crypto.randomUUID(),
      clientCardFilter: { pageSize: 50, pageNumber: 1 },
    }),
})

const clientOptions = computed<ClientCardResponseObject[]>(() => {
  return clientsRawData.value?.data?.clientCards ?? []
})

// Загружаем тренировочные планы через TanStack Query
const { data: plansRawData, isLoading, isError, error, refetch } = useQuery({
  queryKey: ['trainingPlans', debouncedSearchQuery],
  queryFn: () =>
    trainingPlanSearch({
      requestType: 'trainingPlan.search',
      requestId: crypto.randomUUID(),
      trainingPlanFilter: {
        searchString: debouncedSearchQuery.value.trim() || undefined,
        pageSize: 50,
        pageNumber: 1,
      },
    }),
})

const plans = computed<TrainingPlanResponseObject[]>(() => {
  return plansRawData.value?.data?.trainingPlans ?? []
})

interface ExerciseDraft {
  name: string
  sets: number
  reps: number
  weight?: number
}

const newPlan = ref({
  title: '',
  clientCardId: '',
  exercises: [] as ExerciseDraft[],
})

const newExerciseName = ref('')
const newExerciseSets = ref(3)
const newExerciseReps = ref(10)
const newExerciseWeight = ref(0)

const canSubmitPlan = computed(
  () =>
    newPlan.value.title.trim().length >= 3 &&
    newPlan.value.clientCardId.trim().length > 0 &&
    newPlan.value.exercises.length > 0 &&
    !isSubmitting.value,
)

const addExerciseToForm = () => {
  if (!newExerciseName.value.trim()) return
  newPlan.value.exercises.push({
    name: newExerciseName.value.trim(),
    sets: Number(newExerciseSets.value) || 3,
    reps: Number(newExerciseReps.value) || 10,
    weight: Number(newExerciseWeight.value) || 0,
  })
  newExerciseName.value = ''
}

const removeExercise = (index: number) => {
  newPlan.value.exercises.splice(index, 1)
}

const createMutation = useTrainingPlanCreate()

const handleCreatePlan = async () => {
  if (!canSubmitPlan.value) return
  isSubmitting.value = true
  errorMessage.value = null

  try {
    await createMutation.mutateAsync({
      data: {
        requestType: 'trainingPlan.create',
        requestId: crypto.randomUUID(),
        trainingPlan: {
          title: newPlan.value.title.trim(),
          clientCardId: newPlan.value.clientCardId,
          planItems: newPlan.value.exercises.map((e) => ({
            id: crypto.randomUUID(),
            itemType: 'EXERCISE',
            title: e.name,
            description: `${e.sets} подх. × ${e.reps} повт.${e.weight ? ` (${e.weight} кг)` : ''}`,
          })),
        },
      },
    })

    // Инвалидируем кэши TanStack Query
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['trainingPlans'] }),
      queryClient.invalidateQueries({ queryKey: ['trainingPlansCount'] }),
    ])

    // Сброс формы и закрытие диалога
    newPlan.value = {
      title: '',
      clientCardId: '',
      exercises: [],
    }
    isCreateOpen.value = false
  } catch (err: unknown) {
    errorMessage.value = err instanceof Error ? err.message : 'Произошла ошибка, попробуйте позже'
  } finally {
    isSubmitting.value = false
  }
}

const getClientNameById = (clientCardId?: string) => {
  if (!clientCardId) return 'Общий план'
  const found = clientOptions.value.find((c: ClientCardResponseObject) => c.id === clientCardId)
  return found?.displayName ?? 'Клиент'
}

const getItemTypeLabel = (type?: string) => {
  switch (type) {
    case 'EXERCISE':
      return 'Упражнение'
    case 'CIRCUIT':
      return 'Круговая'
    case 'SUPERSET':
      return 'Суперсет'
    default:
      return type ?? 'Упражнение'
  }
}

const formatDate = (isoString?: string) => {
  if (!isoString) return '—'
  try {
    return new Date(isoString).toLocaleString('ru-RU', {
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return isoString
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold tracking-tight text-text-main">Тренировочные планы</h2>
        <p class="text-sm text-text-muted">Программы тренировок</p>
      </div>
      <div class="flex items-center gap-3">
        <Input
          v-model="searchQuery"
          placeholder="Поиск по названию..."
          class="w-64 bg-surface"
        />
        <Button @click="isCreateOpen = true" id="create-plan-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
          Создать план
        </Button>
      </div>
    </div>

    <!-- Состояние загрузки -->
    <div v-if="isLoading" class="flex items-center justify-center p-12">
      <div class="text-center space-y-3">
        <div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
        <p class="text-sm text-text-muted">Загрузка планов...</p>
      </div>
    </div>

    <!-- Ошибка загрузки -->
    <div v-else-if="isError" class="p-6 rounded-xl border border-danger/30 bg-danger-soft text-center space-y-3">
      <p class="text-sm font-medium text-danger">Не удалось загрузить планы</p>
      <p class="text-xs text-text-muted">{{ error }}</p>
      <Button variant="outline" size="sm" @click="() => refetch()">Повторить попытку</Button>
    </div>

    <!-- Пустой список планов -->
    <div v-else-if="plans.length === 0" class="p-12 text-center border border-dashed border-border rounded-xl bg-surface/50 space-y-4">
      <div class="mx-auto w-12 h-12 rounded-full bg-surface-2 flex items-center justify-center text-text-muted">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"/></svg>
      </div>
      <div>
        <h3 class="font-semibold text-text-main text-lg">Тренировочные планы не найдены</h3>
        <p class="text-sm text-text-muted mt-1 max-w-sm mx-auto">
          {{ searchQuery ? 'По вашему поисковому запросу ничего не найдено.' : 'У вас пока нет тренировочных планов. Создайте первый план!' }}
        </p>
      </div>
      <Button v-if="!searchQuery" @click="isCreateOpen = true">Создать первый план</Button>
    </div>

    <!-- Сетка тренировочных планов -->
    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <Card
        v-for="plan in plans"
        :key="plan.id"
        class="cursor-pointer transition-all hover:border-primary/50 hover:shadow-md"
        @click="selectedPlan = plan"
      >
        <CardContent class="p-6">
          <div class="flex items-start justify-between">
            <div>
              <h3 class="font-semibold leading-snug text-text-main">{{ plan.title }}</h3>
              <p class="text-xs font-medium text-primary mt-1">
                {{ getClientNameById(plan.clientCardId) }}
              </p>
            </div>
            <Badge :variant="plan.status === 'ACTIVE' ? 'default' : 'secondary'">
              {{ plan.status === 'ACTIVE' ? 'Активен' : (plan.status ?? 'Черновик') }}
            </Badge>
          </div>

          <div class="mt-4 pt-3 border-t border-border flex items-center justify-between text-xs text-text-muted">
            <span>Упражнений:</span>
            <span class="font-bold text-text-main">{{ (plan.planItems ?? []).length }}</span>
          </div>

          <div class="mt-2 text-xs text-text-faint flex items-center justify-between">
            <span>Создан:</span>
            <span>{{ formatDate(plan.createdAt) }}</span>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Модальное окно создания тренировочного плана -->
    <Dialog :open="isCreateOpen" @update:open="isCreateOpen = $event">
      <DialogContent class="sm:max-w-[550px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Новый план</DialogTitle>
          <DialogDescription>
            Заполните параметры плана
          </DialogDescription>
        </DialogHeader>

        <div v-if="errorMessage" class="p-3 bg-danger-soft text-danger text-xs rounded-md">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleCreatePlan" class="space-y-4 py-2">
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="plan-name">Название плана *</label>
            <Input
              id="plan-name"
              v-model="newPlan.title"
              placeholder="например, Силовой 4-недельный цикл"
              required
              class="bg-surface-2"
            />
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="plan-client-select">Клиент *</label>
            <select
              id="plan-client-select"
              v-model="newPlan.clientCardId"
              required
              class="flex h-9 w-full rounded-md border border-border bg-surface-2 px-3 py-1 text-sm shadow-sm transition-colors text-text-main focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary"
            >
              <option value="" disabled>Выберите клиента</option>
              <option v-for="c in clientOptions" :key="c.id" :value="c.id">
                {{ c.displayName }}
              </option>
            </select>
          </div>

          <!-- Список добавленных упражнений -->
          <div class="space-y-2 border-t border-border pt-3">
            <div class="flex items-center justify-between">
              <label class="text-sm font-semibold text-text-main">
                Упражнения в плане ({{ newPlan.exercises.length }})
              </label>
            </div>

            <div class="space-y-2 max-h-40 overflow-y-auto pr-1">
              <div
                v-for="(exercise, index) in newPlan.exercises"
                :key="index"
                class="flex items-center justify-between bg-surface-2 p-2.5 rounded-lg text-xs"
              >
                <div>
                  <span class="font-semibold text-text-main">{{ exercise.name }}</span>
                  <span class="text-text-muted ml-2">
                    {{ exercise.sets }} подх. × {{ exercise.reps }} повт.
                    <span v-if="exercise.weight">({{ exercise.weight }} кг)</span>
                  </span>
                </div>
                <button
                  type="button"
                  @click="removeExercise(index)"
                  class="text-danger hover:text-danger/80 p-1"
                  title="Удалить"
                >
                  ✕
                </button>
              </div>
            </div>

            <!-- Добавление нового упражнения -->
            <div class="bg-surface-3 p-3 rounded-lg space-y-2 mt-2">
              <span class="text-xs font-medium text-text-muted">Добавить упражнение в план:</span>
              <div class="flex gap-2">
                <Input
                  v-model="newExerciseName"
                  placeholder="Название упражнения..."
                  class="flex-1 bg-surface h-8 text-xs"
                  id="exercise-name-input"
                />
              </div>
              <div class="flex items-center gap-2">
                <div class="flex-1 flex items-center gap-1">
                  <label for="exercise-sets" class="text-xs text-text-muted cursor-pointer">Подходы:</label>
                  <Input
                    id="exercise-sets"
                    type="number"
                    v-model="newExerciseSets"
                    min="1"
                    class="w-14 bg-surface h-7 text-xs"
                  />
                </div>
                <div class="flex-1 flex items-center gap-1">
                  <label for="exercise-reps" class="text-xs text-text-muted cursor-pointer">Повт:</label>
                  <Input
                    id="exercise-reps"
                    type="number"
                    v-model="newExerciseReps"
                    min="1"
                    class="w-14 bg-surface h-7 text-xs"
                  />
                </div>
                <div class="flex-1 flex items-center gap-1">
                  <label for="exercise-weight" class="text-xs text-text-muted cursor-pointer">Вес(кг):</label>
                  <Input
                    id="exercise-weight"
                    type="number"
                    v-model="newExerciseWeight"
                    min="0"
                    class="w-14 bg-surface h-7 text-xs"
                  />
                </div>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  class="h-7 text-xs"
                  @click="addExerciseToForm"
                  id="add-exercise-btn"
                >
                  + Добавить
                </Button>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" @click="isCreateOpen = false">
              Отмена
            </Button>
            <Button type="submit" :disabled="!canSubmitPlan" id="submit-plan-btn">
              {{ isSubmitting ? 'Создание...' : 'Создать' }}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно деталей плана -->
    <Dialog :open="!!selectedPlan" @update:open="(val) => { if (!val) selectedPlan = null }">
      <DialogContent v-if="selectedPlan" class="sm:max-w-[500px]">
        <DialogHeader>
          <div class="flex justify-between items-start">
            <div>
              <DialogTitle>{{ selectedPlan.title }}</DialogTitle>
              <DialogDescription class="mt-1">
                {{ getClientNameById(selectedPlan.clientCardId) }}
              </DialogDescription>
            </div>
            <Badge :variant="selectedPlan.status === 'ACTIVE' ? 'default' : 'secondary'">
              {{ selectedPlan.status === 'ACTIVE' ? 'Активен' : (selectedPlan.status ?? 'Черновик') }}
            </Badge>
          </div>
        </DialogHeader>

        <div class="space-y-3 py-2">
          <h4 class="text-xs font-semibold text-text-muted uppercase tracking-wider">Состав тренировки:</h4>
          <div v-if="(selectedPlan.planItems ?? []).length === 0" class="text-sm text-text-muted italic">
            В плане нет упражнений
          </div>
          <div v-else class="space-y-2">
            <div
              v-for="(item, idx) in selectedPlan.planItems"
              :key="idx"
              class="flex items-center justify-between bg-surface-2 p-3 rounded-lg"
            >
              <div>
                <p class="font-medium text-sm text-text-main">{{ item.title ?? 'Упражнение' }}</p>
                <p class="text-xs text-text-muted mt-0.5">{{ item.description }}</p>
              </div>
              <Badge variant="outline" class="text-xs">
                {{ getItemTypeLabel(item.itemType) }}
              </Badge>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button @click="selectedPlan = null">Закрыть</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
