<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { Card, CardContent } from '@/shared/ui/card'
import { Avatar, AvatarFallback } from '@/shared/ui/avatar'
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
import { clientCardSearch, useClientCardCreate } from '@/shared/api/generated/client-card/client-card'
import type { ClientCardResponseObject } from '@/shared/api/generated/models/clientCardResponseObject'

const queryClient = useQueryClient()
const searchQuery = ref('')
const isCreateOpen = ref(false)
const selectedClient = ref<ClientCardResponseObject | null>(null)
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

const newClient = ref({
  displayName: '',
  note: '',
})

// Реальный запрос к бэкенду через TanStack Query
const { data: searchResponse, isLoading, isError, error, refetch } = useQuery({
  queryKey: ['clientCards', searchQuery],
  queryFn: () =>
    clientCardSearch({
      requestType: 'clientCard.search',
      requestId: crypto.randomUUID(),
      clientCardFilter: {
        searchString: searchQuery.value.trim() || undefined,
        pageSize: 50,
        pageNumber: 1,
      },
    }),
})

const clientCards = computed<ClientCardResponseObject[]>(() => {
  return searchResponse.value?.data?.clientCards ?? []
})

const createMutation = useClientCardCreate()

const handleCreateClient = async () => {
  if (!newClient.value.displayName.trim()) return
  isSubmitting.value = true
  errorMessage.value = null

  try {
    const res = await createMutation.mutateAsync({
      data: {
        requestType: 'clientCard.create',
        requestId: crypto.randomUUID(),
        clientCard: {
          displayName: newClient.value.displayName.trim(),
          note: newClient.value.note.trim() || undefined,
        },
      },
    })

    if (res.data?.result === 'error') {
      errorMessage.value = res.data?.errors?.[0]?.message ?? 'Ошибка при создании карточки клиента'
      return
    }

    // Инвалидируем кэш TanStack Query — бэкенд перезапросит свежий список из базы
    await queryClient.invalidateQueries({ queryKey: ['clientCards'] })
    await queryClient.invalidateQueries({ queryKey: ['clientCardsCount'] })

    // Сброс формы и закрытие диалога
    newClient.value = {
      displayName: '',
      note: '',
    }
    isCreateOpen.value = false
  } catch (err: unknown) {
    errorMessage.value = err instanceof Error ? err.message : 'Не удалось связаться с сервером'
  } finally {
    isSubmitting.value = false
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
        <h2 class="text-2xl font-bold tracking-tight text-text-main">Клиенты</h2>
        <p class="text-sm text-text-muted">Клиентские карточки из базы данных Training Service</p>
      </div>
      <div class="flex items-center gap-3">
        <Input
          v-model="searchQuery"
          placeholder="Поиск по имени..."
          class="w-64 bg-surface"
        />
        <Button @click="isCreateOpen = true" id="add-client-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
          Добавить клиента
        </Button>
      </div>
    </div>

    <!-- Загрузка -->
    <div v-if="isLoading" class="flex items-center justify-center p-12">
      <div class="text-center space-y-3">
        <div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary border-r-transparent"></div>
        <p class="text-sm text-text-muted">Загрузка карточек клиентов из базы данных...</p>
      </div>
    </div>

    <!-- Ошибка загрузки -->
    <div v-else-if="isError" class="p-6 rounded-xl border border-danger/30 bg-danger-soft text-center space-y-3">
      <p class="text-sm font-medium text-danger">Не удалось загрузить данные из сервиса</p>
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
          {{ searchQuery ? 'По вашему поисковому запросу ничего не найдено.' : 'В вашей базе пока нет карточек клиентов. Создайте первую!' }}
        </p>
      </div>
      <Button v-if="!searchQuery" @click="isCreateOpen = true">Создать первую карточку</Button>
    </div>

    <!-- Сетка клиентов из базы данных -->
    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <Card
        v-for="client in clientCards"
        :key="client.id"
        class="cursor-pointer transition-all hover:border-primary/50 hover:shadow-md"
        @click="selectedClient = client"
      >
        <CardContent class="p-6">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-4">
              <Avatar>
                <AvatarFallback>{{ (client.displayName ?? 'КЛ').substring(0, 2).toUpperCase() }}</AvatarFallback>
              </Avatar>
              <div>
                <h3 class="font-semibold leading-none text-text-main">{{ client.displayName ?? 'Без имени' }}</h3>
                <p class="text-xs text-text-faint mt-1 font-mono">ID: {{ (client.id ?? '').substring(0, 8) }}...</p>
              </div>
            </div>
            <Badge :variant="client.status === 'ACTIVE' ? 'default' : 'secondary'">
              {{ client.status === 'ACTIVE' ? 'Активен' : 'В архиве' }}
            </Badge>
          </div>

          <div v-if="client.note" class="mt-3 text-xs text-text-muted line-clamp-2 bg-surface-2 p-2.5 rounded-md">
            📝 {{ client.note }}
          </div>

          <div class="mt-4 text-xs text-text-faint flex items-center justify-between border-t border-border pt-3">
            <span>Создан:</span>
            <span class="font-medium text-text-muted">{{ formatDate(client.createdAt) }}</span>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Модальное окно добавления клиента -->
    <Dialog :open="isCreateOpen" @update:open="isCreateOpen = $event">
      <DialogContent class="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Новый клиент</DialogTitle>
          <DialogDescription>
            Заполните данные для создания карточки в базе данных бэкенда.
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
              {{ isSubmitting ? 'Сохранение в базу...' : 'Создать в базе' }}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- Модальное окно просмотра деталей клиента из БД -->
    <Dialog :open="!!selectedClient" @update:open="(val) => { if (!val) selectedClient = null }">
      <DialogContent v-if="selectedClient" class="sm:max-w-[460px]">
        <DialogHeader>
          <div class="flex items-center gap-3 mb-2">
            <Avatar class="h-12 w-12 text-lg">
              <AvatarFallback>{{ (selectedClient.displayName ?? 'КЛ').substring(0, 2).toUpperCase() }}</AvatarFallback>
            </Avatar>
            <div>
              <DialogTitle>{{ selectedClient.displayName }}</DialogTitle>
              <DialogDescription class="font-mono text-xs">{{ selectedClient.id }}</DialogDescription>
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

          <div class="flex justify-between text-xs text-text-faint">
            <span>Optimistic Lock:</span>
            <span class="font-mono text-xs">{{ (selectedClient.lock ?? '—').substring(0, 8) }}...</span>
          </div>
        </div>

        <DialogFooter>
          <Button @click="selectedClient = null">Закрыть</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
