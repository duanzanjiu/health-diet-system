import axios from "axios";
import { ElMessage } from 'element-plus'
import router from '@/router'

// 不需要携带 token 的公开接口（前缀匹配）
const PUBLIC_URLS = ['/auth/login', '/auth/test']

const service = axios.create({
    baseURL: '/api',
    timeout: 60000          // 大模型生成较慢（首次调用可能 5~30 秒），超时给足
})

// 清掉本地登录态并回到登录页
function clearLoginState() {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('menuList')
    // 已经在登录页就别再跳，否则 vue-router 会抛重复导航
    if (router.currentRoute.value.path !== '/login') {
        router.push('/login')   // ← 用 path，别用 {name:'login'}（名字是大写 Login）
    }
}

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        const url = config.url || ''
        // 公开接口一律不带 token：带了坏 token 会被后端 JwtAuthenticationFilter 直接 401
        const isPublic = PUBLIC_URLS.some(u => url.startsWith(u))
        if (token && !isPublic) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            if (res.code === 401) {
                clearLoginState()
            }
            return Promise.reject(res)
        }
        return response.data
    },
    error => {
        const status = error.response?.status
        const msg = error.response?.data?.msg      // ← 后端其实回了 msg，之前被丢掉了
        if (status === 401) {
            // token 无效/过期：必须清掉本地 token，否则就是死锁（见第三节）
            ElMessage.error(msg || '登录状态已失效，请重新登录')
            clearLoginState()
        } else if (status === 403) {
            // 目前后端"没带 token 访问受保护接口"返回的是 403，先给提示，不清 token
            ElMessage.error(msg || '没有访问权限')
        } else {
            ElMessage.error(msg || error.message || '网络异常，请稍后重试')
        }
        return Promise.reject(error)
    }
)
export default service