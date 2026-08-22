import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PlanItemCard from '../features/training-plan/ui/PlanItemCard.vue'
import PlanItemBuilder from '../features/training-plan/ui/PlanItemBuilder.vue'
import {
  mapDraftToPlanItem,
  countTotalExercises,
  formatPlanStructureSummary,
  getItemTypeLabel,
  type PlanItemDraft,
} from '../features/training-plan/model/types'
import type { ExerciseItem } from '../shared/api/generated/models/exerciseItem'
import type { CircuitItem } from '../shared/api/generated/models/circuitItem'
import type { SupersetItem } from '../shared/api/generated/models/supersetItem'

describe('Plan Item Model & Helpers', () => {
  it('maps EXERCISE draft to valid ExerciseItem payload', () => {
    const draft: PlanItemDraft = {
      itemType: 'EXERCISE',
      name: 'Приседания со штангой',
      sets: 4,
      reps: 8,
      weight: 100,
      restBetweenSetsSeconds: 90,
    }

    const item = mapDraftToPlanItem(draft) as ExerciseItem
    expect(item.itemType).toBe('EXERCISE')
    expect(item.title).toBe('Приседания со штангой')
    expect(item.description).toContain('4 подх. × 8 повт. (100 кг)')
    expect(item.description).toContain('отдых 90 с')
    expect(item.sets?.length).toBe(4)
    expect(item.sets?.[0]?.reps).toBe('8')
    expect(item.sets?.[0]?.weight).toBe('100')
    expect(item.sets?.[0]?.weightUnit).toBe('KG')
    expect(item.restBetweenSetsSeconds).toBe(90)
    expect(item.id).toBeDefined()
  })

  it('maps CIRCUIT draft to valid CircuitItem with nested ExerciseItems', () => {
    const draft: PlanItemDraft = {
      itemType: 'CIRCUIT',
      title: 'Круговая на пресс',
      rounds: 4,
      restBetweenRoundsSeconds: 60,
      items: [
        { name: 'Скручивания', sets: 1, reps: 20, weight: 0 },
        { name: 'Планка', sets: 1, reps: 60, weight: 0 },
      ],
    }

    const item = mapDraftToPlanItem(draft) as CircuitItem
    expect(item.itemType).toBe('CIRCUIT')
    expect(item.title).toBe('Круговая на пресс')
    expect(item.rounds).toBe(4)
    expect(item.restBetweenRoundsSeconds).toBe(60)
    expect(item.items?.length).toBe(2)
    expect(item.items?.[0]?.title).toBe('Скручивания')
    expect(item.items?.[0]?.itemType).toBe('EXERCISE')
    expect(item.items?.[1]?.title).toBe('Планка')
  })

  it('maps SUPERSET draft to valid SupersetItem with nested items', () => {
    const draft: PlanItemDraft = {
      itemType: 'SUPERSET',
      title: 'Суперсет: Грудь + Спина',
      restBetweenSetsSeconds: 120,
      items: [
        { name: 'Жим лежа', sets: 3, reps: 10, weight: 80 },
        { name: 'Тяга блока', sets: 3, reps: 12, weight: 60 },
      ],
    }

    const item = mapDraftToPlanItem(draft) as SupersetItem
    expect(item.itemType).toBe('SUPERSET')
    expect(item.title).toBe('Суперсет: Грудь + Спина')
    expect(item.restBetweenSetsSeconds).toBe(120)
    expect(item.items?.length).toBe(2)
    expect(item.items?.[0]?.title).toBe('Жим лежа')
    expect(item.items?.[1]?.title).toBe('Тяга блока')
  })

  it('calculates total exercise counts and formatted summaries', () => {
    const items = [
      { id: '1', itemType: 'EXERCISE' as const, title: 'Упр 1' },
      {
        id: '2',
        itemType: 'CIRCUIT' as const,
        title: 'Круг 1',
        items: [
          { id: '2-1', itemType: 'EXERCISE' as const, title: 'Круг упр 1' },
          { id: '2-2', itemType: 'EXERCISE' as const, title: 'Круг упр 2' },
        ],
      },
      {
        id: '3',
        itemType: 'SUPERSET' as const,
        title: 'Суперсет 1',
        items: [
          { id: '3-1', itemType: 'EXERCISE' as const, title: 'Суперсет упр 1' },
          { id: '3-2', itemType: 'EXERCISE' as const, title: 'Суперсет упр 2' },
        ],
      },
    ]

    expect(countTotalExercises(items)).toBe(5)
    expect(formatPlanStructureSummary(items)).toBe('1 упр., 1 круг., 1 суперсет.')
    expect(getItemTypeLabel('EXERCISE')).toBe('Упражнение')
    expect(getItemTypeLabel('CIRCUIT')).toBe('Круговая')
    expect(getItemTypeLabel('SUPERSET')).toBe('Суперсет')
  })
})

describe('PlanItemCard Component', () => {
  it('renders single exercise item', () => {
    const item: ExerciseItem = {
      id: 'ex-1',
      itemType: 'EXERCISE',
      title: 'Становая тяга',
      description: '3 подх. × 5 повт. (120 кг)',
      restBetweenSetsSeconds: 180,
    }

    const wrapper = mount(PlanItemCard, {
      props: { item, index: 0 },
    })

    expect(wrapper.text()).toContain('Становая тяга')
    expect(wrapper.text()).toContain('3 подх. × 5 повт. (120 кг)')
    expect(wrapper.text()).toContain('Отдых: 180 с')
    expect(wrapper.text()).toContain('Упражнение')
  })

  it('renders circuit item with rounds and nested exercises', () => {
    const item: CircuitItem = {
      id: 'circ-1',
      itemType: 'CIRCUIT',
      title: 'HIIT Круговая',
      rounds: 5,
      restBetweenRoundsSeconds: 60,
      items: [
        { id: 'sub-1', itemType: 'EXERCISE', title: 'Бёрпи', description: '20 повт.' },
        { id: 'sub-2', itemType: 'EXERCISE', title: 'Махи гирей', description: '25 повт.' },
      ],
    }

    const wrapper = mount(PlanItemCard, {
      props: { item, index: 1 },
    })

    expect(wrapper.text()).toContain('HIIT Круговая')
    expect(wrapper.text()).toContain('5 раундов')
    expect(wrapper.text()).toContain('Отдых между кругами: 60 с')
    expect(wrapper.text()).toContain('Бёрпи')
    expect(wrapper.text()).toContain('Махи гирей')
    expect(wrapper.text()).toContain('Круговая')
  })

  it('renders superset item with nested connected exercises', () => {
    const item: SupersetItem = {
      id: 'sup-1',
      itemType: 'SUPERSET',
      title: 'Суперсет на руки',
      restBetweenSetsSeconds: 90,
      items: [
        { id: 'sub-1', itemType: 'EXERCISE', title: 'Бицепс с гантелями', description: '12 повт.' },
        { id: 'sub-2', itemType: 'EXERCISE', title: 'Брусья на трицепс', description: '12 повт.' },
      ],
    }

    const wrapper = mount(PlanItemCard, {
      props: { item, index: 2 },
    })

    expect(wrapper.text()).toContain('Суперсет на руки')
    expect(wrapper.text()).toContain('2 упражнения без отдыха')
    expect(wrapper.text()).toContain('Отдых после суперсета: 90 с')
    expect(wrapper.text()).toContain('Бицепс с гантелями')
    expect(wrapper.text()).toContain('Брусья на трицепс')
    expect(wrapper.text()).toContain('Суперсет')
  })
})

describe('PlanItemBuilder Component', () => {
  it('allows adding single exercise, circuit, and superset items', async () => {
    const items: PlanItemDraft[] = []
    const wrapper = mount(PlanItemBuilder, {
      props: {
        items,
        'onUpdate:items': (val: PlanItemDraft[]) => wrapper.setProps({ items: val }),
      },
    })

    // 1. Добавляем одиночное упражнение
    const exInput = wrapper.find('#exercise-name-input')
    await exInput.setValue('Подтягивания')
    const addExBtn = wrapper.find('#add-exercise-btn')
    await addExBtn.trigger('click')

    const currentItems = wrapper.props('items') as PlanItemDraft[]
    expect(currentItems.length).toBe(1)
    expect(currentItems[0]?.itemType).toBe('EXERCISE')
    expect((currentItems[0] as { name?: string })?.name).toBe('Подтягивания')

    // 2. Переключаемся на Круговую и добавляем ее
    const tabCircuitBtn = wrapper.find('#tab-circuit-btn')
    await tabCircuitBtn.trigger('click')

    const circuitTitleInput = wrapper.find('#circuit-title-input')
    await circuitTitleInput.setValue('Круг на пресс')

    const circuitSubInput = wrapper.find('#circuit-sub-name-input')
    await circuitSubInput.setValue('Планка')
    const addSubCircBtn = wrapper.find('#add-circuit-sub-btn')
    await addSubCircBtn.trigger('click')

    const addCircuitBtn = wrapper.find('#add-circuit-btn')
    await addCircuitBtn.trigger('click')

    const afterCircuitItems = wrapper.props('items') as PlanItemDraft[]
    expect(afterCircuitItems.length).toBe(2)
    expect(afterCircuitItems[1]?.itemType).toBe('CIRCUIT')

    // 3. Переключаемся на Суперсет и добавляем 2 упражнения
    const tabSupersetBtn = wrapper.find('#tab-superset-btn')
    await tabSupersetBtn.trigger('click')

    const supersetTitleInput = wrapper.find('#superset-title-input')
    await supersetTitleInput.setValue('Суперсет Руки')

    const supersetSubInput = wrapper.find('#superset-sub-name-input')
    const addSubSupBtn = wrapper.find('#add-superset-sub-btn')

    await supersetSubInput.setValue('Подъем штанги')
    await addSubSupBtn.trigger('click')

    await supersetSubInput.setValue('Жим узким хватом')
    await addSubSupBtn.trigger('click')

    const addSupersetBtn = wrapper.find('#add-superset-btn')
    await addSupersetBtn.trigger('click')

    const afterSupersetItems = wrapper.props('items') as PlanItemDraft[]
    expect(afterSupersetItems.length).toBe(3)
    expect(afterSupersetItems[2]?.itemType).toBe('SUPERSET')
  })
})
