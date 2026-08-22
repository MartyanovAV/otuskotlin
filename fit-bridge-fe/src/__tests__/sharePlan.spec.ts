import { describe, it, expect, vi } from 'vitest'
import {
  formatPlanToShareText,
  getVkShareUrl,
  getTelegramShareUrl,
  getWhatsAppShareUrl,
  copyPlanToClipboard,
} from '../features/training-plan/lib/sharePlan'
import type { TrainingPlanResponseObject } from '../shared/api/generated/models/trainingPlanResponseObject'
import type { ExerciseItem } from '../shared/api/generated/models/exerciseItem'
import type { CircuitItem } from '../shared/api/generated/models/circuitItem'
import type { SupersetItem } from '../shared/api/generated/models/supersetItem'

describe('sharePlan utility', () => {
  it('formats basic plan with single exercises correctly', () => {
    const ex1: ExerciseItem = {
      id: 'ex-1',
      itemType: 'EXERCISE',
      title: 'Приседания со штангой',
      description: '3 подх. × 12 повт. (40 кг)',
    }
    const ex2: ExerciseItem = {
      id: 'ex-2',
      itemType: 'EXERCISE',
      title: 'Жим гантелей на наклонной скамье',
      description: '3 подх. × 10 повт. (14 кг)',
      restBetweenSetsSeconds: 60,
    }

    const mockPlan: TrainingPlanResponseObject = {
      id: 'plan-1',
      title: 'Силовой сплит — День 1',
      planItems: [ex1, ex2],
    }

    const text = formatPlanToShareText(mockPlan)

    expect(text).toContain('🏋️ План: Силовой сплит — День 1')
    expect(text).toContain('1. Приседания со штангой')
    expect(text).toContain('   • 3 подх. × 12 повт. (40 кг)')
    expect(text).toContain('2. Жим гантелей на наклонной скамье')
    expect(text).toContain('   • 3 подх. × 10 повт. (14 кг)')
    expect(text).toContain('   • Отдых: 60 сек')
    expect(text).toContain('💪 Продуктивной тренировки!')
    expect(text).not.toContain('Клиент:')
  })

  it('formats circuit training and superset items properly', () => {
    const circuit: CircuitItem = {
      id: 'circuit-1',
      itemType: 'CIRCUIT',
      title: 'Кардио-раунд',
      rounds: 3,
      restBetweenRoundsSeconds: 45,
      items: [
        {
          id: 'sub-1',
          itemType: 'EXERCISE',
          title: 'Планка',
          description: '60 сек',
        },
        {
          id: 'sub-2',
          itemType: 'EXERCISE',
          title: 'Берпи',
          description: '15 повт.',
        },
      ],
    }

    const superset: SupersetItem = {
      id: 'superset-1',
      itemType: 'SUPERSET',
      title: 'Руки суперсет',
      restBetweenSetsSeconds: 90,
      items: [
        {
          id: 'sub-3',
          itemType: 'EXERCISE',
          title: 'Подъем на бицепс',
          description: '3 × 12 (10 кг)',
        },
        {
          id: 'sub-4',
          itemType: 'EXERCISE',
          title: 'Французский жим',
          description: '3 × 12 (15 кг)',
        },
      ],
    }

    const mockPlan: TrainingPlanResponseObject = {
      id: 'plan-2',
      title: 'Круговая и суперсет',
      planItems: [circuit, superset],
    }

    const text = formatPlanToShareText(mockPlan)

    expect(text).toContain('1. Круговая тренировка (3 раунда):')
    expect(text).toContain('   а) Планка (60 сек)')
    expect(text).toContain('   б) Берпи (15 повт.)')
    expect(text).toContain('   • Отдых между кругами: 45 сек')

    expect(text).toContain('2. Суперсет:')
    expect(text).toContain('   а) Подъем на бицепс (3 × 12 (10 кг))')
    expect(text).toContain('   б) Французский жим (3 × 12 (15 кг))')
    expect(text).toContain('   • Отдых: 90 сек')
  })

  it('handles empty plan gracefully', () => {
    const emptyPlan: TrainingPlanResponseObject = {
      id: 'empty',
      title: 'Пустой план',
      planItems: [],
    }

    const text = formatPlanToShareText(emptyPlan)
    expect(text).toContain('🏋️ План: Пустой план')
    expect(text).toContain('В плане пока нет упражнений.')
  })

  it('formats structured exercise sets when description is absent', () => {
    const exercise: ExerciseItem = {
      id: 'exercise-with-sets',
      itemType: 'EXERCISE',
      title: 'Тяга штанги',
      sets: [
        { reps: '12', weight: '40', weightUnit: 'кг' },
        { durationSeconds: 45 },
      ],
      restBetweenSetsSeconds: 60,
    }
    const plan: TrainingPlanResponseObject = {
      id: 'structured-sets',
      title: 'Структурированные подходы',
      planItems: [exercise],
    }

    const text = formatPlanToShareText(plan)

    expect(text).toContain('• Подход 1: 12 повт., 40 кг')
    expect(text).toContain('• Подход 2: 45 сек')
    expect(text).toContain('• Отдых: 60 сек')
  })

  it('generates correct encoded URLs for VK, Telegram, and WhatsApp', () => {
    const rawText = 'Привет! План:\n1. Приседания'
    const vkUrl = getVkShareUrl(rawText)
    const tgUrl = getTelegramShareUrl(rawText)
    const waUrl = getWhatsAppShareUrl(rawText)

    expect(vkUrl).toBe('https://vk.com/share.php?comment=' + encodeURIComponent(rawText))
    expect(tgUrl).toBe('https://t.me/share/url?text=' + encodeURIComponent(rawText))
    expect(waUrl).toBe('https://api.whatsapp.com/send?text=' + encodeURIComponent(rawText))
  })

  it('copies text using navigator.clipboard when available', async () => {
    const writeTextMock = vi.fn<(text: string) => Promise<void>>().mockResolvedValue(undefined)
    Object.assign(navigator, {
      clipboard: {
        writeText: writeTextMock,
      },
    })

    const res = await copyPlanToClipboard('Тестовый текст')
    expect(res).toBe(true)
    expect(writeTextMock).toHaveBeenCalledWith('Тестовый текст')
  })
})
