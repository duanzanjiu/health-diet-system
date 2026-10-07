<template>
    <div class="auth-page">
        <!-- 左侧品牌面板 — 有机视觉叙事 -->
        <aside class="auth-brand">
            <!-- 动态光圈背景 -->
            <div class="brand-orbs">
                <div class="orb orb--1"></div>
                <div class="orb orb--2"></div>
                <div class="orb orb--3"></div>
            </div>
            <!-- 纹理叠加 -->
            <div class="brand-grain"></div>

            <div class="brand-content">
                <div class="brand-badge-wrap">
                <div class="brand-badge">🌿</div>
                <div class="badge-ring"></div>
                </div>
                <h1 class="font-display brand-title">Healthy<span>Diet</span></h1>
                <p class="brand-tagline">健康饮食，从每一餐开始</p>

                <div class="brand-divider"></div>

                <div class="brand-features">
                <div class="feature-item" v-for="(f, i) in features" :key="i"
                    :style="{ animationDelay: `${0.6 + i * 0.12}s` }">
                    <span class="feature-icon">{{ f.icon }}</span>
                    <div class="feature-text">
                    <span class="feature-title">{{ f.title }}</span>
                    <span class="feature-desc">{{ f.desc }}</span>
                    </div>
                </div>
                </div>
            </div>
            <!-- 底部装饰线 -->
            <div class="brand-bottom-line"></div>
        </aside>
        <!-- 右侧登录表单 -->
        <main class="auth-form-area">
            <div class="auth-card">
                <div class="auth-header">
                    <p class="auth-label">SIGN IN</p>
                    <h2 class="font-display">欢迎回来</h2>
                    <p class="auth-desc">登录您的账号，继续健康之旅</p>
                </div>
                <el-form label-width="120px" ref="formRef" label-position="top" :model="form" :rules="rules" >
                    <el-form-item label="用户名" prop="username">
                        <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User"></el-input>
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="form.password" placeholder="请输入密码" type="password" prefix-icon="Lock" show-password="password" @keyup.enter="handleSubmit"></el-input>
                    </el-form-item>
                    <el-form-item class="submit-item">
                        <el-button class="auth-submit" :loading="loading" type="primary" @click="handleSubmit">登录</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </main>
    </div>
</template>
<script setup>
import {ref, reactive } from 'vue'
import {login} from '@/api/auth'
import {ElMessage} from 'element-plus'
//import router from '@/router'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

const form = reactive({
    username: '',
    password: ''
})

const rules = reactive({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
})

//登录
const formRef = ref(null)
const loading = ref(false)
const handleSubmit = async () => {
    try {
        await formRef.value.validate()      // 校验失败也会抛出
        loading.value = true
        const res = await login(form)
        userStore.setLoginInfo({
            token: res.data.token,
            userInfo: res.data.userInfo,
            menuList: res.data.menus
        })
        const roles = res.data.userInfo?.roles || []
        router.push(roles.includes('ADMIN') ? '/admin/dashboard' : '/home/index')
    } catch (e) {
        // 错误提示已由拦截器统一弹出，这里无需重复
    } finally {
        loading.value = false               // ← 无论成败都会复位
    }
}

       const features = [
        { icon: '🥗', title: '智能食谱推荐', desc: '基于AI的个性化方案' },
        { icon: '📊', title: '营养成分分析', desc: '详尽的营养数据追踪' },
        { icon: '🤖', title: 'AI 饮食规划', desc: '通义千问深度定制' },
        { icon: '💚', title: '健康档案管理', desc: '全面的健康画像记录' }
    ]

</script>
<style scoped>
.auth-page {
  width: 100%;
  height: 100vh;
  display: flex;
  background: var(--bg-page);
  overflow: hidden;
}

/* ─── Brand Panel ─── */
.auth-brand {
  width: 52%;
  background: var(--c-forest-900);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 80px;
}

/* Dynamic orb background */
.brand-orbs {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.orb--1 {
  width: 420px;
  height: 420px;
  top: -10%;
  right: -8%;
  background: radial-gradient(circle, rgba(61, 139, 71, 0.2) 0%, transparent 70%);
  animation: gentleFloat 8s ease-in-out infinite;
}

.orb--2 {
  width: 300px;
  height: 300px;
  bottom: -5%;
  left: -5%;
  background: radial-gradient(circle, rgba(196, 147, 58, 0.12) 0%, transparent 70%);
  animation: gentleFloat 10s ease-in-out infinite 2s;
}

.orb--3 {
  width: 180px;
  height: 180px;
  top: 45%;
  right: 20%;
  background: radial-gradient(circle, rgba(228, 192, 106, 0.08) 0%, transparent 70%);
  animation: gentleFloat 7s ease-in-out infinite 4s;
}

/* Grain texture */
.brand-grain {
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='40' height='40' viewBox='0 0 40 40' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23ffffff' fill-opacity='0.018'%3E%3Cpath d='M0 40L40 0H20L0 20M40 40V20L20 40'/%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 1;
}

.brand-content {
  position: relative;
  z-index: 2;
  animation: brandFadeIn 1s var(--ease-out) both;
}

@keyframes brandFadeIn {
  from { opacity: 0; transform: translateX(-24px); }
  to { opacity: 1; transform: translateX(0); }
}

/* Brand badge with decorative ring */
.brand-badge-wrap {
  position: relative;
  width: 64px;
  height: 64px;
  margin-bottom: 28px;
}

.brand-badge {
  font-size: 36px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  filter: drop-shadow(0 4px 16px rgba(0,0,0,0.4));
  z-index: 1;
}

.badge-ring {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(196, 147, 58, 0.25);
  border-radius: 50%;
  animation: rotateSlow 20s linear infinite;
}

.badge-ring::before {
  content: '';
  position: absolute;
  top: -2px;
  left: 50%;
  width: 4px;
  height: 4px;
  background: var(--c-gold-400);
  border-radius: 50%;
}

.brand-title {
  font-size: 52px;
  color: var(--c-sand-50);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.05;
  margin-bottom: 10px;
}

.brand-title span {
  color: var(--c-gold-400);
}

.brand-tagline {
  font-size: 15px;
  color: rgba(250, 249, 246, 0.4);
  font-weight: 400;
  letter-spacing: 0.06em;
}

.brand-divider {
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, var(--c-gold-400), transparent);
  margin: 36px 0;
  border-radius: 2px;
}

/* Feature items */
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  opacity: 0;
  transform: translateX(-16px);
  animation: featureSlideIn 0.6s var(--ease-out) forwards;
}

@keyframes featureSlideIn {
  to { opacity: 1; transform: translateX(0); }
}

.feature-icon {
  font-size: 22px;
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.04);
  border-radius: var(--r-md);
  border: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.feature-text {
  display: flex;
  flex-direction: column;
}

.feature-title {
  font-size: 14px;
  font-weight: 600;
  color: rgba(250, 249, 246, 0.75);
  letter-spacing: 0.01em;
}

.feature-desc {
  font-size: 12px;
  color: rgba(250, 249, 246, 0.3);
  margin-top: 1px;
}

.brand-bottom-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 10%, rgba(196,147,58,0.15) 50%, transparent 90%);
}

/* ─── Form Panel ─── */
.auth-form-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 60px;
  position: relative;
}

.auth-card {
  width: 100%;
  max-width: 380px;
  animation: cardSlideUp 0.7s var(--ease-out) both 0.3s;
}

@keyframes cardSlideUp {
  from { opacity: 0; transform: translateY(28px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-header {
  margin-bottom: 36px;
}

.auth-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--c-gold-500);
  letter-spacing: 0.16em;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.auth-header h2 {
  font-size: 34px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: -0.02em;
  line-height: 1.15;
}

.auth-desc {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.5;
}

.submit-item {
  margin-top: 8px;
}

.auth-submit {
  width: 100%;
  height: 50px;
  font-size: 15px;
  font-weight: 700;
  border-radius: var(--r-md) !important;
  letter-spacing: 0.08em;
  background: var(--c-forest-700) !important;
  border-color: var(--c-forest-700) !important;
  transition: all var(--dur-normal) var(--ease-out) !important;
}

.auth-submit:hover {
  background: var(--c-forest-600) !important;
  border-color: var(--c-forest-600) !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(11, 29, 14, 0.18);
}

.auth-footer {
  text-align: center;
  margin-top: 32px;
  color: var(--text-muted);
  font-size: 14px;
}

.footer-line {
  width: 100%;
  height: 1px;
  background: var(--border-light);
  margin-bottom: 20px;
}

.auth-link {
  color: var(--c-forest-600);
  font-weight: 600;
  margin-left: 4px;
  transition: color var(--dur-fast) ease;
}

.auth-link:hover {
  color: var(--accent);
}

.auth-copyright {
  position: absolute;
  bottom: 24px;
  font-size: 12px;
  color: var(--text-muted);
  opacity: 0.6;
}

/* ─── Form refinements ─── */
:deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 12px;
  color: var(--text-secondary);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

:deep(.el-input__wrapper) {
  height: 48px;
  border-radius: var(--r-md) !important;
  box-shadow: 0 0 0 1px var(--border) inset !important;
  transition: all var(--dur-fast) ease !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--c-forest-300) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--c-forest-500) inset !important;
}

/* ─── Responsive ─── */
@media (max-width: 960px) {
  .auth-brand { display: none; }
  .auth-form-area { padding: 40px 24px; }
}
</style>
