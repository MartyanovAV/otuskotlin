<script setup lang="ts">
import { useToast } from './toastStore'
import Toast from './Toast.vue'

const { items, dismiss } = useToast()
</script>

<template>
  <!--
    Контейнер фиксирован в правом нижнем углу. Стек тостов растёт снизу вверх.
    z-index вынесен в z-modal (--z-toast будет настроен в main.css).
    pointer-events-none на корне, pointer-events-auto на каждом тосте — чтобы клик
    «мимо» тоста не блокировал UI под ним.
  -->
  <div
    aria-label="Уведомления"
    class="pointer-events-none fixed bottom-4 right-4 z-toast flex flex-col-reverse gap-2 sm:bottom-6 sm:right-6"
  >
    <TransitionGroup
      enter-active-class="motion-safe:transition-all motion-safe:duration-200"
      leave-active-class="motion-safe:transition-all motion-safe:duration-200"
      enter-from-class="opacity-0 translate-x-3"
      leave-to-class="opacity-0 translate-x-3"
    >
      <Toast
        v-for="item in items"
        :key="item.id"
        :toast="item"
        @dismiss="dismiss"
      />
    </TransitionGroup>
  </div>
</template>
