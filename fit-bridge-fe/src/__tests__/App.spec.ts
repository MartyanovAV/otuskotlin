import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '../App.vue'
import { Button } from '../shared/ui/button'
import { Badge } from '../shared/ui/badge'
import { Card, CardHeader, CardTitle, CardContent } from '../shared/ui/card'

describe('FitBridge UI Components', () => {
  it('mounts App with RouterView stub', () => {
    const wrapper = mount(App, {
      global: {
        stubs: ['RouterView'],
      },
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('renders Button component with text slot', () => {
    const wrapper = mount(Button, {
      slots: {
        default: 'Добавить клиента',
      },
    })
    expect(wrapper.text()).toBe('Добавить клиента')
    expect(wrapper.classes()).toContain('inline-flex')
  })

  it('renders Badge with default and destructive variants', async () => {
    const wrapper = mount(Badge, {
      slots: {
        default: 'Активен',
      },
    })
    expect(wrapper.text()).toBe('Активен')
    expect(wrapper.classes()).toContain('bg-primary')

    const destructiveWrapper = mount(Badge, {
      props: {
        variant: 'destructive',
      },
      slots: {
        default: 'Ошибка',
      },
    })
    expect(destructiveWrapper.classes()).toContain('bg-danger')
  })

  it('renders Card with header and content', () => {
    const wrapper = mount({
      components: { Card, CardHeader, CardTitle, CardContent },
      template: `
        <Card>
          <CardHeader>
            <CardTitle>Тестовый заголовок</CardTitle>
          </CardHeader>
          <CardContent>
            <p>Тестовое содержимое</p>
          </CardContent>
        </Card>
      `,
    })
    expect(wrapper.text()).toContain('Тестовый заголовок')
    expect(wrapper.text()).toContain('Тестовое содержимое')
  })
})
