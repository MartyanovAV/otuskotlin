import { LayoutDashboard, UsersRound, Dumbbell } from 'lucide-vue-next'
import type { Component } from 'vue'

/**
 * Маршруты верхнеуровневой навигации кабинета тренера.
 * Используется одновременно десктопным Sidebar и мобильным MobileNav —
 * изменения набора пунктов делаем только здесь.
 */
export interface NavItem {
  /** Путь Vue Router. */
  to: string
  /** Локализованная подпись. */
  label: string
  /** Иконка lucide-vue-next. */
  icon: Component
}

/**
 * Возвращает канонический список пунктов навигации кабинета.
 * Реактивностью оборачивать не нужно — список статический, перерендер
 * тригерится сменой маршрута, и `router-link` сам подсветит активный пункт.
 */
export const useNavItems = (): readonly NavItem[] => navItems

const navItems: readonly NavItem[] = [
  { to: '/', label: 'Дашборд', icon: LayoutDashboard },
  { to: '/clients', label: 'Клиенты', icon: UsersRound },
  { to: '/plans', label: 'Планы', icon: Dumbbell },
]
