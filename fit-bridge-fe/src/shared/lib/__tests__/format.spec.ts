import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { formatExactDateTime, formatRelativeDate, formatShortDate } from '../format'

describe('formatRelativeDate', () => {
  const NOW = new Date('2026-08-24T12:00:00.000Z')

  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(NOW)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('возвращает прочерк для null/undefined/invalid', () => {
    expect(formatRelativeDate(undefined)).toBe('—')
    expect(formatRelativeDate(null)).toBe('—')
    expect(formatRelativeDate('')).toBe('—')
    expect(formatRelativeDate('не-дата')).toBe('—')
  })

  it('«только что» для < 1 минуты', () => {
    expect(formatRelativeDate(NOW.toISOString())).toBe('только что')
    expect(formatRelativeDate(new Date(NOW.getTime() - 30_000).toISOString())).toBe('только что')
  })

  it('минуты с правильной плюрализацией: 1/2/5/11/21/22/25', () => {
    expect(formatRelativeDate(new Date(NOW.getTime() - 1 * 60_000).toISOString())).toBe('1 минуту назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 2 * 60_000).toISOString())).toBe('2 минуты назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 5 * 60_000).toISOString())).toBe('5 минут назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 11 * 60_000).toISOString())).toBe('11 минут назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 21 * 60_000).toISOString())).toBe('21 минуту назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 22 * 60_000).toISOString())).toBe('22 минуты назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 25 * 60_000).toISOString())).toBe('25 минут назад')
  })

  it('часы с правильной плюрализацией', () => {
    expect(formatRelativeDate(new Date(NOW.getTime() - 1 * 3_600_000).toISOString())).toBe('1 час назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 3 * 3_600_000).toISOString())).toBe('3 часа назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 5 * 3_600_000).toISOString())).toBe('5 часов назад')
  })

  it('вчера для 1 дня, X дн назад для 2–6', () => {
    const day = 24 * 3_600_000
    expect(formatRelativeDate(new Date(NOW.getTime() - 1 * day).toISOString())).toBe('вчера')
    expect(formatRelativeDate(new Date(NOW.getTime() - 2 * day).toISOString())).toBe('2 дня назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 5 * day).toISOString())).toBe('5 дней назад')
  })

  it('недели с плюрализацией 1/2/3 (4 недели → месяцы)', () => {
    const week = 7 * 24 * 3_600_000
    expect(formatRelativeDate(new Date(NOW.getTime() - 1 * week).toISOString())).toBe('1 неделю назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 2 * week).toISOString())).toBe('2 недели назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 3 * week).toISOString())).toBe('3 недели назад')
  })

  it('месяцы с плюрализацией', () => {
    const month = 30 * 24 * 3_600_000
    expect(formatRelativeDate(new Date(NOW.getTime() - 1 * month).toISOString())).toBe('1 месяц назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 2 * month).toISOString())).toBe('2 месяца назад')
    expect(formatRelativeDate(new Date(NOW.getTime() - 11 * month).toISOString())).toBe('11 месяцев назад')
  })

  it('возвращает короткую дату для будущих/далёких дат', () => {
    const future = new Date(NOW.getTime() + 3_600_000).toISOString()
    const result = formatRelativeDate(future)
    // Формат: "<день> <месяц>." (ru-RU) — проверяем, что это валидная дата-строка
    expect(result).toMatch(/^\d{1,2}\s[а-яё]+\.?$/i)
  })
})

describe('formatExactDateTime', () => {
  it('форматирует валидную дату в ru-RU', () => {
    const result = formatExactDateTime('2026-08-24T03:40:00.000Z')
    expect(result).toMatch(/24 авг\. 2026/)
    expect(result).toMatch(/\d{2}:\d{2}/)
  })

  it('возвращает прочерк для невалидных значений', () => {
    expect(formatExactDateTime(null)).toBe('—')
    expect(formatExactDateTime('не-дата')).toBe('—')
  })
})

describe('formatShortDate', () => {
  it('включает год для прошлого года', () => {
    const lastYear = new Date(new Date().getFullYear() - 1, 5, 15)
    const result = formatShortDate(lastYear)
    expect(result).toMatch(/\d{4}/)
  })
})
