import { describe, it, expect, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter, type Router } from 'vue-router'
import Sidebar from '../shared/ui/layout/Sidebar.vue'

// Регрессионный тест на «редирект» при навигации между дашбордом/клиентами/планами.
//
// Корневая причина бага: в кастомном слоте router-link на onClick вызывался
// `navigate()` без MouseEvent, из-за чего `guardEvent` в vue-router 5 не
// вызывал `event.preventDefault()`. Браузер выполнял default-action на
// <a href> и делал полную перезагрузку страницы.
//
// Тест проверяет, что клик по пункту навигации НЕ приводит к перезагрузке:
// - DOM-элемент получает native click event с defaultPrevented === true
//   (значит preventDefault был вызван, и браузер НЕ пойдёт по href);
// - текущий путь в роутере меняется (значит router.push сработал).

const DashboardStub = { template: '<div data-testid="dashboard-view" />' }
const ClientsStub = { template: '<div data-testid="clients-view" />' }
const PlansStub = { template: '<div data-testid="plans-view" />' }
const UnauthorizedStub = { template: '<div data-testid="unauthorized-view" />' }

const buildRouter = (): Router =>
  createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/',
        component: { template: '<router-view />' },
        children: [
          { path: '', name: 'dashboard', component: DashboardStub },
          { path: 'clients', name: 'clients', component: ClientsStub },
          { path: 'plans', name: 'plans', component: PlansStub },
        ],
      },
      { path: '/unauthorized', name: 'unauthorized', component: UnauthorizedStub },
    ],
  })

describe('Sidebar navigation', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('prevents the default click so the browser does not reload the page', async () => {
    const router = buildRouter()
    router.push('/')
    await router.isReady()

    const wrapper = mount(Sidebar, {
      global: {
        plugins: [router],
        stubs: { Button: { template: '<button @click="$emit(\'click\')"><slot /></button>' } },
      },
    })

    const clientsLink = wrapper.find<HTMLAnchorElement>('a[href="/clients"]')
    expect(clientsLink.exists()).toBe(true)

    // Диспатчим «настоящий» MouseEvent: если RouterLink вызвал preventDefault,
    // свойство defaultPrevented станет true. До фикса оно оставалось false
    // и jsdom пытался выполнить navigation к /clients (Not implemented).
    const event = new MouseEvent('click', { bubbles: true, cancelable: true })
    clientsLink.element.dispatchEvent(event)
    await flushPromises()

    expect(event.defaultPrevented).toBe(true)
  })

  it('updates the router location after clicking a nav item', async () => {
    const router = buildRouter()
    router.push('/')
    await router.isReady()

    const wrapper = mount(Sidebar, {
      global: {
        plugins: [router],
        stubs: { Button: { template: '<button @click="$emit(\'click\')"><slot /></button>' } },
      },
    })

    expect(router.currentRoute.value.path).toBe('/')

    const clientsLink = wrapper.find<HTMLAnchorElement>('a[href="/clients"]')
    expect(clientsLink.exists()).toBe(true)

    await clientsLink.trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/clients')
  })

  it('marks the active nav item with the active-class and aria-current', async () => {
    const router = buildRouter()
    router.push('/clients')
    await router.isReady()

    const wrapper = mount(Sidebar, {
      global: {
        plugins: [router],
        stubs: { Button: { template: '<button @click="$emit(\'click\')"><slot /></button>' } },
      },
    })

    const clientsLink = wrapper.find<HTMLAnchorElement>('a[href="/clients"]')
    const dashboardLink = wrapper.find<HTMLAnchorElement>('a[href="/"]')
    // Активный пункт получает визуальный сигнал через :active-class (bg-primary)
    // и a11y-атрибут aria-current="page" от RouterLink.
    expect(clientsLink.classes()).toContain('!bg-primary')
    expect(clientsLink.attributes('aria-current')).toBe('page')
    // Неактивный пункт — не должен иметь aria-current.
    expect(dashboardLink.attributes('aria-current')).toBeUndefined()
  })
})
