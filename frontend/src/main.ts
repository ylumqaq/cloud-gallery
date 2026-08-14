import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 挂载 Pinia 状态管理与 Vue Router 路由
app.use(createPinia())
app.use(router)

app.mount('#app')
