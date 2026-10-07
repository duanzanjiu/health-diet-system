<template>
    <div class="user-layout">
        <!-- 顶部导航栏 -->
         <header class="user-header">
            <div class="header-inner">
                <div class="header-left">
                    <h1 class="font-display header-logo" @click="$router.push('/home')">
                        Healthy<span>Diet</span>
                    </h1>
                    <nav class="header-nav">
                        <router-link v-for="item in navItems" :key="item.path" :to="item.path"
                                    class="nav-link" active-class="is-active">
                            <el-icon><component :is="item.icon" /></el-icon>
                            <span>{{ item.label }}</span>
                        </router-link>
                    </nav>
                </div>
                <div class="header-right">
                    <el-dropdown @command="handleCommand">
                        <span class="user-trigger">
                            <el-avatar :size="34" :src="userStore.userInfo?.avatar || ''"
                                        style="background: var(--c-forest-100); color: var(--c-forest-700); font-weight: 700;">
                                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                            </el-avatar>
                            <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
                            <el-icon style="font-size: 12px; color: var(--text-muted);"><ArrowDown /></el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="profile">
                                <el-icon><User /></el-icon> 个人中心
                                </el-dropdown-item>
                                <el-dropdown-item divided command="logout">
                                <el-icon><SwitchButton /></el-icon> 退出登录
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </div>
         </header>
         <!-- 页面内容 -->
        <main class="user-main">
            <router-view v-slot="{ Component }">
                <transition name="slide-fade" mode="out-in">
                <component :is="Component" />
                </transition>
            </router-view>
        </main>
        <!-- 底部 -->
        <footer class="user-footer">
            <span>HealthyDiet · 健康饮食管理系统</span>
        </footer>
    </div>
</template>
<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
const $router = useRouter()

const userStore = useUserStore()

const navItems = [
  { label: '首页', path: '/home', icon: 'HomeFilled' },
  { label: '食材浏览', path: '/home/food', icon: 'Apple' },
  { label: '食谱推荐', path: '/home/recipe', icon: 'Dish' },
  { label: '健康档案', path: '/home/health', icon: 'FirstAidKit' },
  { label: 'AI推荐', path: '/home/recommend', icon: 'MagicStick' },
  { label: '饮食清单', path: '/home/diet-plan', icon: 'Document' },
  { label: '推荐历史', path: '/home/recommend/history', icon: 'Clock' }
]

const handleCommand = (command) => {
  if (command === 'profile') {
    $router.push('/home/profile')
  } else if (command === 'logout') {
    userStore.logout()
    $router.push('/login')
  }
}
</script>
<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
}

/* ─── Header ─── */
.user-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-light);
  height: 64px;
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.header-logo {
  font-size: 22px;
  font-weight: 800;
  color: var(--c-forest-800);
  letter-spacing: -0.02em;
  cursor: pointer;
  transition: opacity var(--dur-fast) ease;
  flex-shrink: 0;
}

.header-logo:hover { opacity: 0.8; }
.header-logo span { color: var(--c-gold-500); }

/* Nav links */
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--r-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--dur-fast) ease;
}

.nav-link:hover {
  color: var(--c-forest-700);
  background: var(--c-forest-50);
}

.nav-link.is-active {
  color: var(--c-forest-700);
  background: var(--c-forest-50);
  font-weight: 600;
}

.nav-link .el-icon { font-size: 16px; }

/* Right */
.header-right {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--r-md);
  transition: background var(--dur-fast) ease;
}

.user-trigger:hover { background: var(--c-sand-100); }

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ─── Main ─── */
.user-main {
  flex: 1;
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px;
  width: 100%;
}

/* ─── Footer ─── */
.user-footer {
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: var(--text-muted);
  border-top: 1px solid var(--border-light);
}
</style>

