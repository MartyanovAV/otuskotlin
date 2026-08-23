<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { useDebounce } from '@vueuse/core'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Pagination } from '@/shared/ui/pagination'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { trainingPlanSearch, useTrainingPlanCreate, useTrainingPlanUpdate, useTrainingPlanActivate } from '@/shared/api/generated/training-plan/training-plan'
import { clientCardSearch } from '@/shared/api/generated/client-card/client-card'
import type { TrainingPlanResponseObject } from '@/shared/api/generated/models/trainingPlanResponseObject'
import type { ClientCardResponseObject } from '@/shared/api/generated/models/clientCardResponseObject'
import type { TrainingPlanStatus } from '@/shared/api/generated/models/trainingPlanStatus'
import PlanItemBuilder from './ui/PlanItemBuilder.vue'
import PlanItemCard from './ui/PlanItemCard.vue'
import PlanShareButton from './ui/PlanShareButton.vue'
import CompleteTrainingModal from './ui/CompleteTrainingModal.vue'
import {
  type PlanItemDraft,
  mapDraftToPlanItem,
  mapPlanItemToDraft,
  formatPlanStructureSummary,
} from './model/types'

const queryClient = useQueryClient()
const searchQuery = ref('')
const debouncedSearchQuery = useDebounce(searchQuery, 300)
const selectedClientFilter = ref('')
const selectedStatusFilter = ref<TrainingPlanStatus | ''>('')
const pageNumber = ref(1)
const pageSize = ref(10)

watch([debouncedSearchQuery, selectedClientFilter, selectedStatusFilter], () => {
  pageNumber.value = 1
})

const isCreateOpen = ref(false)
const selectedPlan = ref<TrainingPlanResponseObject | null>(null)
const isCompleteOpen = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

// Загружаем список клиентов для привязки к плану и фильтрации
const loadAllClientCards = async (): Promise<ClientCardResponseObject[]> => {
  const cards: ClientCardResponseObject[] = []
  let page = 1

  while (true) {
    const response = await clientCardSearch({
      requestType: 'clientCard.search',
      requestId: crypto.randomUUID(),
      clientCardFilter: { pageSize: 100, pageNumber: page },
    })
    const currentPage = response.data.clientCards ?? []
    cards.push(...currentPage)
    const totalSize = response.data.totalSize ?? cards.length
    if (currentPage.length === 0 || cards.length >= totalSize) break
    page += 1
  }

  return cards
}

const { data: clientsRawData } = useQuery({
  queryKey: ['clientCardsForPlans'],
  queryFn: loadAllClientCards,
})

const clientOptions = computed<ClientCardResponseObject[]>(() => {
  return clientsRawData.value ?? []
})

// Загружаем тренировочные планы через TanStack Query с учетом фильтров и пагинации
const { data: plansRawData, isLoading, isError, error, refetch } = useQuery({
  queryKey: ['trainingPlans', debouncedSearchQuery, selectedClientFilter, selectedStatusFilter, pageNumber, pageSize],
  queryFn: () =>
    trainingPlanSearch({
      requestType: 'trainingPlan.search',
      requestId: crypto.randomUUID(),
      trainingPlanFilter: {
        searchString: debouncedSearchQuery.value.trim() || undefined,
        clientCardId: selectedClientFilter.value || undefined,
        status: (selectedStatusFilter.value || undefined) as TrainingPlanStatus | undefined,
        pageSize: pageSize.value,
        pageNumber: pageNumber.value,
      },
    }),
})

const plans = computed<TrainingPlanResponseObject[]>(() => {
  return plansRawData.value?.data?.trainingPlans ?? []
})

const totalPlansCount = computed(() => {
  return plansRawData.value?.data?.totalSize ?? plans.value.length
})

const hasActiveFilters = computed(
  () => !!searchQuery.value.trim() || !!selectedClientFilter.value || !!selectedStatusFilter.value,
)

const formatPlanStatus = (status?: TrainingPlanStatus) => {
  if (status === 'ACTIVE') return 'Активен'
  if (status === 'ARCHIVED') return 'В архиве'
  if (status === 'COMPLETED') return 'Завершён'
  if (status === 'DRAFT') return 'Черновик'
  return 'Черновик'
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedClientFilter.value = ''
  selectedStatusFilter.value = ''
  pageNumber.value = 1
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
const activateMutation = useTrainingPlanActivate()
const isActivating = ref(false)

const handleCreatePlan = async (status: TrainingPlanStatus = 'ACTIVE') => {
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
          status,
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

const handleActivatePlan = async (plan: TrainingPlanResponseObject) => {
  if (!plan.id || !plan.lock || isActivating.value) return
  isActivating.value = true

  try {
    const response = await activateMutation.mutateAsync({
      data: {
        requestType: 'trainingPlan.activate',
        requestId: crypto.randomUUID(),
        trainingPlan: {
          id: plan.id,
          lock: plan.lock,
        },
      },
    })

    if (selectedPlan.value && selectedPlan.value.id === plan.id && response.data.trainingPlan) {
      selectedPlan.value = response.data.trainingPlan
    }

    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['trainingPlans'] }),
      queryClient.invalidateQueries({ queryKey: ['trainingPlansCount'] }),
    ])
  } catch (err: unknown) {
    alert(err instanceof Error ? err.message : 'Не удалось активировать тренировочный план')
  } finally {
    isActivating.value = false
  }
}

const isEditOpen = ref(false)
const isUpdating = ref(false)
const editErrorMessage = ref<string | null>(null)
const editPlan = ref({
  id: '',
  lock: '',
  clientCardId: '',
  title: '',
  items: [] as PlanItemDraft[],
})

const updateMutation = useTrainingPlanUpdate()

const openEditPlan = (plan: TrainingPlanResponseObject) => {
  if ((plan.status !== 'ACTIVE' && plan.status !== 'DRAFT') || plan.completedAt) {
    return
  }
  editErrorMessage.value = null
  editPlan.value = {
    id: plan.id ?? '',
    lock: plan.lock ?? '',
    clientCardId: plan.clientCardId ?? '',
    title: plan.title ?? '',
    items: (plan.planItems ?? []).map(mapPlanItemToDraft),
  }
  isEditOpen.value = true
}

const canSubmitEditPlan = computed(
  () =>
    editPlan.value.title.trim().length >= 3 &&
    editPlan.value.items.length > 0 &&
    !isUpdating.value,
)

const handleUpdatePlan = async () => {
  const { id, lock } = editPlan.value
  const title = editPlan.value.title.trim()

  if (!id || !lock || !title) {
    editErrorMessage.value = 'Данные плана устарели. Пожалуйста, обновите страницу и повторите попытку.'
    return
  }

  isUpdating.value = true
  editErrorMessage.value = null

  try {
    const response = await updateMutation.mutateAsync({
      data: {
        requestType: 'trainingPlan.update',
        requestId: crypto.randomUUID(),
        trainingPlan: {
          id,
          lock,
          title,
          planItems: editPlan.value.items.map(mapDraftToPlanItem),
        },
      },
    })

    // Обновляем выбранный план, если открыта детальная модалка
    if (selectedPlan.value && selectedPlan.value.id === id && response.data.trainingPlan) {
      selectedPlan.value = response.data.trainingPlan
    }

    // Инвалидируем кэши TanStack Query
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['trainingPlans'] }),
      queryClient.invalidateQueries({ queryKey: ['trainingPlansCount'] }),
    ])

    isEditOpen.value = false
  } catch (err: unknown) {
    editErrorMessage.value = err instanceof Error ? err.message : 'Произошла ошибка, попробуйте позже'
  } finally {
    isUpdating.value = false
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

const formatDifficulty = (diff?: string) => {
  switch (diff) {
    case 'EASY': return 'Легко'
    case 'NORMAL': return 'Нормально'
    case 'HARD': return 'Тяжело (с трудом)'
    case 'MAX': return 'На пределе возможностей'
    default: return 'Не указана'
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
          <option value="DRAFT">Черновики</option>
          <option value="ACTIVE">Активные</option>
          <option value="COMPLETED">Завершённые</option>
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
                <Badge :variant="plan.status === 'ACTIVE' ? 'default' : plan.status === 'COMPLETED' ? 'outline' : 'secondary'" class="w-fit">
                  {{ formatPlanStatus(plan.status) }}
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
                <div class="flex items-center justify-end gap-2" @click.stop>
                  <PlanShareButton :plan="plan" :show-label="false" />
                  <Button
                    v-if="plan.status === 'DRAFT'"
                    variant="default"
                    size="sm"
                    class="h-8 text-xs bg-emerald-600 hover:bg-emerald-700 text-white"
                    @click.stop="handleActivatePlan(plan)"
                    :disabled="isActivating"
                    :id="`activate-plan-btn-${plan.id}`"
                    title="Активировать план"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                    Активировать
                  </Button>
                  <Button
                    v-if="(plan.status === 'ACTIVE' || plan.status === 'DRAFT') && !plan.completedAt"
                    variant="outline"
                    size="sm"
                    class="h-8 text-xs group-hover:border-primary/50 group-hover:text-primary"
                    @click.stop="openEditPlan(plan)"
                    :id="`edit-plan-btn-${plan.id}`"
                    title="Редактировать план"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/><path d="m15 5 4 4"/></svg>
                    Изменить
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    class="h-8 text-xs group-hover:border-primary/50 group-hover:text-primary"
                    @click.stop="selectedPlan = plan"
                    :id="`view-plan-btn-${plan.id}`"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                    Состав
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Пагинация списка тренировочных планов -->
      <div class="border-t border-border px-4 bg-surface-2/30">
        <Pagination
          v-model:pageNumber="pageNumber"
          v-model:pageSize="pageSize"
          :totalSize="totalPlansCount"
        />
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

        <form @submit.prevent="handleCreatePlan('ACTIVE')" class="space-y-4 py-2">
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

          <DialogFooter class="flex flex-col sm:flex-row sm:justify-end gap-2">
            <Button type="button" variant="outline" @click="isCreateOpen = false">
              Отмена
            </Button>
            <Button type="button" variant="secondary" :disabled="!canSubmitPlan" @click="handleCreatePlan('DRAFT')" id="save-draft-plan-btn">
              {{ isSubmitting ? 'Сохранение...' : 'Сохранить как черновик' }}
            </Button>
            <Button type="submit" :disabled="!canSubmitPlan" id="submit-plan-btn">
              {{ isSubmitting ? 'Создание...' : 'Создать и активировать' }}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно редактирования тренировочного плана -->
    <Dialog :open="isEditOpen" @update:open="isEditOpen = $event">
      <DialogContent class="sm:max-w-[550px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Редактирование плана</DialogTitle>
          <DialogDescription>
            Внесите изменения в параметры плана и состав тренировки
          </DialogDescription>
        </DialogHeader>

        <div v-if="editErrorMessage" class="p-3 bg-danger-soft text-danger text-xs rounded-md" id="edit-plan-error">
          {{ editErrorMessage }}
        </div>

        <form @submit.prevent="handleUpdatePlan" class="space-y-4 py-2">
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="edit-plan-name">Название плана *</label>
            <Input
              id="edit-plan-name"
              v-model="editPlan.title"
              placeholder="например, Силовой 4-недельный цикл"
              required
              class="bg-surface-2"
            />
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-muted">Клиент</label>
            <div class="flex items-center gap-2 p-2.5 rounded-md border border-border bg-surface-3 text-sm text-text-main">
              <span class="inline-flex items-center justify-center w-6 h-6 rounded-full bg-surface-2 text-[11px] font-medium text-text-muted">
                {{ getClientNameById(editPlan.clientCardId).substring(0, 1).toUpperCase() }}
              </span>
              <span>{{ getClientNameById(editPlan.clientCardId) }}</span>
            </div>
          </div>

          <!-- Конструктор элементов плана (Упражнения, Круговые, Суперсеты) -->
          <PlanItemBuilder v-model:items="editPlan.items" />

          <DialogFooter>
            <Button type="button" variant="outline" @click="isEditOpen = false">
              Отмена
            </Button>
            <Button type="submit" :disabled="!canSubmitEditPlan" id="save-plan-btn">
              {{ isUpdating ? 'Сохранение...' : 'Сохранить изменения' }}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно деталей плана -->
    <Dialog :open="!!selectedPlan" @update:open="(val) => { if (!val) selectedPlan = null }">
      <DialogContent v-if="selectedPlan" class="sm:max-w-[550px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{{ selectedPlan.title }}</DialogTitle>
          <DialogDescription>
            Клиент: {{ getClientNameById(selectedPlan.clientCardId) }}
          </DialogDescription>
        </DialogHeader>

        <div class="space-y-3 py-2 text-sm">
          <div class="flex justify-between border-b border-border pb-2">
            <span class="text-text-muted">Статус:</span>
            <Badge :variant="selectedPlan.status === 'ACTIVE' ? 'default' : 'secondary'">
              {{ formatPlanStatus(selectedPlan.status) }}
            </Badge>
          </div>

          <!-- Блок с информацией о завершении -->
          <div v-if="selectedPlan.completedAt" class="p-3 bg-primary-soft/30 rounded-lg border border-primary/20 space-y-1 mb-2">
            <div class="flex items-center justify-between">
              <span class="text-xs font-medium text-text-muted">Завершена:</span>
              <span class="text-xs font-semibold text-text-main">{{ formatDate(selectedPlan.completedAt) }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-xs font-medium text-text-muted">Сложность:</span>
              <span class="text-xs font-semibold text-text-main">{{ formatDifficulty(selectedPlan.difficulty) }}</span>
            </div>
            <div v-if="selectedPlan.coachComment" class="pt-2 mt-2 border-t border-primary/10">
              <span class="text-xs font-medium text-text-muted block mb-1">Комментарий тренера:</span>
              <p class="text-xs text-text-main italic whitespace-pre-wrap">{{ selectedPlan.coachComment }}</p>
            </div>
          </div>

          <div class="flex justify-between text-xs text-text-faint pb-1">
            <span>Дата создания:</span>
            <span class="font-medium text-text-muted">{{ formatDate(selectedPlan.createdAt) }}</span>
          </div>

          <!-- Секция состава плана -->
          <div class="border-t border-border pt-3 space-y-2.5">
            <div class="flex items-center justify-between">
              <h4 class="font-semibold text-sm text-text-main">
                Состав плана ({{ (selectedPlan.planItems ?? []).length }}):
              </h4>
              <span class="text-xs text-text-muted">
                {{ formatPlanStructureSummary(selectedPlan.planItems) }}
              </span>
            </div>

            <div v-if="(selectedPlan.planItems ?? []).length === 0" class="text-sm text-text-muted italic">
              В плане нет элементов
            </div>
            <div v-else class="space-y-2.5 max-h-[50vh] overflow-y-auto pr-1">
              <PlanItemCard
                v-for="(item, idx) in selectedPlan.planItems"
                :key="item.id || idx"
                :item="item"
                :index="idx"
              />
            </div>
          </div>
        </div>

        <DialogFooter class="flex flex-col sm:flex-row sm:justify-between items-stretch sm:items-center w-full gap-2 pt-2">
          <div>
            <PlanShareButton :plan="selectedPlan" variant="outline" placement="top-left" />
          </div>
          <div class="flex items-center justify-end gap-2">
            <Button
              v-if="selectedPlan.status === 'DRAFT'"
              variant="default"
              class="bg-emerald-600 hover:bg-emerald-700 text-white"
              @click="handleActivatePlan(selectedPlan)"
              :disabled="isActivating"
              id="details-activate-plan-btn"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><polygon points="5 3 19 12 5 21 5 3"/></svg>
              Активировать
            </Button>
            <Button
              v-if="(selectedPlan.status === 'ACTIVE' || selectedPlan.status === 'DRAFT') && !selectedPlan.completedAt"
              variant="outline"
              @click="openEditPlan(selectedPlan)"
              id="details-edit-plan-btn"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/><path d="m15 5 4 4"/></svg>
              Редактировать
            </Button>
            <Button
              v-if="selectedPlan.status === 'ACTIVE' && !selectedPlan.completedAt"
              variant="default"
              @click="isCompleteOpen = true"
            >
              Завершить
            </Button>
            <Button variant="outline" @click="selectedPlan = null">Закрыть</Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <CompleteTrainingModal
      v-if="selectedPlan"
      :open="isCompleteOpen"
      @update:open="isCompleteOpen = $event"
      :planId="selectedPlan.id!"
      :planTitle="selectedPlan.title!"
      :clientName="getClientNameById(selectedPlan.clientCardId)"
      :planLock="selectedPlan.lock!"
      @completed="selectedPlan = null"
    />
  </div>
</template>
