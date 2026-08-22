import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Pagination from '../shared/ui/pagination/Pagination.vue'

describe('Pagination Component', () => {
  it('renders correct range text and page buttons for multiple pages', () => {
    const wrapper = mount(Pagination, {
      props: {
        pageNumber: 1,
        pageSize: 10,
        totalSize: 25,
      },
    })

    expect(wrapper.text()).toContain('Показано 1–10 из 25')
    expect(wrapper.find('#pagination-prev-btn').attributes('disabled')).toBeDefined()
    expect(wrapper.find('#pagination-next-btn').attributes('disabled')).toBeUndefined()
    expect(wrapper.find('#pagination-page-1').exists()).toBe(true)
    expect(wrapper.find('#pagination-page-2').exists()).toBe(true)
    expect(wrapper.find('#pagination-page-3').exists()).toBe(true)
  })

  it('emits update:pageNumber when next button or page number is clicked', async () => {
    const wrapper = mount(Pagination, {
      props: {
        pageNumber: 1,
        pageSize: 10,
        totalSize: 25,
      },
    })

    await wrapper.find('#pagination-next-btn').trigger('click')
    expect(wrapper.emitted('update:pageNumber')).toEqual([[2]])

    await wrapper.find('#pagination-page-3').trigger('click')
    expect(wrapper.emitted('update:pageNumber')).toEqual([[2], [3]])
  })

  it('emits update:pageSize and resets pageNumber when page size changes', async () => {
    const wrapper = mount(Pagination, {
      props: {
        pageNumber: 2,
        pageSize: 10,
        totalSize: 50,
      },
    })

    const select = wrapper.find('#pagination-page-size')
    await select.setValue('20')

    expect(wrapper.emitted('update:pageSize')).toEqual([[20]])
    expect(wrapper.emitted('update:pageNumber')).toEqual([[1]])
  })

  it('handles empty totalSize gracefully', () => {
    const wrapper = mount(Pagination, {
      props: {
        pageNumber: 1,
        pageSize: 10,
        totalSize: 0,
      },
    })

    expect(wrapper.text()).toContain('Показано 0–0 из 0')
    expect(wrapper.find('#pagination-prev-btn').attributes('disabled')).toBeDefined()
    expect(wrapper.find('#pagination-next-btn').attributes('disabled')).toBeDefined()
  })
})
