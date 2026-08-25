import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query'
import ClientListView from '../features/client-card/ClientListView.vue'
import * as clientApi from '../shared/api/generated/client-card/client-card'
import * as planApi from '../shared/api/generated/training-plan/training-plan'

vi.mock('../shared/api/generated/client-card/client-card', () => ({
  clientCardSearch: vi.fn<(...args: unknown[]) => unknown>(),
  useClientCardCreate: vi.fn<(...args: unknown[]) => unknown>(),
  useClientCardUpdate: vi.fn<(...args: unknown[]) => unknown>(),
}))

vi.mock('../shared/api/generated/training-plan/training-plan', () => ({
  trainingPlanSearch: vi.fn<(...args: unknown[]) => unknown>(),
  useTrainingPlanCreate: vi.fn<(...args: unknown[]) => unknown>(),
  useTrainingPlanActivate: vi.fn<(...args: unknown[]) => unknown>(() => ({
    mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
  })),
}))

describe('ClientListView with Training Plans', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    vi.clearAllMocks()
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
  })

  it('renders client cards and loads assigned training plans when client is selected', async () => {
    const mockClients = [
      {
        id: 'client-123',
        displayName: 'Иван Иванов',
        note: 'Цель: набор массы',
        status: 'ACTIVE' as const,
        createdAt: '2026-08-20T10:00:00Z',
      },
    ]

    const mockPlans = [
      {
        id: 'plan-456',
        title: 'Силовой цикл А',
        clientCardId: 'client-123',
        status: 'ACTIVE' as const,
        createdAt: '2026-08-21T12:00:00Z',
        planItems: [
          {
            id: 'item-1',
            itemType: 'EXERCISE' as const,
            title: 'Приседания со штангой',
            description: '3 подх. × 10 повт. (80 кг)',
          },
        ],
      },
    ]

    vi.mocked(clientApi.clientCardSearch).mockResolvedValue({
      data: {
        responseType: 'clientCard.search',
        result: 'success',
        clientCards: mockClients,
        totalSize: 1,
      },
      status: 200,
      headers: new Headers(),
    })

    vi.mocked(planApi.trainingPlanSearch).mockResolvedValue({
      data: {
        responseType: 'trainingPlan.search',
        result: 'success',
        trainingPlans: mockPlans,
        totalSize: 1,
      },
      status: 200,
      headers: new Headers(),
    })

    const mutateAsyncMock = vi.fn<(...args: unknown[]) => unknown>()
    vi.mocked(planApi.useTrainingPlanCreate).mockReturnValue({
      mutateAsync: mutateAsyncMock,
    } as unknown as ReturnType<typeof planApi.useTrainingPlanCreate>)

    vi.mocked(clientApi.useClientCardCreate).mockReturnValue({
      mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
    } as unknown as ReturnType<typeof clientApi.useClientCardCreate>)

    vi.mocked(clientApi.useClientCardUpdate).mockReturnValue({
      mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
    } as unknown as ReturnType<typeof clientApi.useClientCardUpdate>)

    const wrapper = mount(ClientListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    // Проверяем отображение таблицы клиентов
    const table = wrapper.find('table')
    expect(table.exists()).toBe(true)
    expect(wrapper.text()).toContain('Клиент')
    expect(wrapper.text()).toContain('Статус')
    expect(wrapper.text()).toContain('Заметка тренера')
    expect(wrapper.text()).toContain('Иван Иванов')
    expect(wrapper.text()).toContain('Цель: набор массы')

    // Кликаем по строке клиента для открытия модального окна
    const clientRow = wrapper.find('#client-row-client-123')
    expect(clientRow.exists()).toBe(true)
    await clientRow.trigger('click')
    await flushPromises()

    // В модальном окне (в document.body) должны отображаться заголовок планов и сам план
    expect(document.body.textContent).toContain('Назначенные планы тренировок')
    expect(document.body.textContent).toContain('Силовой цикл А')
    expect(document.body.textContent).toContain('1 эл. (1 упр.)')

    wrapper.unmount()
  })

  it('allows opening create plan dialog for the selected client', async () => {
    const mockClients = [
      {
        id: 'client-123',
        displayName: 'Иван Иванов',
        status: 'ACTIVE' as const,
      },
    ]

    vi.mocked(clientApi.clientCardSearch).mockResolvedValue({
      data: {
        responseType: 'clientCard.search',
        result: 'success',
        clientCards: mockClients,
        totalSize: 1,
      },
      status: 200,
      headers: new Headers(),
    })

    vi.mocked(planApi.trainingPlanSearch).mockResolvedValue({
      data: {
        responseType: 'trainingPlan.search',
        result: 'success',
        trainingPlans: [],
        totalSize: 0,
      },
      status: 200,
      headers: new Headers(),
    })

    const mutateAsyncMock = vi.fn<(...args: unknown[]) => unknown>().mockResolvedValue({
      data: {
        responseType: 'trainingPlan.create',
        trainingPlan: { id: 'new-plan-1', title: 'Новый план' },
      },
      status: 200,
    })

    vi.mocked(planApi.useTrainingPlanCreate).mockReturnValue({
      mutateAsync: mutateAsyncMock,
    } as unknown as ReturnType<typeof planApi.useTrainingPlanCreate>)

    vi.mocked(clientApi.useClientCardCreate).mockReturnValue({
      mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
    } as unknown as ReturnType<typeof clientApi.useClientCardCreate>)

    vi.mocked(clientApi.useClientCardUpdate).mockReturnValue({
      mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
    } as unknown as ReturnType<typeof clientApi.useClientCardUpdate>)

    const wrapper = mount(ClientListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    // Открываем карточку клиента
    const clientCard = wrapper.find('.cursor-pointer')
    await clientCard.trigger('click')
    await flushPromises()

    // Проверяем, что отображается сообщение об отсутствии планов
    expect(document.body.textContent).toContain(
      'У этого клиента пока нет назначенных тренировочных планов',
    )

    // Нажимаем кнопку добавления плана для клиента
    const addPlanBtn = document.body.querySelector(
      '#add-plan-to-client-btn',
    ) as HTMLButtonElement | null
    expect(addPlanBtn).not.toBeNull()
    addPlanBtn?.click()
    await flushPromises()

    // Проверяем, что открылся диалог создания плана с именем клиента
    expect(document.body.textContent).toContain('Новый план тренировки')
    expect(document.body.textContent).toContain('Назначение плана для клиента: Иван Иванов')

    wrapper.unmount()
  })
})
