import type { TrainingPlanResponseObject } from '@/shared/api/generated/models/trainingPlanResponseObject'
import type { ExerciseItem } from '@/shared/api/generated/models/exerciseItem'
import type { CircuitItem } from '@/shared/api/generated/models/circuitItem'
import type { SupersetItem } from '@/shared/api/generated/models/supersetItem'

function appendExerciseDetails(
  lines: string[],
  exercise: ExerciseItem,
  indent: string,
  includeDescription = true,
) {
  if (includeDescription && exercise.description) {
    lines.push(`${indent}• ${exercise.description}`)
  }
  for (const [index, set] of (exercise.sets ?? []).entries()) {
    const parameters: string[] = []
    if (set.reps?.trim()) parameters.push(`${set.reps.trim()} повт.`)
    if (set.weight?.trim()) {
      parameters.push(`${set.weight.trim()} ${set.weightUnit?.trim() || 'кг'}`)
    }
    if (set.durationSeconds) parameters.push(`${set.durationSeconds} сек`)
    lines.push(`${indent}• Подход ${index + 1}: ${parameters.join(', ') || 'параметры не заданы'}`)
  }
  if (exercise.restBetweenSetsSeconds) {
    lines.push(`${indent}• Отдых: ${exercise.restBetweenSetsSeconds} сек`)
  }
}

/**
 * Преобразует тренировочный план в структурированный читаемый plain-text
 * для отправки клиенту в личные сообщения без лишних метаданных.
 */
export function formatPlanToShareText(plan: TrainingPlanResponseObject): string {
  const lines: string[] = []

  const title = plan.title?.trim() || 'Тренировочный план'
  lines.push(`🏋️ План: ${title}`)
  lines.push('')

  const items = plan.planItems || []
  if (items.length === 0) {
    lines.push('В плане пока нет упражнений.')
  } else {
    items.forEach((item, index) => {
      const num = index + 1
      const itemType = item.itemType

      if (itemType === 'CIRCUIT') {
        const circuit = item as CircuitItem
        const rounds = circuit.rounds ?? 1
        const roundWord = rounds === 1 ? 'раунд' : rounds < 5 ? 'раунда' : 'раундов'
        lines.push(`${num}. Круговая тренировка (${rounds} ${roundWord}):`)
        if (circuit.description && !circuit.description.startsWith('Круговая тренировка:')) {
          lines.push(`   📝 ${circuit.description}`)
        }
        const subItems = circuit.items || []
        subItems.forEach((sub, sIdx) => {
          const letter = String.fromCharCode(1072 + sIdx) // а, б, в, г...
          const subTitle = sub.title || 'Упражнение'
          const subDesc = sub.description ? ` (${sub.description})` : ''
          lines.push(`   ${letter}) ${subTitle}${subDesc}`)
          appendExerciseDetails(lines, sub as ExerciseItem, '      ', false)
        })
        if (circuit.restBetweenRoundsSeconds) {
          lines.push(`   • Отдых между кругами: ${circuit.restBetweenRoundsSeconds} сек`)
        }
      } else if (itemType === 'SUPERSET') {
        const superset = item as SupersetItem
        lines.push(`${num}. Суперсет:`)
        if (superset.description && !superset.description.startsWith('Суперсет из')) {
          lines.push(`   📝 ${superset.description}`)
        }
        const subItems = superset.items || []
        subItems.forEach((sub, sIdx) => {
          const letter = String.fromCharCode(1072 + sIdx) // а, б, в, г...
          const subTitle = sub.title || 'Упражнение'
          const subDesc = sub.description ? ` (${sub.description})` : ''
          lines.push(`   ${letter}) ${subTitle}${subDesc}`)
          appendExerciseDetails(lines, sub as ExerciseItem, '      ', false)
        })
        if (superset.restBetweenSetsSeconds) {
          lines.push(`   • Отдых: ${superset.restBetweenSetsSeconds} сек`)
        }
      } else {
        // Обычное упражнение
        const exercise = item as ExerciseItem
        const exTitle = exercise.title || 'Упражнение'
        lines.push(`${num}. ${exTitle}`)
        appendExerciseDetails(lines, exercise, '   ')
      }

      // Пустая строка между блоками упражнений
      lines.push('')
    })
  }

  lines.push('💪 Продуктивной тренировки!')
  return lines.join('\n')
}

/**
 * Формирует ссылку для шеринга ВКонтакте (открытие окна «Отправить в личном сообщении»)
 */
export function getVkShareUrl(text: string): string {
  return `https://vk.com/share.php?comment=${encodeURIComponent(text)}`
}

/**
 * Формирует ссылку для шеринга в Telegram (выбор диалога и вставка текста)
 */
export function getTelegramShareUrl(text: string): string {
  return `https://t.me/share/url?text=${encodeURIComponent(text)}`
}

/**
 * Формирует ссылку для шеринга в WhatsApp
 */
export function getWhatsAppShareUrl(text: string): string {
  return `https://api.whatsapp.com/send?text=${encodeURIComponent(text)}`
}

/**
 * Копирует текст плана в буфер обмена
 */
export async function copyPlanToClipboard(text: string): Promise<boolean> {
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text)
      return true
    }
    // Fallback для окружений без navigator.clipboard
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    const successful = document.execCommand('copy')
    document.body.removeChild(textarea)
    return successful
  } catch (err) {
    console.error('Failed to copy to clipboard', err)
    return false
  }
}
