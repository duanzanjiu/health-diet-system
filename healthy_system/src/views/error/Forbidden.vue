<template>
  <div class="forbidden-page">
    <div class="nf-orbs">
      <div class="nf-orb nf-orb--1"></div>
      <div class="nf-orb nf-orb--2"></div>
    </div>

    <div class="nf-content">
      <div class="nf-badge">🔒</div>
      <h1 class="font-display nf-code">403</h1>
      <h2 class="nf-title">没有访问权限</h2>
      <p class="nf-desc">您没有权限访问此页面，请联系管理员</p>
      <div class="nf-actions">
        <el-button type="primary" size="large" round @click="goHome">
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-button size="large" round @click="$router.back()">
          <el-icon><Back /></el-icon> 返回上页
        </el-button>
      </div>
    </div>

    <p class="nf-copyright">HealthyDiet · 健康饮食管理系统</p>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const isAdmin = Array.isArray(userInfo.roles) && userInfo.roles.includes('ADMIN')
    router.push(isAdmin ? '/admin/dashboard' : '/home/index')
  } catch {
    router.push('/login')
  }
}
</script>

<style scoped>
.forbidden-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  position: relative;
  overflow: hidden;
}

.nf-orbs {
  position: absolute;
  inset: 0;
}

.nf-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
}

.nf-orb--1 {
  width: 500px; height: 500px;
  top: -15%; right: -10%;
  background: radial-gradient(circle, rgba(196, 68, 58, 0.08) 0%, transparent 70%);
  animation: gentleFloat 10s ease-in-out infinite;
}

.nf-orb--2 {
  width: 400px; height: 400px;
  bottom: -10%; left: -5%;
  background: radial-gradient(circle, rgba(196, 147, 58, 0.06) 0%, transparent 70%);
  animation: gentleFloat 12s ease-in-out infinite 3s;
}

.nf-content {
  text-align: center;
  position: relative;
  z-index: 1;
  animation: cardSlideUp 0.8s var(--ease-out) both;
}

@keyframes cardSlideUp {
  from { opacity: 0; transform: translateY(28px); }
  to { opacity: 1; transform: translateY(0); }
}

.nf-badge {
  font-size: 48px;
  margin-bottom: 16px;
  animation: gentleFloat 4s ease-in-out infinite;
}

.nf-code {
  font-size: 120px;
  font-weight: 800;
  color: rgba(196, 68, 58, 0.15);
  line-height: 1;
  letter-spacing: -0.04em;
  margin-bottom: 8px;
}

.nf-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
  letter-spacing: -0.01em;
}

.nf-desc {
  font-size: 15px;
  color: var(--text-muted);
  margin-bottom: 36px;
}

.nf-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.nf-copyright {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: var(--text-muted);
  opacity: 0.5;
}
</style>
