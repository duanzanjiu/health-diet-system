<template>
    <div class="user-home">
        <section class="hero-section stagger-item" style="animation-delay: 0.1s">
            <div class="hero-bg">
                <div class="hero-orb hero-orb--1"></div>
                <div class="hero-orb hero-orb--2"></div>
                <div class="hero-pattern"></div>
            </div>
            <div class="hero-content">
                <p class="hero-greeting">{{ greeting }}，{{ userStore.userInfo?.nickname || '用户' }}</p>
                <h1 class="font-display hero-title">今天也要吃得健康哦</h1>
                <p class="hero-desc">探索食材、发现食谱、获取专属 AI 饮食推荐</p>
                <div class="hero-actions">
                <el-button type="primary" size="large" round @click="$router.push('/home/recommend')">
                    <el-icon><MagicStick /></el-icon> AI 推荐
                </el-button>
                <el-button size="large" round @click="$router.push('/home/food')">
                    <el-icon><Search /></el-icon> 浏览食材
                </el-button>
                </div>
            </div>
        </section>
        <!-- 功能入口 -->
        <section class="feature-grid">
            <div class="feature-card stagger-item" v-for="(f, i) in features" :key="i"
                :style="{ animationDelay: `${0.2 + i * 0.08}s` }"
                @click="$router.push(f.path)">
                <div class="feature-icon-wrap" :style="{ background: f.bg }">
                <span class="feature-emoji">{{ f.icon }}</span>
                </div>
                <h3 class="feature-name">{{ f.title }}</h3>
                <p class="feature-desc">{{ f.desc }}</p>
                <span class="feature-arrow">→</span>
            </div>
        </section>
        <!-- 今日饮食概览 + 我的状态 -->
        <el-row :gutter="20">
            <el-col :span="12">
                <section class="page-card info-card stagger-item" style="animation-delay: 0.6s">
                    <div class="section-header">
                        <h3 class="section-title font-display">今日饮食概览</h3>
                    </div>
                    <div class="calorie-progress">
                        <div class="calorie-info">
                            <span class="calorie-current font-display">{{ overview.todayCalories }}</span>
                            <span class="calorie-unit">kcal</span>
                            <span class="calorie-sep">/</span>
                            <span class="calorie-target">{{ overview.targetCalories > 0 ? overview.targetCalories : '--' }} kcal</span>
                        </div>
                        <el-progress :percentage="caloriePercent" :stroke-width="10"
                         :color="caloriePercent > 100 ? '#c4453a' : '#3d8b47'"
                         :show-text="false" style="margin: 12px 0;" />
                        <div class="overview-stats">
                            <div class="overview-stat-item">
                                <span class="overview-stat-num font-display">{{ overview.todayPlanCount }}</span>
                                <span class="overview-stat-label">饮食清单</span>
                            </div>
                            <div class="overview-stat-item">
                                <span class="overview-stat-num font-display">{{ overview.todayFoodCount }}</span>
                                <span class="overview-stat-label">食物记录</span>
                            </div>
                            <div class="overview-stat-item">
                                <span class="overview-stat-num font-display">{{ overview.planTotal }}</span>
                                <span class="overview-stat-label">历史清单</span>
                            </div>
                        </div>
                    </div>
                </section>
            </el-col>
            <el-col :span="12">
                <section class="page-card info-card stagger-item" style="animation-delay: 0.7s">
                    <div class="section-header">
                        <h3 class="section-title font-display">我的状态</h3>
                    </div>
                    <div class="status-items">
                        <div class="status-row">
                            <span class="status-label">今日饮食清单</span>
                            <span class="status-value">{{ overview.todayPlanCount > 0 ? overview.todayPlanCount + ' 份' : '暂无' }}</span>
                        </div>
                        <div class="status-row">
                            <span class="status-label">推荐记录</span>
                            <span class="status-value">{{ overview.recommendCount }} 条</span>
                        </div>
                        <div class="status-row">
                            <span class="status-label">健康档案</span>
                            <span :class="['status-value', overview.hasProfile ? 'tag-ok' : 'tag-warn']">
                                {{ overview.hasProfile ? '已完善' : '待完善' }}
                            </span>
                        </div>
                    </div>
                    <div class="tips-list" style="margin-top: 16px;">
                        <div class="tip-item" v-for="(tip, i) in tips" :key="i">
                        <span class="tip-icon">{{ tip.icon }}</span>
                        <span class="tip-text">{{ tip.text }}</span>
                        </div>
                    </div>
                </section>
            </el-col>
        </el-row>
    </div>
</template>
<script setup>
import { computed, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboardOverviewData } from '@/api/dashboard'


onMounted(async () => {
   const res = await getDashboardOverviewData()
   const d = res.data
   overview.todayCalories = d.todayCalories
   overview.targetCalories = d.targetCalories
   overview.todayPlanCount = d.todayPlanCount
   overview.todayFoodCount = d.todayFoodCount
   overview.planTotal = d.planTotal
   overview.recommendCount = d.recommendCount
   overview.hasProfile = d.hasProfile
})

const userStore = useUserStore()

const overview = reactive({
  todayCalories: 0,
  targetCalories: 0,
  todayPlanCount: 0,
  todayFoodCount: 0,
  planTotal: 0,
  recommendCount: 0,
  hasProfile: false
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const features = [
  { title: '食材浏览', desc: '查看各类食材及营养成分', icon: '🥦', path: '/home/food', bg: 'var(--c-forest-50)' },
  { title: '食谱推荐', desc: '精选搭配，科学营养', icon: '🍲', path: '/home/recipe', bg: '#eef6ff' },
  { title: 'AI 推荐', desc: '通义千问为你定制', icon: '🤖', path: '/home/recommend', bg: 'var(--c-gold-100)' },
  { title: '我的饮食', desc: '管理每日饮食清单', icon: '📋', path: '/home/diet-plan', bg: '#f0f0fe' }
]

const tips = [
  { icon: '💧', text: '每天至少饮用 8 杯水' },
  { icon: '🥗', text: '多吃蔬菜水果，保持膳食纤维摄入' },
  { icon: '🏃', text: '适度运动有助于新陈代谢' },
  { icon: '😴', text: '充足睡眠是健康的基础' }
]

// 计算今日卡路里进度
const caloriePercent = computed(() => {
  if (overview.targetCalories <= 0) return 0
  return Math.min(Math.round((overview.todayCalories / overview.targetCalories) * 100), 150)
})


</script>
<style scoped>
.user-home {
  max-width: 960px;
  margin: 0 auto;
}

/* Hero */
.hero-section {
  position: relative;
  border-radius: var(--r-xl);
  padding: 48px 44px;
  margin-bottom: 28px;
  overflow: hidden;
  background: var(--c-forest-900);
}

.hero-bg { position: absolute; inset: 0; }

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
}

.hero-orb--1 {
  width: 300px; height: 300px; top: -20%; right: -5%;
  background: radial-gradient(circle, rgba(196,147,58,0.15) 0%, transparent 70%);
  animation: gentleFloat 8s ease-in-out infinite;
}

.hero-orb--2 {
  width: 200px; height: 200px; bottom: -15%; left: 10%;
  background: radial-gradient(circle, rgba(61,139,71,0.12) 0%, transparent 70%);
  animation: gentleFloat 10s ease-in-out infinite 2s;
}

.hero-pattern {
  position: absolute; inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='40' height='40' viewBox='0 0 40 40' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23ffffff' fill-opacity='0.015'%3E%3Cpath d='M0 40L40 0H20L0 20M40 40V20L20 40'/%3E%3C/g%3E%3C/svg%3E");
}

.hero-content { position: relative; z-index: 1; }

.hero-greeting {
  font-size: 14px;
  color: rgba(250,249,246,0.45);
  margin-bottom: 8px;
  letter-spacing: 0.04em;
}

.hero-title {
  font-size: 32px;
  font-weight: 800;
  color: var(--c-sand-50);
  letter-spacing: -0.02em;
  margin-bottom: 10px;
}

.hero-desc {
  font-size: 15px;
  color: rgba(250,249,246,0.4);
  margin-bottom: 28px;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

/* Feature Grid */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.feature-card {
  padding: 24px 20px;
  border-radius: var(--r-lg);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--dur-normal) var(--ease-out);
  position: relative;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--c-forest-200);
}

.feature-icon-wrap {
  width: 48px; height: 48px;
  border-radius: var(--r-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.feature-emoji { font-size: 24px; }

.feature-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

.feature-arrow {
  position: absolute;
  top: 20px; right: 20px;
  font-size: 16px;
  color: var(--text-muted);
  opacity: 0;
  transform: translateX(-4px);
  transition: all var(--dur-fast) ease;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* Info Cards */
.info-card {
  padding: 24px;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.tip-icon { font-size: 18px; }

.status-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
}

.status-row:last-child { border-bottom: none; }

.status-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.status-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.tag-warn {
  background: rgba(196,147,58,0.1);
  color: var(--c-gold-500);
  padding: 2px 8px;
  border-radius: var(--r-xs);
  font-size: 12px;
}

.tag-ok {
  background: rgba(61,139,71,0.1);
  color: #3d8b47;
  padding: 2px 8px;
  border-radius: var(--r-xs);
  font-size: 12px;
}

/* Calorie Progress */
.calorie-info {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.calorie-current {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.calorie-unit {
  font-size: 14px;
  color: var(--text-muted);
  font-weight: 500;
}

.calorie-sep {
  font-size: 18px;
  color: var(--border);
  margin: 0 4px;
}

.calorie-target {
  font-size: 14px;
  color: var(--text-muted);
}

.overview-stats {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}

.overview-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.overview-stat-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}
</style>
