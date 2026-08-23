<script setup lang="ts">
import { computed } from 'vue'
import { Badge } from '@/shared/ui/badge'
import type { PlanItem } from '@/shared/api/generated/models/planItem'
import type { CircuitItem } from '@/shared/api/generated/models/circuitItem'
import type { SupersetItem } from '@/shared/api/generated/models/supersetItem'
import type { ExerciseItem } from '@/shared/api/generated/models/exerciseItem'
import { getItemTypeLabel } from '../model/types'

const props = defineProps<{
  item: PlanItem
  index?: number
}>()

const isCircuit = computed(() => props.item.itemType === 'CIRCUIT')
const isSuperset = computed(() => props.item.itemType === 'SUPERSET')
const isExercise = computed(() => !isCircuit.value && !isSuperset.value)

const circuitData = computed<CircuitItem>(() => props.item as CircuitItem)
const supersetData = computed<SupersetItem>(() => props.item as SupersetItem)
const exerciseData = computed<ExerciseItem>(() => props.item as ExerciseItem)

const subItems = computed<PlanItem[]>(() => {
  if (isCircuit.value) return circuitData.value.items ?? []
  if (isSuperset.value) return supersetData.value.items ?? []
  return []
})
</script>

<template>
  <!-- Одиночное упражнение -->
  <div
    v-if="isExercise"
    class="flex items-start justify-between bg-surface-2 p-3.5 rounded-xl border border-border/60 hover:border-border transition-colors gap-3"
  >
    <div class="flex items-start gap-3">
      <div class="w-7 h-7 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">
        {{ index !== undefined ? index + 1 : '🏋️' }}
      </div>
      <div>
        <h4 class="font-medium text-sm text-text-main leading-snug">
          {{ item.title || 'Упражнение' }}
        </h4>
        <p v-if="item.description" class="text-xs text-text-muted mt-1 leading-relaxed">
          {{ item.description }}
        </p>
        <div v-if="exerciseData.restBetweenSetsSeconds" class="text-[11px] text-text-muted/80 mt-1 flex items-center gap-1">
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          Отдых: {{ exerciseData.restBetweenSetsSeconds }} с
        </div>
      </div>
    </div>
    <Badge variant="outline" class="text-[11px] shrink-0 font-medium text-text-muted border-border">
      {{ getItemTypeLabel(item.itemType) }}
    </Badge>
  </div>

  <!-- Круговая тренировка -->
  <div
    v-else-if="isCircuit"
    class="bg-warning-soft rounded-xl border border-warning/30 p-3.5 space-y-3"
  >
    <div class="flex items-start justify-between gap-2">
      <div class="flex items-start gap-2.5">
        <div class="w-7 h-7 rounded-lg bg-warning-soft text-text-main flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">
          🔄
        </div>
        <div>
          <div class="flex items-center gap-2 flex-wrap">
            <h4 class="font-semibold text-sm text-text-main">
              {{ item.title || 'Круговая тренировка' }}
            </h4>
            <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-warning-soft text-text-main">
              {{ circuitData.rounds ?? 1 }} {{ (circuitData.rounds ?? 1) === 1 ? 'раунд' : (circuitData.rounds ?? 1) < 5 ? 'раунда' : 'раундов' }}
            </span>
          </div>
          <p v-if="item.description" class="text-xs text-text-muted mt-1">
            {{ item.description }}
          </p>
          <div v-if="circuitData.restBetweenRoundsSeconds" class="text-[11px] text-text-muted mt-1 flex items-center gap-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Отдых между кругами: {{ circuitData.restBetweenRoundsSeconds }} с
          </div>
        </div>
      </div>
      <Badge class="text-[11px] shrink-0 bg-warning-soft text-text-main border-warning/30">
        Круговая
      </Badge>
    </div>

    <!-- Вложенные упражнения круга -->
    <div class="space-y-1.5 pl-9">
      <div
        v-for="(sub, sIdx) in subItems"
        :key="sub.id || sIdx"
        class="flex items-center justify-between bg-surface/80 px-3 py-2 rounded-lg text-xs border border-border/50"
      >
        <div class="flex items-center gap-2">
          <span class="w-4 h-4 rounded-full bg-surface-3 text-[10px] flex items-center justify-center font-medium text-text-muted">
            {{ sIdx + 1 }}
          </span>
          <span class="font-medium text-text-main">{{ sub.title }}</span>
        </div>
        <span v-if="sub.description" class="text-text-muted text-[11px]">
          {{ sub.description }}
        </span>
      </div>
    </div>
  </div>

  <!-- Суперсет -->
  <div
    v-else-if="isSuperset"
    class="bg-primary-soft rounded-xl border border-primary/30 p-3.5 space-y-3"
  >
    <div class="flex items-start justify-between gap-2">
      <div class="flex items-start gap-2.5">
        <div class="w-7 h-7 rounded-lg bg-primary-soft text-primary-soft-text flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">
          ⚡
        </div>
        <div>
          <div class="flex items-center gap-2 flex-wrap">
            <h4 class="font-semibold text-sm text-text-main">
              {{ item.title || 'Суперсет' }}
            </h4>
            <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-primary-soft text-primary-soft-text">
              {{ subItems.length }} упражнения без отдыха
            </span>
          </div>
          <p v-if="item.description" class="text-xs text-text-muted mt-1">
            {{ item.description }}
          </p>
          <div v-if="supersetData.restBetweenSetsSeconds" class="text-[11px] text-primary-soft-text mt-1 flex items-center gap-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Отдых после суперсета: {{ supersetData.restBetweenSetsSeconds }} с
          </div>
        </div>
      </div>
      <Badge class="text-[11px] shrink-0 bg-primary-soft text-primary-soft-text border-primary/30">
        Суперсет
      </Badge>
    </div>

    <!-- Вложенные упражнения суперсета -->
    <div class="space-y-1.5 pl-9 relative">
      <div class="absolute left-4 top-2 bottom-2 w-0.5 bg-primary/20 rounded"></div>
      <div
        v-for="(sub, sIdx) in subItems"
        :key="sub.id || sIdx"
        class="flex items-center justify-between bg-surface/80 px-3 py-2 rounded-lg text-xs border border-border/50 relative z-10"
      >
        <div class="flex items-center gap-2">
          <span class="w-4 h-4 rounded-full bg-primary-soft text-primary-soft-text text-[10px] flex items-center justify-center font-bold">
            {{ String.fromCharCode(65 + sIdx) }}
          </span>
          <span class="font-medium text-text-main">{{ sub.title }}</span>
        </div>
        <span v-if="sub.description" class="text-text-muted text-[11px]">
          {{ sub.description }}
        </span>
      </div>
    </div>
  </div>
</template>
