import type { PlanItem } from '@/shared/api/generated/models/planItem'
import type { ExerciseItem } from '@/shared/api/generated/models/exerciseItem'
import type { CircuitItem } from '@/shared/api/generated/models/circuitItem'
import type { SupersetItem } from '@/shared/api/generated/models/supersetItem'

export interface ExerciseSubDraft {
  name: string
  sets: number
  reps: number
  weight?: number
  restBetweenSetsSeconds?: number
}

export type PlanItemDraft =
  | {
      itemType: 'EXERCISE'
      name: string
      sets: number
      reps: number
      weight?: number
      restBetweenSetsSeconds?: number
      description?: string
    }
  | {
      itemType: 'CIRCUIT'
      title: string
      rounds: number
      restBetweenRoundsSeconds?: number
      items: ExerciseSubDraft[]
      description?: string
    }
  | {
      itemType: 'SUPERSET'
      title: string
      restBetweenSetsSeconds?: number
      items: ExerciseSubDraft[]
      description?: string
    }

export function mapDraftToPlanItem(draft: PlanItemDraft): PlanItem {
  if (draft.itemType === 'EXERCISE') {
    const item: ExerciseItem = {
      id: crypto.randomUUID(),
      itemType: 'EXERCISE',
      title: draft.name,
      description:
        draft.description?.trim() ||
        `${draft.sets} подх. × ${draft.reps} повт.${draft.weight ? ` (${draft.weight} кг)` : ''}${
          draft.restBetweenSetsSeconds ? `, отдых ${draft.restBetweenSetsSeconds} с` : ''
        }`,
      sets: Array.from({ length: Math.max(1, draft.sets || 1) }).map(() => ({
        reps: String(draft.reps || 10),
        weight:
          draft.weight !== undefined && draft.weight !== null && Number(draft.weight) > 0
            ? String(draft.weight)
            : undefined,
        weightUnit: draft.weight ? 'KG' : undefined,
        durationSeconds: 0,
      })),
      restBetweenSetsSeconds: Math.max(0, Number(draft.restBetweenSetsSeconds) || 0),
    }
    return item
  }

  if (draft.itemType === 'CIRCUIT') {
    const item: CircuitItem = {
      id: crypto.randomUUID(),
      itemType: 'CIRCUIT',
      title: draft.title,
      description:
        draft.description?.trim() ||
        `Круговая тренировка: ${draft.rounds} раунд(ов)${
          draft.restBetweenRoundsSeconds
            ? `, отдых между раундами ${draft.restBetweenRoundsSeconds} с`
            : ''
        }`,
      rounds: Math.max(1, Number(draft.rounds) || 1),
      restBetweenRoundsSeconds: Math.max(0, Number(draft.restBetweenRoundsSeconds) || 0),
      items: draft.items.map((sub): ExerciseItem => ({
        id: crypto.randomUUID(),
        itemType: 'EXERCISE',
        title: sub.name,
        description: `${sub.sets} подх. × ${sub.reps} повт.${sub.weight ? ` (${sub.weight} кг)` : ''}`,
        sets: Array.from({ length: Math.max(1, sub.sets || 1) }).map(() => ({
          reps: String(sub.reps || 10),
          weight:
            sub.weight !== undefined && sub.weight !== null && Number(sub.weight) > 0
              ? String(sub.weight)
              : undefined,
          weightUnit: sub.weight ? 'KG' : undefined,
          durationSeconds: 0,
        })),
        restBetweenSetsSeconds: Math.max(0, Number(sub.restBetweenSetsSeconds) || 0),
      })),
    }
    return item
  }

  const item: SupersetItem = {
    id: crypto.randomUUID(),
    itemType: 'SUPERSET',
    title: draft.title,
    description:
      draft.description?.trim() ||
      `Суперсет из ${draft.items.length} упражнений${
        draft.restBetweenSetsSeconds ? `, отдых ${draft.restBetweenSetsSeconds} с` : ''
      }`,
    restBetweenSetsSeconds: Math.max(0, Number(draft.restBetweenSetsSeconds) || 0),
    items: draft.items.map((sub): ExerciseItem => ({
      id: crypto.randomUUID(),
      itemType: 'EXERCISE',
      title: sub.name,
      description: `${sub.sets} подх. × ${sub.reps} повт.${sub.weight ? ` (${sub.weight} кг)` : ''}`,
      sets: Array.from({ length: Math.max(1, sub.sets || 1) }).map(() => ({
        reps: String(sub.reps || 10),
        weight:
          sub.weight !== undefined && sub.weight !== null && Number(sub.weight) > 0
            ? String(sub.weight)
            : undefined,
        weightUnit: sub.weight ? 'KG' : undefined,
        durationSeconds: 0,
      })),
      restBetweenSetsSeconds: Math.max(0, Number(sub.restBetweenSetsSeconds) || 0),
    })),
  }
  return item
}

export function mapPlanItemToDraft(item: PlanItem): PlanItemDraft {
  const rawItem = item as unknown as Record<string, unknown>
  const itemType = item.itemType || 'EXERCISE'

  if (itemType === 'CIRCUIT') {
    const circuit = item as CircuitItem
    const subItems: ExerciseSubDraft[] = (circuit.items ?? []).map((sub) => {
      const ex = sub as ExerciseItem
      const firstSet = ex.sets?.[0]
      return {
        name: ex.title || 'Упражнение',
        sets: ex.sets && ex.sets.length > 0 ? ex.sets.length : 1,
        reps: firstSet?.reps ? parseInt(firstSet.reps, 10) || 10 : 10,
        weight: firstSet?.weight ? Number(firstSet.weight) || 0 : undefined,
        restBetweenSetsSeconds: ex.restBetweenSetsSeconds,
      }
    })
    return {
      itemType: 'CIRCUIT',
      title: circuit.title || 'Круговая тренировка',
      rounds: circuit.rounds || 1,
      restBetweenRoundsSeconds: circuit.restBetweenRoundsSeconds,
      items: subItems,
      description: circuit.description,
    }
  }

  if (itemType === 'SUPERSET') {
    const superset = item as SupersetItem
    const subItems: ExerciseSubDraft[] = (superset.items ?? []).map((sub) => {
      const ex = sub as ExerciseItem
      const firstSet = ex.sets?.[0]
      return {
        name: ex.title || 'Упражнение',
        sets: ex.sets && ex.sets.length > 0 ? ex.sets.length : 1,
        reps: firstSet?.reps ? parseInt(firstSet.reps, 10) || 10 : 10,
        weight: firstSet?.weight ? Number(firstSet.weight) || 0 : undefined,
        restBetweenSetsSeconds: ex.restBetweenSetsSeconds,
      }
    })
    return {
      itemType: 'SUPERSET',
      title: superset.title || 'Суперсет',
      restBetweenSetsSeconds: superset.restBetweenSetsSeconds,
      items: subItems,
      description: superset.description,
    }
  }

  // EXERCISE / default
  const exercise = item as ExerciseItem
  const firstSet = exercise.sets?.[0]
  return {
    itemType: 'EXERCISE',
    name: exercise.title || String(rawItem.title || 'Упражнение'),
    sets: exercise.sets && exercise.sets.length > 0 ? exercise.sets.length : 1,
    reps: firstSet?.reps ? parseInt(firstSet.reps, 10) || 10 : 10,
    weight: firstSet?.weight ? Number(firstSet.weight) || 0 : undefined,
    restBetweenSetsSeconds: exercise.restBetweenSetsSeconds,
    description: exercise.description,
  }
}

export function getItemTypeLabel(type?: string): string {
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

export function countTotalExercises(items?: PlanItem[]): number {
  if (!items || !items.length) return 0
  let count = 0
  for (const item of items) {
    const rawItem = item as unknown as Record<string, unknown>
    if (rawItem.itemType === 'CIRCUIT' || rawItem.itemType === 'SUPERSET') {
      const nested = (rawItem.items as PlanItem[]) ?? []
      count += nested.length > 0 ? countTotalExercises(nested) : 1
    } else {
      count += 1
    }
  }
  return count
}

export function formatPlanStructureSummary(items?: PlanItem[]): string {
  if (!items || !items.length) return '0 упражнений'
  let exercises = 0
  let circuits = 0
  let supersets = 0

  for (const item of items) {
    const type = item.itemType
    if (type === 'CIRCUIT') {
      circuits++
    } else if (type === 'SUPERSET') {
      supersets++
    } else {
      exercises++
    }
  }

  const parts: string[] = []
  if (exercises > 0) parts.push(`${exercises} упр.`)
  if (circuits > 0) parts.push(`${circuits} круг.`)
  if (supersets > 0) parts.push(`${supersets} суперсет.`)

  return parts.join(', ') || `${items.length} эл.`
}
