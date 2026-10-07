import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'      // 样式必须引！
import zhCn from 'element-plus/es/locale/lang/zh-cn'  // 中文文案（可选）
import App from './App.vue'
import { createPinia } from 'pinia'
const pinia = createPinia()
import './styles/index.css'
import router from './router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(router)
app.use(pinia)
app.use(ElementPlus, { locale: zhCn })    // 全局注册
app.mount('#app')
