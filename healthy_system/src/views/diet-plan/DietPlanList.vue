<template>
    <div class="diet-plan-admin-page">
        <div class="page-heading stagger-item" style="animation-delay: 0.05s">
            <h2 class="font-display section-title">清单管理</h2>
            <p class="section-subtitle">查看和管理所有用户的饮食清单</p>
        </div>
        <!-- 统计条 -->
        <div class="stat-strip stagger-item" style="animation-delay: 0.1s">
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(61,139,71,0.08); color: #3d8b47;">
                <el-icon :size="22"><Document /></el-icon>
                </div>
                <div class="strip-info">
                <span class="strip-val font-display">{{ stats.totalCount }}</span>
                <span class="strip-label">清单总数</span>
                </div>
            </div>
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(58,126,196,0.08); color: #3a7ec4;">
                <el-icon :size="22"><User /></el-icon>
                </div>
                <div class="strip-info">
                <span class="strip-val font-display">{{ stats.userCount }}</span>
                <span class="strip-label">涉及用户</span>
                </div>
            </div>
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(196,147,58,0.08); color: #c4933a;">
                <el-icon :size="22"><Check /></el-icon>
                </div>
                <div class="strip-info">
                <span class="strip-val font-display">{{ stats.completedCount }}</span>
                <span class="strip-label">已完成</span>
                </div>
            </div>
            <div class="strip-card">
                <div class="strip-icon" style="background: rgba(139,94,61,0.08); color: #8b5e3d;">
                <el-icon :size="22"><TrendCharts /></el-icon>
                </div>
                <div class="strip-info">
                <span class="strip-val font-display">{{ stats.avgCalories }}</span>
                <span class="strip-label">平均热量(kcal)</span>
                </div>
            </div>
        </div>
        <!-- 筛选 + 表格 -->
        <div class="page-card stagger-item" style="animation-delay: 0.15s">
            <div class="filter-row">
                <el-date-picker v-model="filterDate" type="date" placeholder="选择日期"
                                value-format="YYYY-MM-DD" clearable @change="loadData" style="width: 180px;" />
                <el-select v-model="filterSource" placeholder="来源" clearable style="width: 120px;" @change="loadData">
                <el-option label="手动创建" value="manual" />
                <el-option label="AI推荐" value="recommend" />
                </el-select>
                <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable
                        style="width: 200px;" @keyup.enter="loadData">
                <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-button type="primary" @click="loadData">查询</el-button>
            </div>
            <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;"
                :header-cell-style="{ background: 'var(--c-sand-50)', fontWeight: 700 }">
                <el-table-column prop="planDate" label="日期" width="120" />
                <el-table-column label="用户" min-width="120">
                    <template #default="{ row }">
                        <span>{{ row.nickname || row.username || '--' }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="来源" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.source === 'recommend' ? 'success' : 'primary'" size="small" effect="plain">
                        {{ row.source === 'recommend' ? 'AI推荐' : '手动' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="总热量" width="110" align="right">
                    <template #default="{ row }">
                        <span class="font-display" style="font-weight: 700; color: var(--c-forest-600);">
                        {{ row.totalCalories || 0 }}
                        </span>
                        <span style="font-size: 11px; color: var(--text-muted);"> kcal</span>
                    </template>
                </el-table-column>
                <el-table-column label="匹配度" width="90" align="center">
                    <template #default="{ row }">
                        <span v-if="row.matchScore != null" class="match-badge" :class="scoreClass(row.matchScore)">
                        {{ row.matchScore }}%
                        </span>
                        <span v-else style="color: var(--text-muted);">--</span>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="90" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                        {{ row.status === 1 ? '已完成' : '进行中' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="食物概要" min-width="200">
                    <template #default="{ row }">
                        <span class="food-summary">
                        {{ (row.items || []).map(i => i.foodName).filter(Boolean).join('、') || '暂无' }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
                        <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination-wrapper">
                <el-pagination background layout="total, prev, pager, next, sizes"
                            :total="total" :page-size="pageSize" :current-page="pageNum"
                            :page-sizes="[10, 20, 50]"
                            @current-change="v => { pageNum = v; loadData() }"
                            @size-change="v => { pageSize = v; pageNum = 1; loadData() }" />
            </div>
        </div>
        <!-- 详情弹窗 -->
        <el-dialog v-model="detailVisible" title="清单详情" width="640px" destroy-on-close>
            <div v-if="detailPlan" class="detail-content">
                <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="用户">{{ detailPlan.nickname || detailPlan.username }}</el-descriptions-item>
                    <el-descriptions-item label="日期">{{ detailPlan.planDate }}</el-descriptions-item>
                    <el-descriptions-item label="总热量">{{ detailPlan.totalCalories || 0 }} kcal</el-descriptions-item>
                    <el-descriptions-item label="来源">
                        {{ detailPlan.source === 'recommend' ? 'AI推荐' : '手动创建' }}
                    </el-descriptions-item>
                </el-descriptions>
                <div class="detail-meals">
                    <div v-for="mt in ['breakfast', 'lunch', 'dinner', 'snack']" :key="mt"
                    v-show="mealItems(detailPlan.items || [], mt).length > 0"
                    class="detail-meal-card" :class="mt">
                        <div class="dm-deco"></div>
                        <div class="dm-inner">
                            <div class="dm-head">{{ mealEmoji(mt) }} {{ mealLabel(mt) }}</div>
                            <div v-for="(item, i) in mealItems(detailPlan.items || [], mt)" :key="i" class="dm-food-row">
                                <span class="dm-food-name">{{ item.foodName }}</span>
                                <span class="dm-food-amount" v-if="item.amount">{{ item.amount }}{{ item.unit || 'g' }}</span>
                                <span class="dm-food-cal">{{ item.calories || 0 }} kcal</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDietPlanList, getDietPlanDetail, deleteDietPlan } from '@/api/dietPlan'


const scoreClass = (score) => {
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-ok'
  return 'score-low'
}
const mealEmoji = (mt) => {
  const map = { breakfast: '🌅', lunch: '☀️', dinner: '🌙', snack: '🍪' }
  return map[mt] || '🍽'
}
const mealLabel = (mt) => {
    const map = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }
    return map[mt] || mt
}
const mealItems = (items, mt) => {
  return items.filter(i => i.mealType === mt)
}
const stats = reactive({ 
    totalCount: 0, 
    userCount: 0, 
    completedCount: 0, 
    avgCalories: 0 
})
const filterSource = ref('')
const filterDate = ref('')
const keyword = ref('')
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const pageNum = ref(1)
const detailVisible = ref(false)
const detailPlan = ref(null)

const loadData = async () => {
    loading.value = true
    const res = await getDietPlanList({
        source: filterSource.value,
        date: filterDate.value,
        keyword: keyword.value,
        pageNum: pageNum.value,
        pageSize: pageSize.value
    })
    loading.value = false
    if (res.code === 200) {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
        calcStats()
    }
}

//计算统计信息
const calcStats = () => {
 stats.totalCount = total.value
 const data=tableData.value
 const users=new Set(data.map(item=>item.userId))
 stats.userCount=users.size
 stats.completedCount=data.filter(r=>r.status===1).length
 const cals = data.map(r=>Number(r.totalCalories) ).filter(c=>c>0)
 stats.avgCalories=cals.length>0? Math.round (cals.reduce((a,b)=>a+b,0)/cals.length):0


}

onMounted(() => {
    loadData()
})

const viewDetail = async (row) => {
    const res = await getDietPlanDetail(row.id)
    if (res.code === 200) {
        detailPlan.value = res.data || {}
        detailVisible.value = true
    }

}
const handleDelete = async (row) => {
    const res = await deleteDietPlan(row.id)
    if (res.code === 200) {
        loadData()
    }
}
</script>
<style scoped>
.page-heading { margin-bottom: 24px; }

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
  width: 48px; height: 48px;
  border-radius: var(--r-md);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.strip-val {
  font-size: 26px; font-weight: 700; letter-spacing: -0.02em;
  color: var(--text-primary); display: block; line-height: 1;
}
.strip-label {
  font-size: 12px; color: var(--text-muted); font-weight: 500;
  margin-top: 4px; display: block; letter-spacing: 0.02em;
}

/* ─── Filter ─── */
.filter-row {
  display: flex; gap: 12px; align-items: center;
  margin-bottom: 20px; flex-wrap: wrap;
}

/* ─── Match Badge ─── */
.match-badge {
  font-size: 12px; font-weight: 700;
  padding: 2px 8px; border-radius: var(--r-xs);
}
.match-badge.score-good { background: rgba(61,139,71,0.1); color: var(--c-forest-600); }
.match-badge.score-ok { background: rgba(196,147,58,0.1); color: var(--c-gold-500); }
.match-badge.score-low { background: rgba(212,84,78,0.1); color: #d4544e; }

.food-summary {
  font-size: 12px; color: var(--text-muted);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  display: block; max-width: 200px;
}

/* ─── Detail ─── */
.detail-meals { margin-top: 20px; display: flex; flex-direction: column; gap: 12px; }
.detail-meal-card {
  border-radius: var(--r-md);
  border: 1px solid var(--border-light);
  overflow: hidden;
  background: var(--bg-card);
}
.dm-deco { height: 3px; width: 100%; }
.detail-meal-card.breakfast .dm-deco { background: linear-gradient(90deg, var(--c-gold-400), var(--c-gold-300)); }
.detail-meal-card.lunch .dm-deco { background: linear-gradient(90deg, var(--c-forest-400), var(--c-forest-300)); }
.detail-meal-card.dinner .dm-deco { background: linear-gradient(90deg, #6366f1, #818cf8); }
.detail-meal-card.snack .dm-deco { background: linear-gradient(90deg, #f97316, #fb923c); }
.dm-inner { padding: 14px; }
.dm-head { font-size: 14px; font-weight: 700; color: var(--text-primary); margin-bottom: 10px; }
.dm-food-row {
  display: flex; align-items: center; gap: 12px;
  padding: 6px 0; border-bottom: 1px dashed var(--border-light);
}
.dm-food-row:last-child { border-bottom: none; }
.dm-food-name { flex: 1; font-size: 13px; color: var(--text-secondary); font-weight: 500; }
.dm-food-amount { font-size: 12px; color: var(--text-muted); }
.dm-food-cal { font-size: 12px; color: var(--c-gold-500); font-weight: 600; min-width: 70px; text-align: right; }
</style>
