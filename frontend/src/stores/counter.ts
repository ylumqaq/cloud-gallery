import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

// 定义一个全局计数器 store，用于演示 Pinia 的基本用法
export const useCounterStore = defineStore('counter', () => {
  // 状态：当前计数
  const count = ref(0)
  // 计算属性：翻倍后的计数
  const doubleCount = computed(() => count.value * 2)
  // 动作：计数加一
  function increment() {
    count.value++
  }

  return { count, doubleCount, increment }
})
