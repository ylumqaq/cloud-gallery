<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useCounterStore } from '../stores/counter'

// 获取计数器 store 实例
const counter = useCounterStore()
// 通过 storeToRefs 解构出响应式状态，保持其响应性
const { count, doubleCount } = storeToRefs(counter)
</script>

<template>
  <main class="home">
    <h1>首页</h1>
    <p>当前计数：{{ count }}</p>
    <p>翻倍计数：{{ doubleCount }}</p>
    <button type="button" @click="counter.increment()">+1</button>
  </main>
</template>

<style scoped>
.home {
  max-width: 640px;
  margin: 0 auto;
  padding: 2rem;
  text-align: center;
}
</style>
