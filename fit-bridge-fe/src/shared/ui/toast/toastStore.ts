import { reactive, readonly } from 'vue'

export type ToastVariant = 'success' | 'error' | 'warning' | 'info'

export interface ToastAction {
  label: string
  onClick: () => void
}

export interface ToastItem {
  id: string
  variant: ToastVariant
  title: string
  description?: string
  /** Время в мс до автоматического закрытия. 0 — не закрывать автоматически. */
  duration: number
  action?: ToastAction
  createdAt: number
}

interface ToastState {
  items: ToastItem[]
}

// Синглтон-стор на уровне модуля: один и тот же список виден из любого composable.
const state = reactive<ToastState>({
  items: [],
})

let counter = 0

const nextId = () => `toast-${Date.now().toString(36)}-${(counter += 1)}`

const dismiss = (id: string) => {
  const index = state.items.findIndex((item) => item.id === id)
  if (index >= 0) state.items.splice(index, 1)
}

const push = (item: Omit<ToastItem, 'id' | 'createdAt'>) => {
  const id = nextId()
  const fullItem: ToastItem = { id, createdAt: Date.now(), ...item }
  state.items.push(fullItem)
  if (fullItem.duration > 0) {
    window.setTimeout(() => dismiss(id), fullItem.duration)
  }
  return id
}

const build = (variant: ToastVariant, title: string, description?: string) => {
  // По умолчанию: success/info — 4с, warning — 6с, error — 8с.
  const defaultDurations: Record<ToastVariant, number> = {
    success: 4000,
    info: 4000,
    warning: 6000,
    error: 8000,
  }
  return {
    variant,
    title,
    description,
    duration: defaultDurations[variant],
  }
}

export const useToast = () => {
  return {
    items: readonly(state).items,
    dismiss,
    success: (title: string, description?: string) => push(build('success', title, description)),
    info: (title: string, description?: string) => push(build('info', title, description)),
    warning: (title: string, description?: string) => push(build('warning', title, description)),
    error: (title: string, description?: string) => push(build('error', title, description)),
    /**
     * Низкоуровневый push — для случаев, когда нужна экстра-кнопка или кастомный duration.
     */
    push,
  }
}
