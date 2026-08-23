import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query'
import TrainingPlanListView from '../features/training-plan/TrainingPlanListView.vue'
import * as planApi from '../shared/api/generated/training-plan/training-plan'
import * as clientApi from '../shared/api/generated/client-card/client-card'

vi.mock('../shared/api/generated/training-plan/training-plan', () => ({
  trainingPlanSearch: vi.fn<(...args: unknown[]) => unknown>(),
  useTrainingPlanCreate: vi.fn<(...args: unknown[]) => unknown>(),
  useTrainingPlanUpdate: vi.fn<(...args: unknown[]) => unknown>(),
  useTrainingPlanActivate: vi.fn<(...args: unknown[]) => unknown>(() => ({
    mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
  })),
  useTrainingPlanComplete: vi.fn<(...args: unknown[]) => unknown>(() => ({
    mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
  })),
}))

vi.mock('../shared/api/generated/client-card/client-card', () => ({
  clientCardSearch: vi.fn<(...args: unknown[]) => unknown>(),
}))

describe('TrainingPlanListView Table View and Plan Items', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    vi.clearAllMocks()
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
  })

  it('renders training plans in a table and opens details modal with EXERCISE, CIRCUIT, SUPERSET items', async () => {
    const mockClients = [
      {
        id: 'client-1',
        displayName: 'Дмитрий Кузнецов',
      },
    ]

    const mockPlans = [
      {
        id: 'plan-101',
        title: 'Комплексная программа',
        clientCardId: 'client-1',
        status: 'ACTIVE' as const,
        createdAt: '2026-08-22T10:00:00Z',
        planItems: [
          {
            id: 'item-1',
            itemType: 'EXERCISE' as const,
            title: 'Жим лежа',
            description: '4 подх. × 8 повт. (90 кг)',
            restBetweenSetsSeconds: 60,
          },
          {
            id: 'item-2',
            itemType: 'CIRCUIT' as const,
            title: 'Круговая на пресс',
            description: '3 раунда на кор',
            rounds: 3,
            restBetweenRoundsSeconds: 45,
            items: [
              {
                id: 'sub-1',
                itemType: 'EXERCISE' as const,
                title: 'Планка',
                description: '1 подх. × 60 сек',
              },
              {
                id: 'sub-2',
                itemType: 'EXERCISE' as const,
                title: 'Скручивания',
                description: '1 подх. × 20 повт.',
              },
            ],
          },
          {
            id: 'item-3',
            itemType: 'SUPERSET' as const,
            title: 'Суперсет на руки',
            restBetweenSetsSeconds: 90,
            items: [
              {
                id: 'sub-3',
                itemType: 'EXERCISE' as const,
                title: 'Подъем штанги на бицепс',
                description: '3 подх. × 12 повт.',
              },
              {
                id: 'sub-4',
                itemType: 'EXERCISE' as const,
                title: 'Французский жим',
                description: '3 подх. × 12 повт.',
              },
            ],
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

    vi.mocked(planApi.useTrainingPlanCreate).mockReturnValue({
      mutateAsync: vi.fn<(...args: unknown[]) => unknown>(),
    } as unknown as ReturnType<typeof planApi.useTrainingPlanCreate>)

    const wrapper = mount(TrainingPlanListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    // Проверяем наличие таблицы и заголовков
    const table = wrapper.find('table')
    expect(table.exists()).toBe(true)
    expect(wrapper.text()).toContain('Название плана')
    expect(wrapper.text()).toContain('Клиент')
    expect(wrapper.text()).toContain('Статус')
    expect(wrapper.text()).toContain('Упражнений')

    // Проверяем строку плана и сводку по структуре (3 эл.: 1 упр., 1 круг., 1 суперсет.)
    expect(wrapper.text()).toContain('Комплексная программа')
    expect(wrapper.text()).toContain('Дмитрий Кузнецов')
    expect(wrapper.text()).toContain('3 эл.')
    expect(wrapper.text()).toContain('1 упр., 1 круг., 1 суперсет.')

    // Кликаем по строке плана
    const planRow = wrapper.find('#plan-row-plan-101')
    expect(planRow.exists()).toBe(true)
    await planRow.trigger('click')
    await flushPromises()

    // Проверяем открытие модального окна деталей с поддержкой всех типов
    expect(document.body.textContent).toContain('Состав плана (3):')
    // Проверяем EXERCISE
    expect(document.body.textContent).toContain('Жим лежа')
    expect(document.body.textContent).toContain('4 подх. × 8 повт. (90 кг)')
    // Проверяем CIRCUIT
    expect(document.body.textContent).toContain('Круговая на пресс')
    expect(document.body.textContent).toContain('3 раунда')
    expect(document.body.textContent).toContain('Планка')
    expect(document.body.textContent).toContain('Скручивания')
    // Проверяем SUPERSET
    expect(document.body.textContent).toContain('Суперсет на руки')
    expect(document.body.textContent).toContain('Подъем штанги на бицепс')
    expect(document.body.textContent).toContain('Французский жим')

    wrapper.unmount()
  })

  it('filters training plans by client and status using dropdown selectors', async () => {
    const mockClients = [
      { id: 'client-1', displayName: 'Алексей' },
      { id: 'client-2', displayName: 'Борис' },
    ]

    vi.mocked(clientApi.clientCardSearch).mockResolvedValue({
      data: {
        responseType: 'clientCard.search',
        result: 'success',
        clientCards: mockClients,
        totalSize: 2,
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

    const wrapper = mount(TrainingPlanListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    // Проверяем наличие выпадающих списков фильтрации
    const clientSelect = wrapper.find('#plan-client-filter')
    const statusSelect = wrapper.find('#plan-status-filter')

    expect(clientSelect.exists()).toBe(true)
    expect(statusSelect.exists()).toBe(true)
    expect(statusSelect.find('option[value="COMPLETED"]').exists()).toBe(true)

    // Выбираем клиента и статус
    await clientSelect.setValue('client-1')
    await statusSelect.setValue('ACTIVE')
    await flushPromises()

    // Проверяем вызов API с параметрами фильтра
    expect(planApi.trainingPlanSearch).toHaveBeenCalledWith(
      expect.objectContaining({
        trainingPlanFilter: expect.objectContaining({
          clientCardId: 'client-1',
          status: 'ACTIVE',
        }),
      }),
    )

    // Проверяем кнопку сброса
    const resetBtn = wrapper.find('#reset-plan-filters-btn')
    expect(resetBtn.exists()).toBe(true)
    await resetBtn.trigger('click')
    await flushPromises()

    expect((clientSelect.element as HTMLSelectElement).value).toBe('')
    expect((statusSelect.element as HTMLSelectElement).value).toBe('')

    wrapper.unmount()
  })

  it('opens edit modal with pre-filled plan data and submits update request', async () => {
    const mockClients = [
      { id: 'client-1', displayName: 'Дмитрий Кузнецов' },
    ]

    const mockPlan = {
      id: 'plan-101',
      lock: 'lock-test-123',
      title: 'Силовая база',
      clientCardId: 'client-1',
      status: 'ACTIVE' as const,
      createdAt: '2026-08-22T10:00:00Z',
      planItems: [
        {
          id: 'item-1',
          itemType: 'EXERCISE' as const,
          title: 'Приседания со штангой',
          description: '3 подх. × 10 повт. (100 кг), отдых 90 с',
          sets: [
            { reps: '10', weight: '100', weightUnit: 'KG', durationSeconds: 0 },
            { reps: '10', weight: '100', weightUnit: 'KG', durationSeconds: 0 },
            { reps: '10', weight: '100', weightUnit: 'KG', durationSeconds: 0 },
          ],
          restBetweenSetsSeconds: 90,
        },
      ],
    }

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
        trainingPlans: [mockPlan],
        totalSize: 1,
      },
      status: 200,
      headers: new Headers(),
    })

    const updateMutateAsync = vi.fn<(...args: unknown[]) => unknown>().mockResolvedValue({
      data: {
        responseType: 'trainingPlan.update',
        result: 'success',
        trainingPlan: {
          ...mockPlan,
          title: 'Обновленная силовая база',
          lock: 'lock-new-456',
        },
      },
      status: 200,
      headers: new Headers(),
    })

    vi.mocked(planApi.useTrainingPlanUpdate).mockReturnValue({
      mutateAsync: updateMutateAsync,
    } as unknown as ReturnType<typeof planApi.useTrainingPlanUpdate>)

    const wrapper = mount(TrainingPlanListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    // Нажимаем кнопку "Изменить" в таблице
    const editBtn = wrapper.find('#edit-plan-btn-plan-101')
    expect(editBtn.exists()).toBe(true)
    await editBtn.trigger('click')
    await flushPromises()

    // Проверяем открытие модалки и предзаполнение заголовка
    const editTitleInput = document.body.querySelector('#edit-plan-name') as HTMLInputElement
    expect(editTitleInput).not.toBeNull()
    expect(editTitleInput.value).toBe('Силовая база')
    expect(document.body.textContent).toContain('Дмитрий Кузнецов')
    expect(document.body.textContent).toContain('Приседания со штангой')

    // Меняем название
    editTitleInput.value = 'Обновленная силовая база'
    editTitleInput.dispatchEvent(new Event('input'))
    await flushPromises()

    // Нажимаем кнопку сохранения
    const saveBtn = document.body.querySelector('#save-plan-btn') as HTMLButtonElement
    expect(saveBtn).not.toBeNull()
    expect(saveBtn.disabled).toBe(false)
    saveBtn.click()
    await flushPromises()

    // Проверяем вызов useTrainingPlanUpdate
    expect(updateMutateAsync).toHaveBeenCalledWith({
      data: expect.objectContaining({
        requestType: 'trainingPlan.update',
        trainingPlan: expect.objectContaining({
          id: 'plan-101',
          lock: 'lock-test-123',
          title: 'Обновленная силовая база',
          planItems: expect.arrayContaining([
            expect.objectContaining({
              itemType: 'EXERCISE',
              title: 'Приседания со штангой',
            }),
          ]),
        }),
      }),
    })

    wrapper.unmount()
  })

  it('displays error message when update fails (concurrency conflict)', async () => {
    const mockClients = [{ id: 'client-1', displayName: 'Дмитрий' }]
    const mockPlan = {
      id: 'plan-102',
      lock: 'lock-old',
      title: 'Кардио план',
      clientCardId: 'client-1',
      status: 'ACTIVE' as const,
      createdAt: '2026-08-22T10:00:00Z',
      planItems: [
        {
          id: 'item-1',
          itemType: 'EXERCISE' as const,
          title: 'Бег',
          sets: [{ reps: '1', durationSeconds: 600 }],
        },
      ],
    }

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
        trainingPlans: [mockPlan],
        totalSize: 1,
      },
      status: 200,
      headers: new Headers(),
    })

    const updateMutateAsync = vi.fn<(...args: unknown[]) => unknown>().mockRejectedValue(
      new Error('План был изменен другим пользователем. Обновите данные.')
    )

    vi.mocked(planApi.useTrainingPlanUpdate).mockReturnValue({
      mutateAsync: updateMutateAsync,
    } as unknown as ReturnType<typeof planApi.useTrainingPlanUpdate>)

    const wrapper = mount(TrainingPlanListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    const editBtn = wrapper.find('#edit-plan-btn-plan-102')
    await editBtn.trigger('click')
    await flushPromises()

    const saveBtn = document.body.querySelector('#save-plan-btn') as HTMLButtonElement
    saveBtn.click()
    await flushPromises()

    const errorEl = document.body.querySelector('#edit-plan-error')
    expect(errorEl).not.toBeNull()
    expect(errorEl?.textContent).toContain('План был изменен другим пользователем')

    wrapper.unmount()
  })

  it('does not render edit button for completed training plans', async () => {
    const mockClients = [{ id: 'client-1', displayName: 'Дмитрий' }]
    const mockCompletedPlan = {
      id: 'plan-completed-1',
      lock: 'lock-completed',
      title: 'Завершенная тренировка',
      clientCardId: 'client-1',
      status: 'COMPLETED' as const,
      completedAt: '2026-08-22T12:00:00Z',
      createdAt: '2026-08-22T10:00:00Z',
      planItems: [
        {
          id: 'item-1',
          itemType: 'EXERCISE' as const,
          title: 'Бег',
          sets: [{ reps: '1', durationSeconds: 600 }],
        },
      ],
    }

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
        trainingPlans: [mockCompletedPlan],
        totalSize: 1,
      },
      status: 200,
      headers: new Headers(),
    })

    const wrapper = mount(TrainingPlanListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    // Кнопка редактирования в строке таблицы не должна отображаться
    const editBtn = wrapper.find('#edit-plan-btn-plan-completed-1')
    expect(editBtn.exists()).toBe(false)

    // Открываем модальное окно деталей
    const planRow = wrapper.find('#plan-row-plan-completed-1')
    await planRow.trigger('click')
    await flushPromises()

    // Кнопка редактирования в деталях также не должна отображаться
    const detailsEditBtn = document.body.querySelector('#details-edit-plan-btn')
    expect(detailsEditBtn).toBeNull()

    wrapper.unmount()
  })

  it('renders draft badge and allows activating draft training plan', async () => {
    const mockClients = [
      {
        id: 'client-1',
        displayName: 'Дмитрий Кузнецов',
      },
    ]

    const mockDraftPlan = {
      id: 'plan-draft-1',
      lock: 'lock-draft-1',
      title: 'Черновой план',
      clientCardId: 'client-1',
      status: 'DRAFT' as const,
      createdAt: '2026-08-22T10:00:00Z',
      planItems: [
        {
          id: 'item-1',
          itemType: 'EXERCISE' as const,
          title: 'Приседания',
        },
      ],
    }

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
        trainingPlans: [mockDraftPlan],
        totalSize: 1,
      },
      status: 200,
      headers: new Headers(),
    })

    const mutateAsyncActivate = vi.fn<(...args: unknown[]) => unknown>().mockResolvedValue({
      data: {
        responseType: 'trainingPlan.activate',
        result: 'success',
        trainingPlan: {
          ...mockDraftPlan,
          status: 'ACTIVE' as const,
        },
      },
      status: 200,
      headers: new Headers(),
    })

    vi.mocked(planApi.useTrainingPlanActivate).mockReturnValue({
      mutateAsync: mutateAsyncActivate,
    } as unknown as ReturnType<typeof planApi.useTrainingPlanActivate>)

    const wrapper = mount(TrainingPlanListView, {
      attachTo: document.body,
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })

    await flushPromises()

    // В строке таблицы виден статус "Черновик" и кнопка "Активировать"
    expect(wrapper.text()).toContain('Черновик')
    const activateBtn = wrapper.find('#activate-plan-btn-plan-draft-1')
    expect(activateBtn.exists()).toBe(true)

    // Кликаем по кнопке "Активировать"
    await activateBtn.trigger('click')
    await flushPromises()

    expect(mutateAsyncActivate).toHaveBeenCalledWith({
      data: {
        requestType: 'trainingPlan.activate',
        requestId: expect.any(String),
        trainingPlan: {
          id: 'plan-draft-1',
          lock: 'lock-draft-1',
        },
      },
    })

    wrapper.unmount()
  })
})
