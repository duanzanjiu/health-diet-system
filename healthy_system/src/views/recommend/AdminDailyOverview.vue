<template>
    <div class="admin-daily-page">
        <div class="page-heading stagger-item" style="animation-delay: 0.05s">
            <h2 class="font-display section-title">每日推荐总览</h2>
            <p class="section-subtitle">查看今日系统为所有用户生成的饮食推荐方案</p>
        </div>

        <!-- 统计卡片 -->
        <div class="stat-strip stagger-item" style="animation-delay: 0.1s">
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(61,139,71,0.08); color: #3d8b47;">
                    <el-icon :size="22">
                        <MagicStick />
                    </el-icon>
                </div>
                <div class="strip-info">
                    <span class="strip-val font-display">{{ stats.todayCount }}</span>
                    <span class="strip-label">今日推荐数</span>
                </div>
            </div>
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(58,126,196,0.08); color: #3a7ec4;">
                    <el-icon :size="22">
                        <User />
                    </el-icon>
                </div>
                <div class="strip-info">
                    <span class="strip-val font-display">{{ stats.userCount }}</span>
                    <span class="strip-label">涉及用户</span>
                </div>
            </div>
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(196,147,58,0.08); color: #c4933a;">
                    <el-icon :size="22">
                        <ChatDotRound />
                    </el-icon>
                </div>
                <div class="strip-info">
                    <span class="strip-val font-display">{{ stats.feedbackCount }}</span>
                    <span class="strip-label">已反馈</span>
                </div>
            </div>
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(139,94,61,0.08); color: #8b5e3d;">
                    <el-icon :size="22">
                        <TrendCharts />
                    </el-icon>
                </div>
                <div class="strip-info">
                    <span class="strip-val font-display">{{ stats.avgCalories }}</span>
                    <span class="strip-label">平均热量(kcal)</span>
                </div>
            </div>
        </div>

        <!-- 筛选栏 -->
        <div class="page-card stagger-item" style="animation-delay: 0.15s">
            <div class="filter-row">
                <el-date-picker v-model="dateFilter" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" clearable
                    @change="loadData" style="width: 180px;" />
                <el-select v-model="sourceFilter" placeholder="来源" clearable style="width: 120px;" @change="loadData">
                    <el-option label="AI推荐" value="AI" />
                    <el-option label="规则推荐" value="RULE" />
                </el-select>
                <el-button type="primary" @click="loadData">查询</el-button>
            </div>
            <!-- 推荐卡片列表 -->
            <div v-loading="loading" class="record-cards">
                <div v-if="!loading && tableData.length === 0" class="empty-block">
                    <el-empty description="暂无推荐记录" />
                </div>
                <div v-for="record in tableData" :key="record.id" class="rec-card">
                    <div class="rec-card-left">
                        <div class="rec-avatar">
                            <el-avatar :size="40" :src="record.avatar ? record.avatar : undefined">
                                {{ (record.nickname || record.username || '?').charAt(0) }}
                            </el-avatar>
                        </div>
                        <div class="rec-user-info">
                            <span class="rec-nickname">{{ record.nickname || record.username }}</span>
                            <div class="rec-meta">
                                <el-tag :type="record.source === 'AI' ? 'success' : 'info'" size="small" effect="plain">
                                    {{ record.source }}
                                </el-tag>
                                <span class="rec-time">{{ record.recommendDate }}</span>
                                <el-tag v-if="record.feedback" :type="fbTagType(record.feedback)" size="small">
                                    {{ fbLabel(record.feedback) }}
                                </el-tag>
                            </div>
                        </div>
                    </div>
                    <div class="rec-card-meals">
                        <div v-for="meal in getMeals(record.content)" :key="meal.key" class="rec-meal-chip">
                            <span class="rec-meal-emoji">{{ meal.emoji }}</span>
                            <div class="rec-meal-body">
                                <span class="rec-meal-name">{{ meal.data?.name || '--' }}</span>
                                <span class="rec-meal-cal">{{ meal.data?.calories || 0 }} kcal</span>
                            </div>
                        </div>
                    </div>
                    <div class="rec-card-total">
                        <span class="rec-total-num font-display">{{ getTotalCalories(record.content) }}</span>
                        <span class="rec-total-unit">kcal</span>
                    </div>
                    <div class="rec-card-actions">
                        <el-button type="primary" link size="small" @click="viewDetail(record)">
                            <el-icon>
                                <View />
                            </el-icon> 详情
                        </el-button>
                    </div>
                </div>
            </div>
            <div class="pagination-wrapper">
                <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :page-size="pageSize"
                    :current-page="pageNum" :page-sizes="[10, 20, 50]"
                    @current-change="(v) => { pageNum = v; loadData() }"
                    @size-change="(v) => { pageSize = v; pageNum = 1; loadData() }" />
            </div>
        </div>

        <!-- 详情弹窗 -->
        <el-dialog v-model="detailVisible" title="推荐方案详情" width="680px" destroy-on-close>
            <div v-if="detailData" class="detail-content">
                <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="用户">{{ detailData.nickname || detailData.username
                        }}</el-descriptions-item>
                    <el-descriptions-item label="推荐日期">{{ detailData.recommendDate }}</el-descriptions-item>
                    <el-descriptions-item label="来源">{{ detailData.source }}</el-descriptions-item>
                    <el-descriptions-item label="反馈">{{ fbLabel(detailData.feedback) || '未反馈' }}</el-descriptions-item>
                </el-descriptions>
                <div class="detail-meals" v-if="detailParsed">
                    <h4 style="margin: 20px 0 14px; font-weight: 700; font-size: 15px;">推荐内容</h4>
                    <el-row :gutter="12" class="meal-row-eq">
                        <el-col :span="8" v-for="meal in getDetailMeals()" :key="meal.key" class="meal-col-eq">
                            <div class="detail-meal-card" :class="meal.key">
                                <div class="dm-deco"></div>
                                <div class="dm-inner">
                                    <div class="dm-head">{{ meal.emoji }} {{ meal.label }}</div>
                                    <p class="dm-name">{{ meal.data?.name || '--' }}</p>
                                    <p class="dm-cal">{{ meal.data?.calories || 0 }} kcal</p>
                                    <div class="dm-foods" v-if="meal.data?.foods?.length">
                                        <el-tag v-for="(f, i) in meal.data.foods" :key="i" size="small" type="info"
                                            effect="plain">{{ f }}</el-tag>
                                    </div>
                                    <p class="dm-desc" v-if="meal.data?.description">{{ meal.data.description }}</p>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                    <div v-if="detailParsed.nutritionAdvice" class="dm-advice">
                        <el-icon style="color: var(--c-gold-500); flex-shrink: 0;"><InfoFilled /></el-icon>
                        {{ detailParsed.nutritionAdvice }}
                    </div>
                </div>
                <div v-else class="raw-json">
                    <pre>{{ detailData.content }}</pre>
                </div>
            </div>
        </el-dialog>
    </div>
</template>
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getAllRecommend } from '@/api/recommend'

onMounted(() => {
    loadData()
})

const loadData = async () => {
    loading.value = true
    const res = await getAllRecommend({
        recommendDate: dateFilter.value,
        source: sourceFilter.value,
        pageNum: pageNum.value,
        pageSize: pageSize.value
    })
    if (res.code === 200) {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
    }
    loading.value = false
    calcStats()
 }

const calcStats = () => {

 stats.todayCount = total.value
 const data=tableData.value
 const users=new Set(data.map(item=>item.userId))
 stats.userCount=users.size
 data.filter(r=>r.feedback).length
 stats.feedbackCount=data.filter(r=>r.feedback).length
 const cals = data.map(r=>getTotalCalories(r.content)).filter(c=>c>0)
 stats.avgCalories=cals.length>0? Math.round (cals.reduce((a,b)=>a+b,0)/cals.length):0


}

const stats = reactive({
    todayCount: 0,
    userCount: 0,
    feedbackCount: 0,
    avgCalories: 0
})

// 日期处理方法
const formatDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

const dateFilter = ref(formatDate(new Date()))
const sourceFilter = ref('')
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const pageNum = ref(1)
const tableData = ref([])

// 解析推荐内容，提取三餐信息
const getMeals = (content) => {
    try {
        const c = JSON.parse(content)
        return [
            { key: 'breakfast', emoji: '🌅', data: c.breakfast },
            { key: 'lunch', emoji: '☀️', data: c.lunch },
            { key: 'dinner', emoji: '🌙', data: c.dinner }
        ]
    } catch { return [] }
}
// 解析推荐内容，提取总卡路里
const getTotalCalories = (content) => {
    try {
        const c = JSON.parse(content)
        return c.totalCalories || 0
    } catch { return 0 }
}
// 解析反馈类型，返回标签文本
const fbLabel = (fb) => {
    const map = { like: '喜欢', dislike: '不喜欢', adopted: '已采用', ignored: '已忽略' }
    return map[fb] || ''
}
// 解析反馈类型，返回标签类型
const fbTagType = (fb) => {
  const map = { like: 'success', dislike: 'danger', adopted: '', ignored: 'info' }
  return map[fb] || 'info'
}
// 查看详情
const viewDetail = (record) => {
    detailData.value = record
    detailVisible.value = true
    console.log(record)
}

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref(null)
const detailParsed = computed(() => {
    if (!detailData.value?.content) return null
    try { return JSON.parse(detailData.value.content) } catch { return null }
})
const getDetailMeals = () => {
    const c = detailParsed.value
    if (!c) return []
    return [
        { key: 'breakfast', label: '早餐', emoji: '🌅', data: c.breakfast },
        { key: 'lunch', label: '午餐', emoji: '☀️', data: c.lunch },
        { key: 'dinner', label: '晚餐', emoji: '🌙', data: c.dinner }
    ]
}
</script>
<style scoped>
.page-heading {
    margin-bottom: 24px;
}

/* ─── Stat Strip ─── */
.stat-strip {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 24px;
}

.strip-card {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--r-lg);
    padding: 22px 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    transition: all var(--dur-fast) var(--ease-out);
}

.strip-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
    border-color: var(--border);
}

.strip-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--r-md);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.strip-val {
    font-size: 26px;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--text-primary);
    display: block;
    line-height: 1;
}

.strip-label {
    font-size: 12px;
    color: var(--text-muted);
    font-weight: 500;
    margin-top: 4px;
    display: block;
    letter-spacing: 0.02em;
}

/* ─── Filter ─── */
.filter-row {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: 24px;
    flex-wrap: wrap;
}

/* ─── Record Cards ─── */
.record-cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 200px;
}

.rec-card {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 18px 24px;
    border: 1px solid var(--border-light);
    border-radius: var(--r-lg);
    background: var(--bg-card);
    transition: all var(--dur-fast) var(--ease-out);
}

.rec-card:hover {
    border-color: var(--border);
    box-shadow: var(--shadow-sm);
}

.rec-card-left {
    display: flex;
    align-items: center;
    gap: 14px;
    min-width: 180px;
}

.rec-nickname {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    display: block;
    margin-bottom: 4px;
}

.rec-meta {
    display: flex;
    align-items: center;
    gap: 8px;
}

.rec-time {
    font-size: 12px;
    color: var(--text-muted);
}

.rec-card-meals {
    flex: 1;
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
}

.rec-meal-chip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 14px;
    background: var(--c-sand-50);
    border: 1px solid var(--border-light);
    border-radius: var(--r-md);
    min-width: 140px;
}

.rec-meal-emoji {
    font-size: 20px;
}

.rec-meal-name {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
    display: block;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100px;
}

.rec-meal-cal {
    font-size: 11px;
    color: var(--c-gold-500);
    font-weight: 600;
}

.rec-card-total {
    display: flex;
    align-items: baseline;
    gap: 4px;
    min-width: 100px;
    justify-content: flex-end;
}

.rec-total-num {
    font-size: 22px;
    font-weight: 700;
    color: var(--c-forest-600);
}

.rec-total-unit {
    font-size: 12px;
    color: var(--text-muted);
}

.rec-card-actions {
    min-width: 60px;
    text-align: right;
}

/* ─── Empty ─── */
.empty-block {
    padding: 40px 0;
}

/* ─── Detail Modal ─── */
.meal-row-eq {
    display: flex;
    flex-wrap: wrap;
}

.meal-col-eq {
    display: flex;
    flex-direction: column;
}

.detail-meal-card {
    border-radius: var(--r-md);
    border: 1px solid var(--border-light);
    margin-bottom: 10px;
    overflow: hidden;
    background: var(--bg-card);
    flex: 1;
    display: flex;
    flex-direction: column;
}

.dm-deco {
    height: 3px;
    width: 100%;
}

.detail-meal-card.breakfast .dm-deco {
    background: linear-gradient(90deg, var(--c-gold-400), var(--c-gold-300));
}

.detail-meal-card.lunch .dm-deco {
    background: linear-gradient(90deg, var(--c-forest-400), var(--c-forest-300));
}

.detail-meal-card.dinner .dm-deco {
    background: linear-gradient(90deg, #6366f1, #818cf8);
}

.dm-inner {
    padding: 14px;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.dm-head {
    font-size: 14px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 6px;
}

.dm-name {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 4px;
}

.dm-cal {
    font-size: 12px;
    color: var(--c-gold-500);
    font-weight: 600;
    margin-bottom: 8px;
}

.dm-foods {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-bottom: 6px;
}

.dm-desc {
    font-size: 12px;
    color: var(--text-muted);
}

.dm-advice {
    margin-top: 14px;
    padding: 12px 14px;
    background: var(--c-gold-100);
    border-radius: var(--r-md);
    font-size: 13px;
    color: var(--text-secondary);
    display: flex;
    align-items: flex-start;
    gap: 8px;
}

.raw-json {
    margin-top: 12px;
}

.raw-json pre {
    font-size: 12px;
    white-space: pre-wrap;
    word-break: break-all;
    color: var(--text-secondary);
}
</style>
