import { reactive } from 'vue'

export type ConfirmVariant = 'default' | 'destructive'

export interface ConfirmOptions {
  title: string
  description?: string
  confirmText?: string
  cancelText?: string
  variant?: ConfirmVariant
}

interface PendingConfirm extends ConfirmOptions {
  resolve: (value: boolean) => void
}

interface ConfirmState {
  current: PendingConfirm | null
}

const state = reactive<ConfirmState>({
  current: null,
})

/**
 * Императивный API подтверждения. Возвращает Promise, который резолвится в true,
 * если пользователь подтвердил, и в false, если отменил (в т.ч. Esc / клик по оверлею / крестик).
 *
 * @example
 *   const ok = await confirm({ title: 'Удалить клиента?', variant: 'destructive' })
 *   if (!ok) return
 */
export const confirm = (options: ConfirmOptions): Promise<boolean> => {
  // Если уже открыт какой-то confirm — отменяем предыдущий как «отказ».
  if (state.current) {
    state.current.resolve(false)
  }
  return new Promise<boolean>((resolve) => {
    state.current = { ...options, resolve }
  })
}

/** Внутренние хелперы, вызываются из ConfirmHost. */
export const useConfirmState = () => {
  const resolve = (value: boolean) => {
    const current = state.current
    if (!current) return
    state.current = null
    current.resolve(value)
  }
  return {
    current: state,
    accept: () => resolve(true),
    cancel: () => resolve(false),
  }
}
