<template>
    <div class="home-page">
        <div class="home-card">
            <div class="home-badge">🌿</div>
            <h1 class="font-display">你好，{{ nickname }}</h1>
            <p class="home-desc">欢迎来到 HealthyDiet 用户端首页</p>
            <div class="home-menus" v-if="menuNames.length">
                <span class="home-menu-chip" v-for="name in menuNames" :key="name">{{ name }}</span>
            </div>
            <p class="home-hint">用户端页面建设中，你可以先使用上方功能菜单对应的接口联调</p>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

//从登录时保存的用户信息里取昵称
const nickname = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '用户')

//展示当前角色被分配的菜单（来自后端返回、按角色过滤过的 menuList）
const menuNames = computed(() => {
    const menus = userStore.menuList || []
    return menus.map(m => m.menuName).filter(Boolean)
})
</script>

<style scoped>
.home-page {
    width: 100%;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-page, #f6f5f2);
    padding: 40px 20px;
}

.home-card {
    width: 100%;
    max-width: 520px;
    text-align: center;
    background: #fff;
    border-radius: 16px;
    padding: 48px 40px;
    box-shadow: 0 8px 30px rgba(11, 29, 14, 0.06);
}

.home-badge {
    width: 64px;
    height: 64px;
    margin: 0 auto 20px;
    border-radius: 50%;
    background: var(--c-forest-50, #eef5ef);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
}

.home-card h1 {
    font-size: 30px;
    font-weight: 800;
    color: var(--text-primary, #1a2b1d);
    margin-bottom: 8px;
    letter-spacing: -0.02em;
}

.home-desc {
    color: var(--text-muted, #8a948c);
    font-size: 15px;
    margin-bottom: 28px;
}

.home-menus {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
    margin-bottom: 28px;
}

.home-menu-chip {
    font-size: 13px;
    color: var(--c-forest-700, #2f6a3c);
    background: var(--c-forest-50, #eef5ef);
    border-radius: 999px;
    padding: 6px 14px;
}

.home-hint {
    font-size: 13px;
    color: var(--text-muted, #a8b0a9);
}
</style>
