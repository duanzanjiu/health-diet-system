<template>
  <div class="dashboard-page">
    <!-- 欢迎横幅 -->
    <section class="welcome-banner stagger-item" style="animation-delay: 0.1s">
      <div class="banner-bg">
        <div class="banner-orb banner-orb--1"></div>
        <div class="banner-orb banner-orb--2"></div>
        <div class="banner-pattern"></div>
      </div>
      <div class="welcome-body">
        <div class="welcome-text">
          <p class="welcome-greeting">{{ greeting }}，</p>
          <h1 class="font-display">{{ userStore.userInfo?.nickname || '管理员' }}</h1>
          <p class="welcome-desc">让我们一起规划今天的健康饮食方案</p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" size="large" round class="btn-accent"
                     @click="$router.push('/admin/recommend/ai')">
            <el-icon><MagicStick /></el-icon> 获取今日推荐
          </el-button>
          <el-button size="large" round class="btn-outline"
                     @click="$router.push('/admin/diet-plan/list')">
            <el-icon><Document /></el-icon> 清单管理
          </el-button>
        </div>
      </div>
      <div class="time-badge">
        <span class="time-date font-display">{{ todayDate }}</span>
        <span class="time-day">{{ todayDay }}</span>
      </div>
    </section>

    <!-- 统计卡片行 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="(stat, i) in stats" :key="i">
        <div class="stat-card stagger-item" :style="{ animationDelay: `${0.2 + i * 0.1}s` }">
          <div class="stat-body">
            <div class="stat-icon-wrap" :style="{ background: stat.bg, color: stat.color }">
              <el-icon :size="22"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value font-display">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>
          <div class="stat-trend" :style="{ color: stat.color }">
            {{ stat.trend }}
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <section class="page-card chart-card stagger-item" style="animation-delay: 0.6s">
          <div class="section-header">
            <h3 class="section-title font-display">近7天推荐趋势</h3>
          </div>
          <div ref="trendChartRef" class="chart-box"></div>
        </section>
      </el-col>
      <el-col :span="10">
        <section class="page-card chart-card stagger-item" style="animation-delay: 0.7s">
          <div class="section-header">
            <h3 class="section-title font-display">食材分类分布</h3>
          </div>
          <div ref="pieChartRef" class="chart-box"></div>
        </section>
      </el-col>
    </el-row>

    <!-- 快速入口 + 公告 -->
    <el-row :gutter="20" class="equal-row">
      <el-col :span="14">
        <section class="quick-section page-card stagger-item" style="animation-delay: 0.8s">
          <div class="section-header">
            <h3 class="section-title font-display">快速入口</h3>
            <span class="section-subtitle">常用功能一键直达</span>
          </div>
          <div class="grid-items">
            <div class="grid-item" v-for="(item, i) in quickLinks" :key="i"
                 @click="$router.push(item.path)">
              <div class="grid-icon" :style="{ background: item.bg }">
                <el-icon :size="24" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
              </div>
              <span class="grid-label">{{ item.label }}</span>
              <span class="grid-arrow">→</span>
            </div>
          </div>
        </section>
      </el-col>
      <el-col :span="10">
        <section class="notice-card page-card stagger-item" style="animation-delay: 0.9s">
          <div class="section-header">
            <h3 class="section-title font-display">系统公告</h3>
          </div>
          <div class="notice-items">
            <div class="notice-item" v-for="(n, i) in notices" :key="i">
              <span class="notice-tag" :style="{ background: typeStyleMap[n.noticeType]?.bg, color: typeStyleMap[n.noticeType]?.color }">
                {{ typeStyleMap[n.noticeType]?.label || n.noticeType }}
              </span>
              <span class="notice-text">{{ n.title }}</span>
            </div>
            <div v-if="notices.length === 0" class="notice-empty">暂无公告</div>
          </div>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboardData, getDashboardTrend } from '@/api/dashboard'
import * as echarts from 'echarts'
import { getNoticeActive } from '@/api/notice'


const userStore = useUserStore()
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayDate = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const todayDay = computed(() => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date().getDay()]
})

const stats = reactive([
  { label: '食材总数', value: '--', icon: 'Apple', color: '#3d8b47', bg: 'rgba(61,139,71,0.08)', trend: '' },
  { label: '食谱总数', value: '--', icon: 'Dish', color: '#3a7ec4', bg: 'rgba(58,126,196,0.08)', trend: '' },
  { label: '今日推荐', value: '--', icon: 'MagicStick', color: '#c4933a', bg: 'rgba(196,147,58,0.08)', trend: '' },
  { label: '注册用户', value: '--', icon: 'User', color: '#8b5e3d', bg: 'rgba(139,94,61,0.08)', trend: '' }
])

const quickLinks = reactive([
  { label: '食材分类', icon: 'Grid', path: '/admin/food/category', bg: 'var(--c-forest-50)', color: 'var(--c-forest-600)' },
  { label: '食材信息', icon: 'Apple', path: '/admin/food/list', bg: '#eef6ff', color: '#3a7ec4' },
  { label: 'AI推荐', icon: 'MagicStick', path: '/admin/recommend/ai', bg: 'var(--c-gold-100)', color: 'var(--c-gold-500)' },
  { label: '食谱管理', icon: 'Dish', path: '/admin/food/recipe', bg: '#fef2f0', color: '#c4453a' },
  { label: '清单管理', icon: 'Document', path: '/admin/diet-plan/list', bg: '#f0f0fe', color: '#6b5ec4' },
  { label: '健康档案', icon: 'FirstAidKit', path: '/admin/health/list', bg: '#f4f2ec', color: '#8b7a5e' }
])

const notices = ref([])

const typeStyleMap = {
  feature: { label: '新功能', bg: 'rgba(61,139,71,0.08)', color: '#3d8b47' },
  notice: { label: '通知', bg: 'rgba(58,126,196,0.08)', color: '#3a7ec4' },
  tip: { label: '提示', bg: 'rgba(196,147,58,0.08)', color: '#c4933a' },
  warning: { label: '警告', bg: 'rgba(196,68,58,0.08)', color: '#c4453a' }
}

async function loadStats() {
  const res = await getDashboardData()
  const data = res.data || {}
  stats[0].value = data.foodCount || '--'
  stats[1].value = data.recipeCount || '--'
  stats[2].value = data.todayRecommendCount || '--'
  stats[3].value = data.userCount || '--'
  stats[2].trend='今日'
  stats[3].trend=`今日+${data.todayNewUsers}`
}

async function loadCharts() {
  const res = await getDashboardTrend()
  const data = res.data || {}
  initTrendChart(data.recommendTrend || [])
  initPieChart(data.categoryDistribution || [])
 // handleResize()
}

function initTrendChart(trend) {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: trend.map(i => i.date),
      axisLine: { lineStyle: { color: '#e0ddd5' } },
      axisLabel: { color: '#8c8578' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0eee8' } },
      axisLabel: { color: '#8c8578' }
    },
    series: [{
      type: 'line',
      data: trend.map(i => i.count),
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#3d8b47', width: 3 },
      itemStyle: { color: '#3d8b47' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(61,139,71,0.2)' },
          { offset: 1, color: 'rgba(61,139,71,0.02)' }
        ])
      }
    }]
  })
}

function initPieChart(dist) {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  const colors = ['#3d8b47', '#c4933a', '#3a7ec4', '#c4453a', '#6b5ec4', '#8b5e3d', '#2d9596', '#e07c24']
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    color: colors,
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '50%'],
      label: { fontSize: 12, color: '#6b6560' },
      emphasis: {
        label: { fontSize: 14, fontWeight: 'bold' }
      },
      data: dist
    }]
  })
}

function handleResize() {
  trendChart?.resize()
  pieChart?.resize()
}

async function loadNotices() {
  const res = await getNoticeActive()
  notices.value = res.data || []
}

onMounted(() => {
  loadStats()
  loadCharts()
  loadNotices()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  max-width: 1200px;
}

/* ─── Welcome Banner ─── */
.welcome-banner {
  background: var(--c-forest-900);
  border-radius: var(--r-xl);
  padding: 44px 48px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.banner-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
}

.banner-orb--1 {
  width: 320px; height: 320px;
  top: -30%; right: -5%;
  background: radial-gradient(circle, rgba(196, 147, 58, 0.15) 0%, transparent 70%);
  animation: gentleFloat 8s ease-in-out infinite;
}

.banner-orb--2 {
  width: 200px; height: 200px;
  bottom: -20%; left: 30%;
  background: radial-gradient(circle, rgba(61, 139, 71, 0.12) 0%, transparent 70%);
  animation: gentleFloat 10s ease-in-out infinite 3s;
}

.banner-pattern {
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='40' height='40' viewBox='0 0 40 40' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23ffffff' fill-opacity='0.015'%3E%3Cpath d='M0 40L40 0H20L0 20M40 40V20L20 40'/%3E%3C/g%3E%3C/svg%3E");
}

.welcome-body {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-text { position: relative; z-index: 2; }

.welcome-greeting {
  font-size: 14px;
  color: rgba(250, 249, 246, 0.4);
  font-weight: 500;
  letter-spacing: 0.06em;
  margin-bottom: 4px;
}

.welcome-text h1 {
  font-size: 38px;
  font-weight: 800;
  color: var(--c-sand-50);
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}

.welcome-desc {
  font-size: 15px;
  color: rgba(250, 249, 246, 0.45);
}

.welcome-actions {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 12px;
}

.btn-outline {
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
  color: rgba(250, 249, 246, 0.8) !important;
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.25) !important;
}

/* Time badge */
.time-badge {
  position: absolute;
  top: 20px; right: 24px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  z-index: 3;
}

.time-date {
  font-size: 13px;
  font-weight: 600;
  color: rgba(250, 249, 246, 0.5);
  letter-spacing: 0.02em;
}

.time-day {
  font-size: 11px;
  color: rgba(250, 249, 246, 0.3);
  letter-spacing: 0.06em;
}

/* ─── Stats ─── */
.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  padding: 22px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrap {
  width: 48px; height: 48px;
  border-radius: var(--r-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform var(--dur-fast) ease;
}

.stat-card:hover .stat-icon-wrap {
  transform: scale(1.08);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  display: block;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
  margin-top: 4px;
  display: block;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.stat-trend {
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

/* ─── Equal Height Row ─── */
.equal-row {
  display: flex !important;
  align-items: stretch;
}

.equal-row > .el-col > .page-card {
  height: 100%;
  box-sizing: border-box;
}

/* ─── Charts ─── */
.chart-row {
  margin-bottom: 4px;
}

.chart-card {
  padding: 28px;
}

.chart-box {
  width: 100%;
  height: 280px;
}

/* ─── Section Headers ─── */
.section-header {
  margin-bottom: 20px;
}

.section-header .section-subtitle {
  margin-top: 4px;
}

/* ─── Quick Grid ─── */
.quick-section {
  padding: 28px;
}

.grid-items {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.grid-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: var(--r-md);
  cursor: pointer;
  transition: all var(--dur-fast) var(--ease-out);
  border: 1px solid transparent;
  position: relative;
}

.grid-item:hover {
  background: var(--c-sand-100);
  border-color: var(--border-light);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.grid-icon {
  width: 44px; height: 44px;
  border-radius: var(--r-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.grid-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  flex: 1;
}

.grid-arrow {
  font-size: 14px;
  color: var(--text-muted);
  opacity: 0;
  transform: translateX(-4px);
  transition: all var(--dur-fast) ease;
}

.grid-item:hover .grid-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* ─── Overview Card ─── */
.overview-card {
  padding: 28px;
  margin-bottom: 20px;
}

.overview-items {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
}

.overview-item:last-child {
  border-bottom: none;
}

.overview-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.overview-label {
  flex: 1;
  font-size: 13px;
  color: var(--text-secondary);
}

.overview-value {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.overview-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 14px;
  background: var(--c-forest-50);
  border-radius: var(--r-md);
  font-size: 12px;
  color: var(--c-forest-600);
}

.overview-tip .el-icon {
  font-size: 14px;
}

/* ─── Notice Card ─── */
.notice-card {
  padding: 28px;
}

.notice-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notice-tag {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--r-xs);
  flex-shrink: 0;
  letter-spacing: 0.02em;
}

.notice-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.notice-empty {
  font-size: 13px;
  color: var(--text-muted);
  text-align: center;
  padding: 20px 0;
}
</style>
