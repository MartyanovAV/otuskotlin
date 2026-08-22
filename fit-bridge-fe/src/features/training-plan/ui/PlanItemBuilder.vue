<script setup lang="ts">
import { ref } from 'vue'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Badge } from '@/shared/ui/badge'
import type { PlanItemDraft, ExerciseSubDraft } from '../model/types'

const items = defineModel<PlanItemDraft[]>('items', { default: () => [] })

// Текущий выбранный тип для добавления
const activeType = ref<'EXERCISE' | 'CIRCUIT' | 'SUPERSET'>('EXERCISE')

// Поля для одиночного упражнения
const exName = ref('')
const exSets = ref(3)
const exReps = ref(10)
const exWeight = ref(0)
const exRest = ref(60)

// Поля для круговой тренировки
const circuitTitle = ref('')
const circuitRounds = ref(3)
const circuitRest = ref(60)
const circuitSubItems = ref<ExerciseSubDraft[]>([])

// Поля для добавления упражнения внутрь круговой
const circuitSubName = ref('')
const circuitSubSets = ref(1)
const circuitSubReps = ref(12)
const circuitSubWeight = ref(0)

// Поля для суперсета
const supersetTitle = ref('')
const supersetRest = ref(90)
const supersetSubItems = ref<ExerciseSubDraft[]>([])

// Поля для добавления упражнения внутрь суперсета
const supersetSubName = ref('')
const supersetSubSets = ref(3)
const supersetSubReps = ref(10)
const supersetSubWeight = ref(0)

// Добавление одиночного упражнения в общий список плана
const addExercise = () => {
  if (!exName.value.trim()) return
  items.value.push({
    itemType: 'EXERCISE',
    name: exName.value.trim(),
    sets: Number(exSets.value) || 3,
    reps: Number(exReps.value) || 10,
    weight: Number(exWeight.value) || 0,
    restBetweenSetsSeconds: Number(exRest.value) || 0,
  })
  exName.value = ''
}

// Добавление упражнения в драфт круговой
const addSubExerciseToCircuit = () => {
  if (!circuitSubName.value.trim()) return
  circuitSubItems.value.push({
    name: circuitSubName.value.trim(),
    sets: Number(circuitSubSets.value) || 1,
    reps: Number(circuitSubReps.value) || 12,
    weight: Number(circuitSubWeight.value) || 0,
  })
  circuitSubName.value = ''
}

const removeCircuitSubItem = (index: number) => {
  circuitSubItems.value.splice(index, 1)
}

// Добавление сформированной круговой в общий список плана
const addCircuit = () => {
  if (!circuitTitle.value.trim() || circuitSubItems.value.length === 0) return
  items.value.push({
    itemType: 'CIRCUIT',
    title: circuitTitle.value.trim(),
    rounds: Math.max(1, Number(circuitRounds.value) || 1),
    restBetweenRoundsSeconds: Math.max(0, Number(circuitRest.value) || 0),
    items: [...circuitSubItems.value],
  })
  circuitTitle.value = ''
  circuitSubItems.value = []
}

// Добавление упражнения в драфт суперсета
const addSubExerciseToSuperset = () => {
  if (!supersetSubName.value.trim()) return
  supersetSubItems.value.push({
    name: supersetSubName.value.trim(),
    sets: Number(supersetSubSets.value) || 3,
    reps: Number(supersetSubReps.value) || 10,
    weight: Number(supersetSubWeight.value) || 0,
  })
  supersetSubName.value = ''
}

const removeSupersetSubItem = (index: number) => {
  supersetSubItems.value.splice(index, 1)
}

// Добавление сформированного суперсета в общий список плана
const addSuperset = () => {
  if (!supersetTitle.value.trim() || supersetSubItems.value.length < 2) return
  items.value.push({
    itemType: 'SUPERSET',
    title: supersetTitle.value.trim(),
    restBetweenSetsSeconds: Math.max(0, Number(supersetRest.value) || 0),
    items: [...supersetSubItems.value],
  })
  supersetTitle.value = ''
  supersetSubItems.value = []
}

const removeItem = (index: number) => {
  items.value.splice(index, 1)
}
</script>

<template>
  <div class="space-y-3">
    <!-- Список добавленных элементов плана -->
    <div class="space-y-2 border-t border-border pt-3">
      <div class="flex items-center justify-between">
        <label class="text-sm font-semibold text-text-main">
          Элементы плана ({{ items.length }})
        </label>
        <span class="text-xs text-text-muted">
          {{ items.length === 0 ? 'Добавьте хотя бы 1 элемент' : '' }}
        </span>
      </div>

      <div v-if="items.length > 0" class="space-y-2 max-h-48 overflow-y-auto pr-1">
        <div
          v-for="(item, index) in items"
          :key="index"
          class="flex items-center justify-between p-2.5 rounded-lg text-xs transition-colors border"
          :class="{
            'bg-surface-2 border-border/70': item.itemType === 'EXERCISE',
            'bg-amber-500/10 border-amber-500/30': item.itemType === 'CIRCUIT',
            'bg-purple-500/10 border-purple-500/30': item.itemType === 'SUPERSET',
          }"
        >
          <div class="space-y-0.5">
            <div class="flex items-center gap-1.5">
              <Badge
                class="text-[10px] px-1.5 py-0"
                :class="{
                  'bg-surface-3 text-text-main': item.itemType === 'EXERCISE',
                  'bg-amber-500/20 text-amber-700 dark:text-amber-300': item.itemType === 'CIRCUIT',
                  'bg-purple-500/20 text-purple-700 dark:text-purple-300': item.itemType === 'SUPERSET',
                }"
              >
                {{ item.itemType === 'EXERCISE' ? 'Упражнение' : item.itemType === 'CIRCUIT' ? 'Круговая' : 'Суперсет' }}
              </Badge>
              <span class="font-semibold text-text-main">
                {{ item.itemType === 'EXERCISE' ? item.name : item.title }}
              </span>
            </div>
            <div class="text-text-muted">
              <template v-if="item.itemType === 'EXERCISE'">
                {{ item.sets }} подх. × {{ item.reps }} повт.
                <span v-if="item.weight">({{ item.weight }} кг)</span>
                <span v-if="item.restBetweenSetsSeconds">, отдых {{ item.restBetweenSetsSeconds }}с</span>
              </template>
              <template v-else-if="item.itemType === 'CIRCUIT'">
                {{ item.rounds }} раунд(ов) • {{ item.items.length }} упр. • отдых {{ item.restBetweenRoundsSeconds || 0 }}с
              </template>
              <template v-else-if="item.itemType === 'SUPERSET'">
                {{ item.items.length }} упр. без отдыха • отдых {{ item.restBetweenSetsSeconds || 0 }}с
              </template>
            </div>
          </div>
          <button
            type="button"
            @click="removeItem(index)"
            class="text-danger hover:text-danger/80 p-1 font-bold text-sm"
            title="Удалить"
          >
            ✕
          </button>
        </div>
      </div>
    </div>

    <!-- Переключатель типов добавляемых элементов -->
    <div class="bg-surface-3 p-3 rounded-xl space-y-3 border border-border">
      <div class="flex items-center justify-between gap-2">
        <span class="text-xs font-semibold text-text-main">Добавить в программу:</span>
        <div class="flex p-0.5 bg-surface-2 rounded-lg border border-border text-xs">
          <button
            type="button"
            @click="activeType = 'EXERCISE'"
            class="px-2.5 py-1 rounded-md transition-colors text-xs font-medium"
            :class="activeType === 'EXERCISE' ? 'bg-primary text-primary-inverse shadow-xs' : 'text-text-muted hover:text-text-main'"
            id="tab-exercise-btn"
          >
            Упражнение
          </button>
          <button
            type="button"
            @click="activeType = 'CIRCUIT'"
            class="px-2.5 py-1 rounded-md transition-colors text-xs font-medium"
            :class="activeType === 'CIRCUIT' ? 'bg-amber-500 text-white shadow-xs' : 'text-text-muted hover:text-text-main'"
            id="tab-circuit-btn"
          >
            Круговая
          </button>
          <button
            type="button"
            @click="activeType = 'SUPERSET'"
            class="px-2.5 py-1 rounded-md transition-colors text-xs font-medium"
            :class="activeType === 'SUPERSET' ? 'bg-purple-600 text-white shadow-xs' : 'text-text-muted hover:text-text-main'"
            id="tab-superset-btn"
          >
            Суперсет
          </button>
        </div>
      </div>

      <!-- ФОРМА: Одиночное упражнение -->
      <div v-if="activeType === 'EXERCISE'" class="space-y-2">
        <Input
          v-model="exName"
          placeholder="Название упражнения (например, Жим лежа)..."
          class="bg-surface h-8 text-xs"
          id="exercise-name-input"
          @keydown.enter.prevent="addExercise"
        />
        <div class="flex flex-wrap items-center gap-2">
          <div class="flex items-center gap-1">
            <label for="ex-sets" class="text-xs text-text-muted">Подх:</label>
            <Input id="ex-sets" type="number" v-model="exSets" min="1" class="w-14 bg-surface h-7 text-xs" />
          </div>
          <div class="flex items-center gap-1">
            <label for="ex-reps" class="text-xs text-text-muted">Повт:</label>
            <Input id="ex-reps" type="number" v-model="exReps" min="1" class="w-14 bg-surface h-7 text-xs" />
          </div>
          <div class="flex items-center gap-1">
            <label for="ex-weight" class="text-xs text-text-muted">Вес(кг):</label>
            <Input id="ex-weight" type="number" v-model="exWeight" min="0" class="w-14 bg-surface h-7 text-xs" />
          </div>
          <div class="flex items-center gap-1">
            <label for="ex-rest" class="text-xs text-text-muted">Отдых(с):</label>
            <Input id="ex-rest" type="number" v-model="exRest" min="0" class="w-14 bg-surface h-7 text-xs" />
          </div>
          <Button
            type="button"
            variant="default"
            size="sm"
            class="h-7 text-xs ml-auto"
            :disabled="!exName.trim()"
            @click="addExercise"
            id="add-exercise-btn"
          >
            + Добавить
          </Button>
        </div>
      </div>

      <!-- ФОРМА: Круговая тренировка -->
      <div v-else-if="activeType === 'CIRCUIT'" class="space-y-2.5">
        <Input
          v-model="circuitTitle"
          placeholder="Название круга (например, Круговая на выносливость)..."
          class="bg-surface h-8 text-xs"
          id="circuit-title-input"
        />
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-1.5">
            <label for="circuit-rounds" class="text-xs text-text-muted">Раундов:</label>
            <Input id="circuit-rounds" type="number" v-model="circuitRounds" min="1" class="w-14 bg-surface h-7 text-xs" />
          </div>
          <div class="flex items-center gap-1.5">
            <label for="circuit-rest" class="text-xs text-text-muted">Отдых между кругами (сек):</label>
            <Input id="circuit-rest" type="number" v-model="circuitRest" min="0" class="w-16 bg-surface h-7 text-xs" />
          </div>
        </div>

        <!-- Список добавленных в круг упражнений -->
        <div class="border border-amber-500/30 rounded-lg p-2 bg-amber-500/5 space-y-1.5">
          <div class="flex items-center justify-between text-[11px] text-amber-700 dark:text-amber-300 font-medium">
            <span>Упражнения круга ({{ circuitSubItems.length }}):</span>
            <span v-if="circuitSubItems.length === 0" class="text-amber-600/80">требуется минимум 1</span>
          </div>

          <div v-if="circuitSubItems.length > 0" class="space-y-1 max-h-28 overflow-y-auto">
            <div
              v-for="(sub, sIdx) in circuitSubItems"
              :key="sIdx"
              class="flex items-center justify-between bg-surface px-2 py-1 rounded text-xs"
            >
              <span>{{ sIdx + 1 }}. {{ sub.name }} ({{ sub.reps }} повт.<span v-if="sub.weight">, {{ sub.weight }} кг</span>)</span>
              <button type="button" @click="removeCircuitSubItem(sIdx)" class="text-danger p-0.5 hover:text-danger/80">✕</button>
            </div>
          </div>

          <!-- Строка добавления упражнения в круг -->
          <div class="flex flex-wrap items-center gap-1.5 pt-1">
            <Input
              v-model="circuitSubName"
              placeholder="Упражнение в круг..."
              class="flex-1 min-w-[120px] bg-surface h-7 text-xs"
              id="circuit-sub-name-input"
              @keydown.enter.prevent="addSubExerciseToCircuit"
            />
            <Input type="number" v-model="circuitSubReps" min="1" placeholder="Повт" title="Повторения" class="w-12 bg-surface h-7 text-xs" />
            <Input type="number" v-model="circuitSubWeight" min="0" placeholder="Кг" title="Вес (кг)" class="w-12 bg-surface h-7 text-xs" />
            <Button
              type="button"
              variant="secondary"
              size="sm"
              class="h-7 text-xs"
              :disabled="!circuitSubName.trim()"
              @click="addSubExerciseToCircuit"
              id="add-circuit-sub-btn"
            >
              +
            </Button>
          </div>
        </div>

        <Button
          type="button"
          class="w-full h-8 text-xs bg-amber-600 hover:bg-amber-700 text-white font-medium"
          :disabled="!circuitTitle.trim() || circuitSubItems.length === 0"
          @click="addCircuit"
          id="add-circuit-btn"
        >
          + Добавить круговую в план
        </Button>
      </div>

      <!-- ФОРМА: Суперсет -->
      <div v-else-if="activeType === 'SUPERSET'" class="space-y-2.5">
        <Input
          v-model="supersetTitle"
          placeholder="Название суперсета (например, Бицепс + Трицепс)..."
          class="bg-surface h-8 text-xs"
          id="superset-title-input"
        />
        <div class="flex items-center gap-1.5">
          <label for="superset-rest" class="text-xs text-text-muted">Отдых после суперсета (сек):</label>
          <Input id="superset-rest" type="number" v-model="supersetRest" min="0" class="w-16 bg-surface h-7 text-xs" />
        </div>

        <!-- Список упражнений суперсета -->
        <div class="border border-purple-500/30 rounded-lg p-2 bg-purple-500/5 space-y-1.5">
          <div class="flex items-center justify-between text-[11px] text-purple-700 dark:text-purple-300 font-medium">
            <span>Упражнения суперсета ({{ supersetSubItems.length }}/мин. 2):</span>
            <span v-if="supersetSubItems.length < 2" class="text-purple-600/80">добавьте минимум 2</span>
          </div>

          <div v-if="supersetSubItems.length > 0" class="space-y-1 max-h-28 overflow-y-auto">
            <div
              v-for="(sub, sIdx) in supersetSubItems"
              :key="sIdx"
              class="flex items-center justify-between bg-surface px-2 py-1 rounded text-xs"
            >
              <span>{{ String.fromCharCode(65 + sIdx) }}. {{ sub.name }} ({{ sub.sets }}×{{ sub.reps }}<span v-if="sub.weight">, {{ sub.weight }} кг</span>)</span>
              <button type="button" @click="removeSupersetSubItem(sIdx)" class="text-danger p-0.5 hover:text-danger/80">✕</button>
            </div>
          </div>

          <!-- Строка добавления упражнения в суперсет -->
          <div class="flex flex-wrap items-center gap-1.5 pt-1">
            <Input
              v-model="supersetSubName"
              placeholder="Упражнение суперсета..."
              class="flex-1 min-w-[120px] bg-surface h-7 text-xs"
              id="superset-sub-name-input"
              @keydown.enter.prevent="addSubExerciseToSuperset"
            />
            <Input type="number" v-model="supersetSubSets" min="1" placeholder="Подх" title="Подходы" class="w-12 bg-surface h-7 text-xs" />
            <Input type="number" v-model="supersetSubReps" min="1" placeholder="Повт" title="Повторения" class="w-12 bg-surface h-7 text-xs" />
            <Input type="number" v-model="supersetSubWeight" min="0" placeholder="Кг" title="Вес (кг)" class="w-12 bg-surface h-7 text-xs" />
            <Button
              type="button"
              variant="secondary"
              size="sm"
              class="h-7 text-xs"
              :disabled="!supersetSubName.trim()"
              @click="addSubExerciseToSuperset"
              id="add-superset-sub-btn"
            >
              +
            </Button>
          </div>
        </div>

        <Button
          type="button"
          class="w-full h-8 text-xs bg-purple-600 hover:bg-purple-700 text-white font-medium"
          :disabled="!supersetTitle.trim() || supersetSubItems.length < 2"
          @click="addSuperset"
          id="add-superset-btn"
        >
          + Добавить суперсет в план
        </Button>
      </div>
    </div>
  </div>
</template>
