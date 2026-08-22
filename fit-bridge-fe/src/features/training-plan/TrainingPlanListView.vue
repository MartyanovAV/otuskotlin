<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { useDebounce } from '@vueuse/core'
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
import type { TrainingPlanStatus } from '@/shared/api/generated/models/trainingPlanStatus'
import PlanItemBuilder from './ui/PlanItemBuilder.vue'
import PlanItemCard from './ui/PlanItemCard.vue'
import {
  type PlanItemDraft,
  mapDraftToPlanItem,
  formatPlanStructureSummary,
} from './model/types'

const queryClient = useQueryClient()
const searchQuery = ref('')
const debouncedSearchQuery = useDebounce(searchQuery, 300)
const selectedClientFilter = ref('')
const selectedStatusFilter = ref<TrainingPlanStatus | ''>('')
const isCreateOpen = ref(false)
const selectedPlan = ref<TrainingPlanResponseObject | null>(null)
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

// Загружаем список клиентов для привязки к плану и фильтрации
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

// Загружаем тренировочные планы через TanStack Query с учетом фильтров
const { data: plansRawData, isLoading, isError, error, refetch } = useQuery({
  queryKey: ['trainingPlans', debouncedSearchQuery, selectedClientFilter, selectedStatusFilter],
  queryFn: () =>
    trainingPlanSearch({
      requestType: 'trainingPlan.search',
      requestId: crypto.randomUUID(),
      trainingPlanFilter: {
        searchString: debouncedSearchQuery.value.trim() || undefined,
        clientCardId: selectedClientFilter.value || undefined,
        status: (selectedStatusFilter.value || undefined) as TrainingPlanStatus | undefined,
        pageSize: 50,
        pageNumber: 1,
      },
    }),
})

const plans = computed<TrainingPlanResponseObject[]>(() => {
  return plansRawData.value?.data?.trainingPlans ?? []
})

const hasActiveFilters = computed(
  () => !!searchQuery.value.trim() || !!selectedClientFilter.value || !!selectedStatusFilter.value,
)

const resetFilters = () => {
  searchQuery.value = ''
  selectedClientFilter.value = ''
  selectedStatusFilter.value = ''
}

const newPlan = ref({
  title: '',
  clientCardId: '',
  items: [] as PlanItemDraft[],
})

const canSubmitPlan = computed(
  () =>
    newPlan.value.title.trim().length >= 3 &&
    newPlan.value.clientCardId.trim().length > 0 &&
    newPlan.value.items.length > 0 &&
    !isSubmitting.value,
)

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
          planItems: newPlan.value.items.map(mapDraftToPlanItem),
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
      items: [],
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
        <Button @click="isCreateOpen = true" id="create-plan-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
          Создать план
        </Button>
      </div>
    </div>

    <!-- Панель фильтров: поиск, выбор клиента, выбор статуса -->
    <div class="flex flex-wrap items-center gap-3 p-3 rounded-xl bg-surface border border-border">
      <!-- Поиск по названию -->
      <div class="flex-1 min-w-[200px]">
        <Input
          v-model="searchQuery"
          placeholder="Поиск по названию..."
          class="bg-surface-2 h-9 text-sm"
          id="plan-search-input"
        />
      </div>

      <!-- Фильтр по клиенту -->
      <div class="w-full sm:w-56">
        <select
          v-model="selectedClientFilter"
          class="flex h-9 w-full rounded-md border border-border bg-surface-2 px-3 py-1 text-sm shadow-xs transition-colors text-text-main focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary"
          id="plan-client-filter"
        >
          <option value="">Все клиенты</option>
          <option v-for="c in clientOptions" :key="c.id" :value="c.id">
            {{ c.displayName }}
          </option>
        </select>
      </div>

      <!-- Фильтр по статусу -->
      <div class="w-full sm:w-44">
        <select
          v-model="selectedStatusFilter"
          class="flex h-9 w-full rounded-md border border-border bg-surface-2 px-3 py-1 text-sm shadow-xs transition-colors text-text-main focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary"
          id="plan-status-filter"
        >
          <option value="">Все статусы</option>
          <option value="ACTIVE">Активные</option>
          <option value="ARCHIVED">В архиве</option>
        </select>
      </div>

      <!-- Кнопка сброса фильтров -->
      <Button
        v-if="hasActiveFilters"
        variant="ghost"
        size="sm"
        class="h-9 text-xs text-text-muted hover:text-text-main"
        @click="resetFilters"
        id="reset-plan-filters-btn"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1.5"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
        Сбросить
      </Button>
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
          {{ hasActiveFilters ? 'По выбранным фильтрам ничего не найдено.' : 'У вас пока нет тренировочных планов. Создайте первый план!' }}
        </p>
      </div>
      <Button v-if="hasActiveFilters" variant="outline" @click="resetFilters" id="empty-state-reset-btn">
        Сбросить фильтры
      </Button>
      <Button v-else @click="isCreateOpen = true">Создать первый план</Button>
    </div>

    <!-- Таблица тренировочных планов -->
    <div v-else class="rounded-xl border border-border bg-surface overflow-hidden shadow-xs">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="border-b border-border bg-surface-2/60 text-xs font-semibold uppercase tracking-wider text-text-muted">
            <tr>
              <th scope="col" class="py-3.5 pl-6 pr-4">Название плана</th>
              <th scope="col" class="px-4 py-3.5">Клиент</th>
              <th scope="col" class="px-4 py-3.5">Статус</th>
              <th scope="col" class="px-4 py-3.5 text-center">Упражнений</th>
              <th scope="col" class="px-4 py-3.5">Дата создания</th>
              <th scope="col" class="py-3.5 pl-4 pr-6 text-right">Действия</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-border text-text-main">
            <tr
              v-for="plan in plans"
              :key="plan.id"
              class="hover:bg-surface-2/70 transition-colors cursor-pointer group"
              @click="selectedPlan = plan"
              :id="`plan-row-${plan.id}`"
            >
              <td class="py-4 pl-6 pr-4 font-medium">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-lg bg-primary-soft text-primary flex items-center justify-center shrink-0">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"/></svg>
                  </div>
                  <div>
                    <span class="font-semibold text-text-main group-hover:text-primary transition-colors">
                      {{ plan.title }}
                    </span>
                  </div>
                </div>
              </td>
              <td class="px-4 py-4">
                <div class="flex items-center gap-2">
                  <span class="inline-flex items-center justify-center w-6 h-6 rounded-full bg-surface-3 text-[11px] font-medium text-text-muted">
                    {{ getClientNameById(plan.clientCardId).substring(0, 1).toUpperCase() }}
                  </span>
                  <span class="text-sm text-text-main">
                    {{ getClientNameById(plan.clientCardId) }}
                  </span>
                </div>
              </td>
              <td class="px-4 py-4 whitespace-nowrap">
                <Badge :variant="plan.status === 'ACTIVE' ? 'default' : 'secondary'">
                  {{ plan.status === 'ACTIVE' ? 'Активен' : (plan.status ?? 'Черновик') }}
                </Badge>
              </td>
              <td class="px-4 py-4 whitespace-nowrap">
                <div class="flex items-center gap-1.5">
                  <span class="inline-flex items-center justify-center px-2 py-0.5 rounded-md bg-surface-2 text-xs font-semibold text-text-main border border-border/80">
                    {{ (plan.planItems ?? []).length }} эл.
                  </span>
                  <span class="text-[11px] text-text-muted hidden md:inline">
                    ({{ formatPlanStructureSummary(plan.planItems) }})
                  </span>
                </div>
              </td>
              <td class="px-4 py-4 text-xs text-text-muted whitespace-nowrap">
                {{ formatDate(plan.createdAt) }}
              </td>
              <td class="py-4 pl-4 pr-6 text-right whitespace-nowrap">
                <Button
                  variant="outline"
                  size="sm"
                  class="h-8 text-xs group-hover:border-primary/50 group-hover:text-primary"
                  @click.stop="selectedPlan = plan"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                  Состав
                </Button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Модальное окно создания тренировочного плана -->
    <Dialog :open="isCreateOpen" @update:open="isCreateOpen = $event">
      <DialogContent class="sm:max-w-[550px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Новый план</DialogTitle>
          <DialogDescription>
            Заполните параметры плана и состав тренировки
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

          <!-- Конструктор элементов плана (Упражнения, Круговые, Суперсеты) -->
          <PlanItemBuilder v-model:items="newPlan.items" />

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
      <DialogContent v-if="selectedPlan" class="sm:max-w-[550px] max-h-[90vh] overflow-y-auto">
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
          <div class="flex items-center justify-between">
            <h4 class="text-xs font-semibold text-text-muted uppercase tracking-wider">
              Состав плана ({{ (selectedPlan.planItems ?? []).length }}):
            </h4>
            <span class="text-xs text-text-muted">
              {{ formatPlanStructureSummary(selectedPlan.planItems) }}
            </span>
          </div>

          <div v-if="(selectedPlan.planItems ?? []).length === 0" class="text-sm text-text-muted italic">
            В плане нет элементов
          </div>
          <div v-else class="space-y-2.5 max-h-[60vh] overflow-y-auto pr-1">
            <PlanItemCard
              v-for="(item, idx) in selectedPlan.planItems"
              :key="item.id || idx"
              :item="item"
              :index="idx"
            />
          </div>
        </div>

        <DialogFooter>
          <Button @click="selectedPlan = null">Закрыть</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
