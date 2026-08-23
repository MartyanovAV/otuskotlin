<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { useDebounce } from '@vueuse/core'
import { Avatar, AvatarFallback } from '@/shared/ui/avatar'
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
import {
  clientCardSearch,
  useClientCardCreate,
  useClientCardUpdate,
} from '@/shared/api/generated/client-card/client-card'
import {
  trainingPlanSearch,
  useTrainingPlanCreate,
  useTrainingPlanActivate,
} from '@/shared/api/generated/training-plan/training-plan'
import type { ClientCardResponseObject } from '@/shared/api/generated/models/clientCardResponseObject'
import type { TrainingPlanResponseObject } from '@/shared/api/generated/models/trainingPlanResponseObject'
import type { TrainingPlanStatus } from '@/shared/api/generated/models/trainingPlanStatus'
import PlanItemBuilder from '@/features/training-plan/ui/PlanItemBuilder.vue'
import PlanItemCard from '@/features/training-plan/ui/PlanItemCard.vue'
import PlanShareButton from '@/features/training-plan/ui/PlanShareButton.vue'
import {
  type PlanItemDraft,
  mapDraftToPlanItem,
  formatPlanStructureSummary,
} from '@/features/training-plan/model/types'

const queryClient = useQueryClient()
const searchQuery = ref('')
const debouncedSearchQuery = useDebounce(searchQuery, 300)
const pageNumber = ref(1)
const pageSize = ref(10)

watch(debouncedSearchQuery, () => {
  pageNumber.value = 1
})

const isCreateOpen = ref(false)
const isEditOpen = ref(false)
const selectedClient = ref<ClientCardResponseObject | null>(null)
const isSubmitting = ref(false)
const isUpdating = ref(false)
const errorMessage = ref<string | null>(null)
const editErrorMessage = ref<string | null>(null)

// Состояние для создания и просмотра планов клиента
const isCreatePlanOpen = ref(false)
const selectedPlanDetails = ref<TrainingPlanResponseObject | null>(null)
const isPlanSubmitting = ref(false)
const planErrorMessage = ref<string | null>(null)

const newClientPlan = ref({
  title: '',
  items: [] as PlanItemDraft[],
})

const newClient = ref({
  displayName: '',
  note: '',
})

const editClient = ref({
  id: '',
  displayName: '',
  note: '',
  lock: '',
})

// Запрос списка клиентов через TanStack Query с учетом пагинации
const { data: searchResponse, isLoading, isError, error, refetch } = useQuery({
  queryKey: ['clientCards', debouncedSearchQuery, pageNumber, pageSize],
  queryFn: () =>
    clientCardSearch({
      requestType: 'clientCard.search',
      requestId: crypto.randomUUID(),
      clientCardFilter: {
        searchString: debouncedSearchQuery.value.trim() || undefined,
        pageSize: pageSize.value,
        pageNumber: pageNumber.value,
      },
    }),
})

const clientCards = computed<ClientCardResponseObject[]>(() => {
  return searchResponse.value?.data?.clientCards ?? []
})

const totalClientsCount = computed(() => {
  return searchResponse.value?.data?.totalSize ?? clientCards.value.length
})

// Запрос тренировочных планов для выбранного клиента
const selectedClientId = computed(() => selectedClient.value?.id)

const loadAllClientPlans = async (): Promise<TrainingPlanResponseObject[]> => {
  if (!selectedClientId.value) return []
  const plans: TrainingPlanResponseObject[] = []
  let page = 1

  while (true) {
    const response = await trainingPlanSearch({
      requestType: 'trainingPlan.search',
      requestId: crypto.randomUUID(),
      trainingPlanFilter: {
        clientCardId: selectedClientId.value,
        pageSize: 100,
        pageNumber: page,
      },
    })
    const currentPage = response.data.trainingPlans ?? []
    plans.push(...currentPage)
    const totalSize = response.data.totalSize ?? plans.length
    if (currentPage.length === 0 || plans.length >= totalSize) break
    page += 1
  }

  return plans
}

const { data: clientPlansData, isLoading: isClientPlansLoading } = useQuery({
  queryKey: computed(() => ['clientPlans', selectedClientId.value]),
  queryFn: loadAllClientPlans,
  enabled: computed(() => !!selectedClientId.value),
})

const clientPlans = computed<TrainingPlanResponseObject[]>(() => {
  return clientPlansData.value ?? []
})

const formatPlanStatus = (status?: TrainingPlanStatus) => {
  if (status === 'ACTIVE') return 'Активен'
  if (status === 'ARCHIVED') return 'В архиве'
  if (status === 'COMPLETED') return 'Завершён'
  if (status === 'DRAFT') return 'Черновик'
  return 'Черновик'
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

const createMutation = useClientCardCreate()
const updateMutation = useClientCardUpdate()
const createPlanMutation = useTrainingPlanCreate()
const activatePlanMutation = useTrainingPlanActivate()
const isActivatingPlan = ref(false)

const handleCreateClient = async () => {
  if (!newClient.value.displayName.trim()) return
  isSubmitting.value = true
  errorMessage.value = null

  try {
    await createMutation.mutateAsync({
      data: {
        requestType: 'clientCard.create',
        requestId: crypto.randomUUID(),
        clientCard: {
          displayName: newClient.value.displayName.trim(),
          note: newClient.value.note.trim() || undefined,
        },
      },
    })

    // Инвалидируем кэши TanStack Query
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['clientCards'] }),
      queryClient.invalidateQueries({ queryKey: ['clientCardsCount'] }),
      queryClient.invalidateQueries({ queryKey: ['clientCardsForPlans'] }),
    ])

    // Сброс формы и закрытие диалога
    newClient.value = {
      displayName: '',
      note: '',
    }
    isCreateOpen.value = false
  } catch (err: unknown) {
    errorMessage.value = err instanceof Error ? err.message : 'Произошла ошибка, попробуйте позже'
  } finally {
    isSubmitting.value = false
  }
}

const openEditClient = (client: ClientCardResponseObject) => {
  editErrorMessage.value = null
  editClient.value = {
    id: client.id ?? '',
    displayName: client.displayName ?? '',
    note: client.note ?? '',
    lock: client.lock ?? '',
  }
  selectedClient.value = null
  isEditOpen.value = true
}

const handleUpdateClient = async () => {
  const { id, lock } = editClient.value
  const displayName = editClient.value.displayName.trim()

  if (!id || !lock || !displayName) {
    editErrorMessage.value = 'Данные клиента устарели. Пожалуйста, обновите страницу и повторите попытку.'
    return
  }

  isUpdating.value = true
  editErrorMessage.value = null

  try {
    await updateMutation.mutateAsync({
      data: {
        requestType: 'clientCard.update',
        requestId: crypto.randomUUID(),
        clientCard: {
          id,
          lock,
          displayName,
          note: editClient.value.note.trim() || undefined,
        },
      },
    })

    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['clientCards'] }),
      queryClient.invalidateQueries({ queryKey: ['clientCardsCount'] }),
      queryClient.invalidateQueries({ queryKey: ['clientCardsForPlans'] }),
    ])
    isEditOpen.value = false
  } catch (err: unknown) {
    editErrorMessage.value = err instanceof Error ? err.message : 'Произошла ошибка, попробуйте позже'
  } finally {
    isUpdating.value = false
  }
}

// Создание плана для выбранного клиента
const canSubmitClientPlan = computed(
  () =>
    newClientPlan.value.title.trim().length >= 3 &&
    newClientPlan.value.items.length > 0 &&
    !isPlanSubmitting.value,
)

const openCreatePlanForClient = () => {
  planErrorMessage.value = null
  newClientPlan.value = {
    title: '',
    items: [],
  }
  isCreatePlanOpen.value = true
}

const handleCreateClientPlan = async (status: TrainingPlanStatus = 'ACTIVE') => {
  if (!selectedClient.value?.id || !canSubmitClientPlan.value) return
  isPlanSubmitting.value = true
  planErrorMessage.value = null

  try {
    await createPlanMutation.mutateAsync({
      data: {
        requestType: 'trainingPlan.create',
        requestId: crypto.randomUUID(),
        trainingPlan: {
          title: newClientPlan.value.title.trim(),
          clientCardId: selectedClient.value.id,
          status,
          planItems: newClientPlan.value.items.map(mapDraftToPlanItem),
        },
      },
    })

    // Инвалидируем кэши планов
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['clientPlans', selectedClient.value.id] }),
      queryClient.invalidateQueries({ queryKey: ['trainingPlans'] }),
      queryClient.invalidateQueries({ queryKey: ['trainingPlansCount'] }),
    ])

    newClientPlan.value = {
      title: '',
      items: [],
    }
    isCreatePlanOpen.value = false
  } catch (err: unknown) {
    planErrorMessage.value = err instanceof Error ? err.message : 'Произошла ошибка при создании плана'
  } finally {
    isPlanSubmitting.value = false
  }
}

const handleActivateClientPlan = async (plan: TrainingPlanResponseObject) => {
  if (!plan.id || !plan.lock || isActivatingPlan.value) return
  isActivatingPlan.value = true

  try {
    const response = await activatePlanMutation.mutateAsync({
      data: {
        requestType: 'trainingPlan.activate',
        requestId: crypto.randomUUID(),
        trainingPlan: {
          id: plan.id,
          lock: plan.lock,
        },
      },
    })

    if (selectedPlanDetails.value && selectedPlanDetails.value.id === plan.id && response.data.trainingPlan) {
      selectedPlanDetails.value = response.data.trainingPlan
    }

    if (selectedClient.value?.id) {
      await queryClient.invalidateQueries({ queryKey: ['clientPlans', selectedClient.value.id] })
    }
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['trainingPlans'] }),
      queryClient.invalidateQueries({ queryKey: ['trainingPlansCount'] }),
    ])
  } catch (err: unknown) {
    alert(err instanceof Error ? err.message : 'Не удалось активировать план')
  } finally {
    isActivatingPlan.value = false
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
        <h1 class="text-2xl font-bold tracking-tight text-text-main">Клиенты</h1>
        <p class="text-sm text-text-muted">Список клиентов</p>
      </div>
      <div class="flex items-center gap-3">
        <Input
          v-model="searchQuery"
          placeholder="Поиск по имени..."
          aria-label="Поиск клиентов по имени"
          class="w-64 bg-surface"
        />
        <Button @click="isCreateOpen = true" id="add-client-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
          Добавить клиента
        </Button>
      </div>
    </div>

    <!-- Загрузка -->
    <div v-if="isLoading" class="flex items-center justify-center p-12" role="status" aria-live="polite">
      <div class="text-center space-y-3">
        <div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
        <p class="text-sm text-text-muted">Загрузка клиентов...</p>
      </div>
    </div>

    <!-- Ошибка загрузки -->
    <div v-else-if="isError" class="p-6 rounded-xl border border-danger/30 bg-danger-soft text-center space-y-3" role="alert">
      <p class="text-sm font-medium text-danger">Не удалось загрузить клиентов</p>
      <p class="text-xs text-text-muted">{{ error }}</p>
      <Button variant="outline" size="sm" @click="() => refetch()">Повторить попытку</Button>
    </div>

    <!-- Пустой список клиентов -->
    <div v-else-if="clientCards.length === 0" class="p-12 text-center border border-dashed border-border rounded-xl bg-surface/50 space-y-4">
      <div class="mx-auto w-12 h-12 rounded-full bg-surface-2 flex items-center justify-center text-text-muted">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
      </div>
      <div>
        <h3 class="font-semibold text-text-main text-lg">Клиенты не найдены</h3>
        <p class="text-sm text-text-muted mt-1 max-w-sm mx-auto">
          {{ searchQuery ? 'По вашему поисковому запросу ничего не найдено.' : 'У вас пока нет клиентов. Создайте первую карточку!' }}
        </p>
      </div>
      <Button v-if="!searchQuery" @click="isCreateOpen = true">Добавить клиента</Button>
    </div>

    <!-- Список клиентов: карточки на телефоне, таблица на desktop -->
    <template v-else>
      <section class="space-y-3 md:hidden" aria-label="Список клиентов">
        <article
          v-for="client in clientCards"
          :key="client.id"
          class="rounded-xl border border-border bg-surface p-4 shadow-sm"
        >
          <div class="flex items-start gap-3">
            <Avatar class="h-10 w-10 shrink-0 text-xs">
              <AvatarFallback>{{ (client.displayName ?? 'КЛ').substring(0, 2).toUpperCase() }}</AvatarFallback>
            </Avatar>
            <div class="min-w-0 flex-1">
              <div class="flex items-start justify-between gap-2">
                <h2 class="truncate text-base font-semibold text-text-main">{{ client.displayName ?? 'Без имени' }}</h2>
                <Badge :variant="client.status === 'ACTIVE' ? 'default' : 'secondary'" class="shrink-0">
                  {{ client.status === 'ACTIVE' ? 'Активен' : 'В архиве' }}
                </Badge>
              </div>
              <p class="mt-1 line-clamp-2 text-sm text-text-muted">{{ client.note || 'Заметок нет' }}</p>
              <p class="mt-2 text-xs text-text-faint">Создан: {{ formatDate(client.createdAt) }}</p>
            </div>
          </div>
          <div class="mt-4 grid grid-cols-2 gap-2">
            <Button variant="outline" @click="selectedClient = client" :id="`mobile-open-client-btn-${client.id}`">
              Карточка
            </Button>
            <Button variant="secondary" @click="openEditClient(client)" :aria-label="`Редактировать клиента ${client.displayName ?? 'без имени'}`">
              Редактировать
            </Button>
          </div>
        </article>
      </section>

      <div class="hidden overflow-hidden rounded-xl border border-border bg-surface shadow-xs md:block">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="border-b border-border bg-surface-2/60 text-xs font-semibold uppercase tracking-wider text-text-muted">
            <tr>
              <th scope="col" class="py-3.5 pl-6 pr-4">Клиент</th>
              <th scope="col" class="px-4 py-3.5">Статус</th>
              <th scope="col" class="px-4 py-3.5">Заметка тренера</th>
              <th scope="col" class="px-4 py-3.5">Дата создания</th>
              <th scope="col" class="py-3.5 pl-4 pr-6 text-right">Действия</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-border text-text-main">
            <tr
              v-for="client in clientCards"
              :key="client.id"
              class="hover:bg-surface-2/70 transition-colors cursor-pointer group"
              @click="selectedClient = client"
              @keydown.enter="selectedClient = client"
              @keydown.space.prevent="selectedClient = client"
              tabindex="0"
              role="button"
              :aria-label="`Открыть карточку клиента ${client.displayName ?? 'без имени'}`"
              :id="`client-row-${client.id}`"
            >
              <td class="py-4 pl-6 pr-4 font-medium">
                <div class="flex items-center gap-3">
                  <Avatar class="h-9 w-9 text-xs">
                    <AvatarFallback>{{ (client.displayName ?? 'КЛ').substring(0, 2).toUpperCase() }}</AvatarFallback>
                  </Avatar>
                  <div>
                    <span class="font-semibold text-text-main group-hover:text-primary transition-colors">
                      {{ client.displayName ?? 'Без имени' }}
                    </span>
                  </div>
                </div>
              </td>
              <td class="px-4 py-4 whitespace-nowrap">
                <Badge :variant="client.status === 'ACTIVE' ? 'default' : 'secondary'">
                  {{ client.status === 'ACTIVE' ? 'Активен' : 'В архиве' }}
                </Badge>
              </td>
              <td class="px-4 py-4 max-w-xs truncate text-xs text-text-muted">
                <span v-if="client.note" class="truncate block">
                  📝 {{ client.note }}
                </span>
                <span v-else class="text-text-faint italic">
                  —
                </span>
              </td>
              <td class="px-4 py-4 text-xs text-text-muted whitespace-nowrap">
                {{ formatDate(client.createdAt) }}
              </td>
              <td class="py-4 pl-4 pr-6 text-right whitespace-nowrap">
                <div class="flex items-center justify-end gap-2" @click.stop>
                  <Button
                    variant="outline"
                    size="sm"
                    class="h-8 text-xs group-hover:border-primary/50 group-hover:text-primary"
                    @click="selectedClient = client"
                    :id="`open-client-btn-${client.id}`"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                    Карточка
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    class="h-8 w-8 p-0 text-text-muted hover:text-text-main"
                    title="Редактировать"
                    :aria-label="`Редактировать клиента ${client.displayName ?? 'без имени'}`"
                    @click="openEditClient(client)"
                    :id="`edit-client-btn-${client.id}`"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      </div>

      <div class="rounded-xl border border-border bg-surface-2/30 px-2 md:px-4">
        <Pagination
          v-model:pageNumber="pageNumber"
          v-model:pageSize="pageSize"
          :totalSize="totalClientsCount"
        />
      </div>
    </template>

    <!-- Модальное окно добавления клиента -->
    <Dialog :open="isCreateOpen" @update:open="isCreateOpen = $event">
      <DialogContent class="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Новый клиент</DialogTitle>
          <DialogDescription>
            Заполните данные клиента
          </DialogDescription>
        </DialogHeader>

        <div v-if="errorMessage" class="p-3 bg-danger-soft text-danger text-xs rounded-md">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleCreateClient" class="space-y-4 py-2">
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="client-name">Имя клиента *</label>
            <Input
              id="client-name"
              v-model="newClient.displayName"
              placeholder="например, Алексей Смирнов"
              required
              class="bg-surface-2"
            />
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="client-note">Внутренняя заметка тренера</label>
            <Input
              id="client-note"
              v-model="newClient.note"
              placeholder="Ограничения, цели, контакты"
              class="bg-surface-2"
            />
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" @click="isCreateOpen = false">
              Отмена
            </Button>
            <Button type="submit" :disabled="!newClient.displayName.trim() || isSubmitting" id="submit-client-btn">
              {{ isSubmitting ? 'Сохранение...' : 'Добавить' }}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно просмотра деталей клиента (с планами тренировок) -->
    <Dialog :open="!!selectedClient" @update:open="(val) => { if (!val) selectedClient = null }">
      <DialogContent v-if="selectedClient" class="sm:max-w-[560px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div class="flex items-center gap-3 mb-2">
            <Avatar class="h-12 w-12 text-lg">
              <AvatarFallback>{{ (selectedClient.displayName ?? 'КЛ').substring(0, 2).toUpperCase() }}</AvatarFallback>
            </Avatar>
            <div>
              <DialogTitle>{{ selectedClient.displayName }}</DialogTitle>
              <DialogDescription>Информация о клиенте и назначенных программах</DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div class="space-y-3 py-2 text-sm">
          <div class="flex justify-between border-b border-border pb-2">
            <span class="text-text-muted">Статус:</span>
            <Badge :variant="selectedClient.status === 'ACTIVE' ? 'default' : 'secondary'">
              {{ selectedClient.status === 'ACTIVE' ? 'Активен' : 'В архиве' }}
            </Badge>
          </div>

          <div class="border-b border-border pb-2">
            <span class="text-text-muted block mb-1">Заметка тренера:</span>
            <p class="text-text-main bg-surface-2 p-2.5 rounded-md font-sans text-xs">
              {{ selectedClient.note || 'Заметок нет' }}
            </p>
          </div>

          <div class="flex justify-between text-xs text-text-faint">
            <span>Дата создания:</span>
            <span class="font-medium text-text-muted">{{ formatDate(selectedClient.createdAt) }}</span>
          </div>

          <!-- Секция назначенных тренировочных планов -->
          <div class="border-t border-border pt-4 space-y-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <h4 class="font-semibold text-sm text-text-main">Назначенные планы тренировок</h4>
                <Badge variant="outline" class="text-xs" id="client-plans-count-badge">
                  {{ isClientPlansLoading ? '...' : clientPlans.length }}
                </Badge>
              </div>
              <Button size="sm" variant="secondary" @click="openCreatePlanForClient" id="add-plan-to-client-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1.5"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
                Добавить план
              </Button>
            </div>

            <!-- Загрузка планов -->
            <div v-if="isClientPlansLoading" class="py-6 text-center text-xs text-text-muted">
              <div class="inline-block h-5 w-5 animate-spin rounded-full border-2 border-solid border-primary border-r-transparent mr-2 align-middle"></div>
              Загрузка планов...
            </div>

            <!-- Пустой список планов -->
            <div v-else-if="clientPlans.length === 0" class="p-5 text-center border border-dashed border-border rounded-lg bg-surface-2/50 space-y-2">
              <p class="text-xs text-text-muted">У этого клиента пока нет назначенных тренировочных планов</p>
              <Button size="sm" variant="outline" class="text-xs h-8" @click="openCreatePlanForClient">
                Создать первый план
              </Button>
            </div>

            <!-- Список планов клиента -->
            <div v-else class="space-y-2 max-h-56 overflow-y-auto pr-1">
              <div
                v-for="plan in clientPlans"
                :key="plan.id"
                class="p-3 rounded-lg bg-surface-2 border border-border/60 hover:border-primary/50 transition-all cursor-pointer flex items-center justify-between group"
                @click="selectedPlanDetails = plan"
                :id="`client-plan-item-${plan.id}`"
              >
                <div class="space-y-1">
                  <div class="flex items-center gap-2">
                    <span class="font-medium text-sm text-text-main group-hover:text-primary transition-colors">
                      {{ plan.title }}
                    </span>
                    <Badge :variant="plan.status === 'ACTIVE' ? 'default' : 'secondary'" class="text-[10px] px-1.5 py-0">
                      {{ formatPlanStatus(plan.status) }}
                    </Badge>
                  </div>
                  <div class="text-xs text-text-muted flex items-center gap-3">
                    <span>{{ (plan.planItems ?? []).length }} эл. ({{ formatPlanStructureSummary(plan.planItems) }})</span>
                    <span>•</span>
                    <span>{{ formatDate(plan.createdAt) }}</span>
                  </div>
                </div>
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-text-muted group-hover:text-primary transition-colors"><path d="m9 18 6-6-6-6"/></svg>
              </div>
            </div>
          </div>
        </div>

        <DialogFooter class="mt-2">
          <Button variant="outline" @click="selectedClient = null">Закрыть</Button>
          <Button @click="openEditClient(selectedClient)">Редактировать</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно создания плана для выбранного клиента -->
    <Dialog :open="isCreatePlanOpen" @update:open="isCreatePlanOpen = $event">
      <DialogContent class="sm:max-w-[550px] max-h-[90vh] overflow-y-auto z-[60]" overlay-class="z-[55]">
        <DialogHeader>
          <DialogTitle>Новый план тренировки</DialogTitle>
          <DialogDescription>
            Назначение плана для клиента: <span class="font-semibold text-text-main">{{ selectedClient?.displayName }}</span>
          </DialogDescription>
        </DialogHeader>

        <div v-if="planErrorMessage" class="p-3 bg-danger-soft text-danger text-xs rounded-md">
          {{ planErrorMessage }}
        </div>

        <form @submit.prevent="handleCreateClientPlan('ACTIVE')" class="space-y-4 py-2">
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="client-plan-name">Название плана *</label>
            <Input
              id="client-plan-name"
              v-model="newClientPlan.title"
              placeholder="например, Силовой сплит на 4 недели"
              required
              class="bg-surface-2"
            />
          </div>

          <!-- Конструктор элементов плана (Упражнения, Круговые, Суперсеты) -->
          <PlanItemBuilder v-model:items="newClientPlan.items" />

          <DialogFooter class="flex flex-col sm:flex-row sm:justify-end gap-2">
            <Button type="button" variant="outline" @click="isCreatePlanOpen = false">
              Отмена
            </Button>
            <Button type="button" variant="secondary" :disabled="!canSubmitClientPlan" @click="handleCreateClientPlan('DRAFT')" id="save-draft-client-plan-btn">
              {{ isPlanSubmitting ? 'Сохранение...' : 'Сохранить как черновик' }}
            </Button>
            <Button type="submit" :disabled="!canSubmitClientPlan" id="submit-client-plan-btn">
              {{ isPlanSubmitting ? 'Создание...' : 'Создать и активировать' }}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно просмотра деталей плана из карточки клиента -->
    <Dialog :open="!!selectedPlanDetails" @update:open="(val) => { if (!val) selectedPlanDetails = null }">
      <DialogContent v-if="selectedPlanDetails" class="sm:max-w-[550px] max-h-[90vh] overflow-y-auto z-[60]" overlay-class="z-[55]">
        <DialogHeader>
          <DialogTitle>{{ selectedPlanDetails.title }}</DialogTitle>
          <DialogDescription>
            Назначен: {{ selectedClient?.displayName }}
          </DialogDescription>
        </DialogHeader>

        <div class="space-y-3 py-2 text-sm">
          <div class="flex justify-between border-b border-border pb-2">
            <span class="text-text-muted">Статус:</span>
            <Badge :variant="selectedPlanDetails.status === 'ACTIVE' ? 'default' : 'secondary'">
              {{ formatPlanStatus(selectedPlanDetails.status) }}
            </Badge>
          </div>

          <!-- Блок с информацией о завершении -->
          <div v-if="selectedPlanDetails.completedAt" class="p-3 bg-primary-soft/30 rounded-lg border border-primary/20 space-y-1 mb-2">
            <div class="flex items-center justify-between">
              <span class="text-xs font-medium text-text-muted">Завершена:</span>
              <span class="text-xs font-semibold text-text-main">{{ formatDate(selectedPlanDetails.completedAt) }}</span>
            </div>
            <div v-if="selectedPlanDetails.difficulty" class="flex items-center justify-between">
              <span class="text-xs font-medium text-text-muted">Сложность:</span>
              <span class="text-xs font-semibold text-text-main">{{ formatDifficulty(selectedPlanDetails.difficulty) }}</span>
            </div>
            <div v-if="selectedPlanDetails.coachComment" class="pt-2 mt-2 border-t border-primary/10">
              <span class="text-xs font-medium text-text-muted block mb-1">Комментарий тренера:</span>
              <p class="text-xs text-text-main italic whitespace-pre-wrap">{{ selectedPlanDetails.coachComment }}</p>
            </div>
          </div>

          <div class="flex justify-between text-xs text-text-faint pb-1">
            <span>Дата создания:</span>
            <span class="font-medium text-text-muted">{{ formatDate(selectedPlanDetails.createdAt) }}</span>
          </div>

          <!-- Секция состава плана -->
          <div class="border-t border-border pt-3 space-y-2.5">
            <div class="flex items-center justify-between">
              <h4 class="font-semibold text-sm text-text-main">
                Состав плана ({{ (selectedPlanDetails.planItems ?? []).length }}):
              </h4>
              <span class="text-xs text-text-muted">
                {{ formatPlanStructureSummary(selectedPlanDetails.planItems) }}
              </span>
            </div>

            <div v-if="(selectedPlanDetails.planItems ?? []).length === 0" class="text-sm text-text-muted italic">
              В плане нет элементов
            </div>
            <div v-else class="space-y-2.5 max-h-60 overflow-y-auto pr-1">
              <PlanItemCard
                v-for="(item, idx) in selectedPlanDetails.planItems"
                :key="item.id || idx"
                :item="item"
                :index="idx"
              />
            </div>
          </div>
        </div>

        <DialogFooter class="flex flex-col sm:flex-row sm:justify-between items-stretch sm:items-center w-full gap-2 pt-2">
          <div>
            <PlanShareButton :plan="selectedPlanDetails" variant="outline" placement="top-left" />
          </div>
          <div class="flex items-center gap-2">
            <Button
              v-if="selectedPlanDetails.status === 'DRAFT'"
              variant="default"
              class="bg-success text-text-inverse hover:opacity-90"
              @click="handleActivateClientPlan(selectedPlanDetails)"
              :disabled="isActivatingPlan"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><polygon points="5 3 19 12 5 21 5 3"/></svg>
              Активировать
            </Button>
            <Button variant="outline" @click="selectedPlanDetails = null">Закрыть</Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно редактирования клиента -->
    <Dialog :open="isEditOpen" @update:open="isEditOpen = $event">
      <DialogContent class="sm:max-w-[425px] z-[60]" overlay-class="z-[55]">
        <DialogHeader>
          <DialogTitle>Редактирование клиента</DialogTitle>
          <DialogDescription>
            Измените данные клиента
          </DialogDescription>
        </DialogHeader>

        <div v-if="editErrorMessage" class="p-3 bg-danger-soft text-danger text-xs rounded-md">
          {{ editErrorMessage }}
        </div>

        <form @submit.prevent="handleUpdateClient" class="space-y-4 py-2">
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="edit-client-name">Имя клиента *</label>
            <Input
              id="edit-client-name"
              v-model="editClient.displayName"
              required
              maxlength="120"
              class="bg-surface-2"
            />
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-text-main" for="edit-client-note">Внутренняя заметка тренера</label>
            <Input
              id="edit-client-note"
              v-model="editClient.note"
              maxlength="1000"
              class="bg-surface-2"
            />
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" @click="isEditOpen = false">
              Отмена
            </Button>
            <Button type="submit" :disabled="!editClient.displayName.trim() || isUpdating">
              {{ isUpdating ? 'Сохранение...' : 'Сохранить' }}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  </div>
</template>
