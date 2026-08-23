import { describe, it, expect } from 'vitest'
import { mapDraftToPlanItem, mapPlanItemToDraft, type PlanItemDraft } from '../features/training-plan/model/types'
import type { ExerciseItem } from '../shared/api/generated/models/exerciseItem'
import type { CircuitItem } from '../shared/api/generated/models/circuitItem'
import type { SupersetItem } from '../shared/api/generated/models/supersetItem'

describe('mapPlanItemToDraft and mapDraftToPlanItem bidirectional mapping', () => {
  it('maps single exercise correctly to draft and back', () => {
    const exercise: ExerciseItem = {
      id: 'ex-1',
      itemType: 'EXERCISE',
      title: 'Жим лежа',
      description: '3 подх. × 10 повт. (80 кг), отдых 60 с',
      sets: [
        { reps: '10', weight: '80', weightUnit: 'KG', durationSeconds: 0 },
        { reps: '10', weight: '80', weightUnit: 'KG', durationSeconds: 0 },
        { reps: '10', weight: '80', weightUnit: 'KG', durationSeconds: 0 },
      ],
      restBetweenSetsSeconds: 60,
    }

    const draft = mapPlanItemToDraft(exercise)

    expect(draft.itemType).toBe('EXERCISE')
    const exerciseDraft = draft as Extract<PlanItemDraft, { itemType: 'EXERCISE' }>
    expect(exerciseDraft.name).toBe('Жим лежа')
    expect(exerciseDraft.sets).toBe(3)
    expect(exerciseDraft.reps).toBe(10)
    expect(exerciseDraft.weight).toBe(80)
    expect(exerciseDraft.restBetweenSetsSeconds).toBe(60)
    expect(exerciseDraft.description).toBe('3 подх. × 10 повт. (80 кг), отдых 60 с')

    const mappedBack = mapDraftToPlanItem(draft) as ExerciseItem
    expect(mappedBack.title).toBe('Жим лежа')
    expect(mappedBack.sets?.length).toBe(3)
    expect(mappedBack.sets?.[0]?.reps).toBe('10')
    expect(mappedBack.sets?.[0]?.weight).toBe('80')
    expect(mappedBack.restBetweenSetsSeconds).toBe(60)
  })

  it('maps circuit item correctly to draft and back', () => {
    const circuit: CircuitItem = {
      id: 'circ-1',
      itemType: 'CIRCUIT',
      title: 'Круговая кардио',
      rounds: 4,
      restBetweenRoundsSeconds: 90,
      description: 'Круговая тренировка: 4 раунд(ов), отдых между раундами 90 с',
      items: [
        {
          id: 'sub-1',
          itemType: 'EXERCISE',
          title: 'Берпи',
          sets: [{ reps: '15', durationSeconds: 0 }],
        } as ExerciseItem,
        {
          id: 'sub-2',
          itemType: 'EXERCISE',
          title: 'Махи гирей',
          sets: [{ reps: '20', weight: '16', weightUnit: 'KG', durationSeconds: 0 }],
        } as ExerciseItem,
      ],
    }

    const draft = mapPlanItemToDraft(circuit)

    expect(draft.itemType).toBe('CIRCUIT')
    const circuitDraft = draft as Extract<PlanItemDraft, { itemType: 'CIRCUIT' }>
    expect(circuitDraft.title).toBe('Круговая кардио')
    expect(circuitDraft.rounds).toBe(4)
    expect(circuitDraft.restBetweenRoundsSeconds).toBe(90)
    expect(circuitDraft.items.length).toBe(2)
    expect(circuitDraft.items[0]?.name).toBe('Берпи')
    expect(circuitDraft.items[0]?.reps).toBe(15)
    expect(circuitDraft.items[1]?.name).toBe('Махи гирей')
    expect(circuitDraft.items[1]?.weight).toBe(16)

    const mappedBack = mapDraftToPlanItem(draft) as CircuitItem
    expect(mappedBack.title).toBe('Круговая кардио')
    expect(mappedBack.rounds).toBe(4)
    expect(mappedBack.restBetweenRoundsSeconds).toBe(90)
    expect(mappedBack.items?.length).toBe(2)
  })

  it('maps superset item correctly to draft and back', () => {
    const superset: SupersetItem = {
      id: 'super-1',
      itemType: 'SUPERSET',
      title: 'Руки: бицепс + трицепс',
      restBetweenSetsSeconds: 75,
      description: 'Суперсет из 2 упражнений, отдых 75 с',
      items: [
        {
          id: 'sub-1',
          itemType: 'EXERCISE',
          title: 'Подъем штанги на бицепс',
          sets: [
            { reps: '12', weight: '30', durationSeconds: 0 },
            { reps: '12', weight: '30', durationSeconds: 0 },
            { reps: '12', weight: '30', durationSeconds: 0 },
          ],
        } as ExerciseItem,
        {
          id: 'sub-2',
          itemType: 'EXERCISE',
          title: 'Французский жим',
          sets: [
            { reps: '12', weight: '25', durationSeconds: 0 },
            { reps: '12', weight: '25', durationSeconds: 0 },
            { reps: '12', weight: '25', durationSeconds: 0 },
          ],
        } as ExerciseItem,
      ],
    }

    const draft = mapPlanItemToDraft(superset)

    expect(draft.itemType).toBe('SUPERSET')
    const supersetDraft = draft as Extract<PlanItemDraft, { itemType: 'SUPERSET' }>
    expect(supersetDraft.title).toBe('Руки: бицепс + трицепс')
    expect(supersetDraft.restBetweenSetsSeconds).toBe(75)
    expect(supersetDraft.items.length).toBe(2)
    expect(supersetDraft.items[0]?.name).toBe('Подъем штанги на бицепс')
    expect(supersetDraft.items[0]?.sets).toBe(3)
    expect(supersetDraft.items[0]?.reps).toBe(12)
    expect(supersetDraft.items[0]?.weight).toBe(30)
    expect(supersetDraft.items[1]?.name).toBe('Французский жим')
    expect(supersetDraft.items[1]?.weight).toBe(25)

    const mappedBack = mapDraftToPlanItem(draft) as SupersetItem
    expect(mappedBack.title).toBe('Руки: бицепс + трицепс')
    expect(mappedBack.restBetweenSetsSeconds).toBe(75)
    expect(mappedBack.items?.length).toBe(2)
  })
})
