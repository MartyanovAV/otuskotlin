<script setup lang="ts">
import { computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { Card, CardHeader, CardTitle, CardContent } from '@/shared/ui/card'
import { clientCardSearch } from '@/shared/api/generated/client-card/client-card'
import { trainingPlanSearch } from '@/shared/api/generated/training-plan/training-plan'
import type { ClientCardResponseObject } from '@/shared/api/generated/models/clientCardResponseObject'

const { data: clientsData, isLoading: isClientsLoading } = useQuery({
  queryKey: ['clientCardsCount'],
  queryFn: () =>
    clientCardSearch({
      requestType: 'clientCard.search',
      requestId: crypto.randomUUID(),
      clientCardFilter: { pageSize: 100, pageNumber: 1 },
    }),
})

const { data: plansData, isLoading: isPlansLoading } = useQuery({
  queryKey: ['trainingPlansCount'],
  queryFn: () =>
    trainingPlanSearch({
      requestType: 'trainingPlan.search',
      requestId: crypto.randomUUID(),
      trainingPlanFilter: { pageSize: 100, pageNumber: 1 },
    }),
})

const totalClients = computed(() => {
  return clientsData.value?.data?.clientCards?.length ?? 0
})

const totalPlans = computed(() => {
  return plansData.value?.data?.trainingPlans?.length ?? 0
})

const recentClients = computed<ClientCardResponseObject[]>(() => {
  const cards = clientsData.value?.data?.clientCards ?? []
  return cards.slice(0, 3)
})
</script>

<template>
  <div class="space-y-6">
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Всего клиентов в БД</CardTitle>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-text-muted"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">
            <span v-if="isClientsLoading" class="animate-pulse text-text-muted">...</span>
            <span v-else>{{ totalClients }}</span>
          </div>
          <p class="text-xs text-text-muted">Реальные карточки из PostgreSQL</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Тренировочные планы</CardTitle>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-text-muted"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"/></svg>
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">
            <span v-if="isPlansLoading" class="animate-pulse text-text-muted">...</span>
            <span v-else>{{ totalPlans }}</span>
          </div>
          <p class="text-xs text-text-muted">Активные программы в Training Service</p>
        </CardContent>
      </Card>
    </div>

    <!-- Список последних клиентов -->
    <Card class="col-span-4">
      <CardHeader>
        <CardTitle>Недавние клиенты тренера</CardTitle>
      </CardHeader>
      <CardContent>
        <div v-if="isClientsLoading" class="text-sm text-text-muted">Загрузка данных из БД...</div>
        <div v-else-if="recentClients.length === 0" class="text-sm text-text-muted">
          Клиенты ещё не добавлены. Перейдите в раздел «Клиенты», чтобы создать первую карточку.
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="c in recentClients"
            :key="c.id"
            class="flex items-center justify-between p-3 rounded-lg bg-surface-2"
          >
            <div>
              <p class="font-medium text-sm text-text-main">{{ c.displayName }}</p>
              <p class="text-xs text-text-muted mt-0.5">{{ c.note || 'Без заметок' }}</p>
            </div>
            <span class="text-xs text-text-faint font-mono">{{ (c.id ?? '').substring(0, 8) }}...</span>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
