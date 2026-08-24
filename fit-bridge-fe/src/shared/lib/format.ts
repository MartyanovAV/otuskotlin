/**
 * Утилиты форматирования дат, чисел и относительных интервалов.
 *
 * formatRelativeDate — «5 мин назад», «вчера», «3 дня назад», «2 мес назад».
 * formatExactDateTime — точный «24 авг. 2026, 03:40» (для title/tooltip).
 * formatShortDate — «24 авг.» (для старых записей без времени).
 */

type Plurals = [string, string, string];

function pluralizeRu(n: number, [one, few, many]: Plurals): string {
  const mod10 = n % 10;
  const mod100 = n % 100;
  if (mod10 === 1 && mod100 !== 11) return one;
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return few;
  return many;
}

const MINUTE: Plurals = ['минуту', 'минуты', 'минут'];
const HOUR: Plurals = ['час', 'часа', 'часов'];
const DAY: Plurals = ['день', 'дня', 'дней'];
const WEEK: Plurals = ['неделю', 'недели', 'недель'];
const MONTH: Plurals = ['месяц', 'месяца', 'месяцев'];

const MS_IN_MINUTE = 60_000;
const MS_IN_HOUR = 60 * MS_IN_MINUTE;
const MS_IN_DAY = 24 * MS_IN_HOUR;
const MS_IN_WEEK = 7 * MS_IN_DAY;
const MS_IN_MONTH = 30 * MS_IN_DAY;
const MS_IN_YEAR = 365 * MS_IN_DAY;

/**
 * Возвращает человекочитаемое «относительное» представление даты.
 * Для null/invalid — прочерк. Для будущих дат — короткая дата.
 */
export function formatRelativeDate(isoString?: string | null): string {
  if (!isoString) return '—';
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return '—';

  const diffMs = Date.now() - date.getTime();
  if (diffMs < 0) return formatShortDate(date);

  if (diffMs < MS_IN_MINUTE) return 'только что';

  const minutes = Math.floor(diffMs / MS_IN_MINUTE);
  if (minutes < 60) return `${minutes} ${pluralizeRu(minutes, MINUTE)} назад`;

  const hours = Math.floor(diffMs / MS_IN_HOUR);
  if (hours < 24) return `${hours} ${pluralizeRu(hours, HOUR)} назад`;

  const days = Math.floor(diffMs / MS_IN_DAY);
  if (days === 1) return 'вчера';
  if (days < 7) return `${days} ${pluralizeRu(days, DAY)} назад`;

  const weeks = Math.floor(diffMs / MS_IN_WEEK);
  if (weeks < 4) return `${weeks} ${pluralizeRu(weeks, WEEK)} назад`;

  const months = Math.floor(diffMs / MS_IN_MONTH);
  if (months < 12) return `${months} ${pluralizeRu(months, MONTH)} назад`;

  return formatShortDate(date);
}

/**
 * Точная дата+время: «24 авг. 2026, 03:40» (ru-RU, с текущей локалью).
 * Для tooltip/title атрибутов.
 */
export function formatExactDateTime(isoString?: string | null): string {
  if (!isoString) return '—';
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Короткая дата без времени: «24 авг.» (год добавляется, если не текущий).
 */
export function formatShortDate(date: Date): string {
  const includeYear = date.getFullYear() !== new Date().getFullYear();
  return date.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'short',
    ...(includeYear ? { year: 'numeric' as const } : {}),
  });
}
