<template>
    <div class="record-list-page">
        <div class="page-heading stagger-item" style="animation-delay: 0.05s">
            <h2 class="font-display section-title">推荐记录管理</h2>
            <p class="section-subtitle">查看所有用户的推荐记录和反馈统计</p>
        </div>
        <div class="page-card stagger-item" style="animation-delay: 0.1s">
            <div class="search-bar">
                <el-select v-model="sourceFilter" placeholder="来源" clearable style="width: 120px;" @change="loadData">
                    <el-option label="AI" value="AI" />
                    <el-option label="RULE" value="RULE" />
                </el-select>
                <el-date-picker v-model="dateFilter" type="date" placeholder="筛选日期"
                        value-format="YYYY-MM-DD" clearable @change="loadData" style="width: 180px;" />
                <el-button type="primary" @click="loadData">查询</el-button>
            </div>
            <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="nickname" label="昵称" width="120" />
                <el-table-column prop="recommendDate" label="推荐日期" width="120" align="center" />
                <el-table-column label="来源" width="80" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.source === 'AI' ? 'success' : 'info'" size="small">{{ row.source }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="反馈" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag v-if="row.feedback" :type="fbTagType(row.feedback)" size="small">
                        {{ fbLabel(row.feedback) }}
                        </el-tag>
                        <span v-else class="text-muted">未反馈</span>
                    </template>
                </el-table-column>
                <el-table-column label="推荐摘要" min-width="250" show-overflow-tooltip>
                    <template #default="{ row }">
                        {{ getSummary(row.content) }}
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="80" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
                        {{ row.status === 0 ? '正常' : '已过期' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
                <el-table-column label="操作" width="100" fixed="right" align="center">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination-wrapper">
                <el-pagination background layout="total, prev, pager, next, sizes"
                            :total="total" :page-size="pageSize" :current-page="pageNum"
                            :page-sizes="[10, 20, 50]"
                            @current-change="(v) => { pageNum = v; loadData() }"
                            @size-change="(v) => { pageSize = v; pageNum = 1; loadData() }" />
            </div>
        </div>

        <!-- 详情弹窗 -->
        <el-dialog v-model="detailVisible" title="推荐详情" width="640px" destroy-on-close>
            <div v-if="detailData" class="detail-content">
                <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="用户">{{ detailData.nickname || detailData.username }}</el-descriptions-item>
                    <el-descriptions-item label="推荐日期">{{ detailData.recommendDate }}</el-descriptions-item>
                    <el-descriptions-item label="来源">{{ detailData.source }}</el-descriptions-item>
                    <el-descriptions-item label="反馈">{{ fbLabel(detailData.feedback) || '未反馈' }}</el-descriptions-item>
                </el-descriptions>
                <div class="detail-meals" v-if="detailParsed">
                    <h4 style="margin: 16px 0 12px; font-weight: 600;">推荐内容</h4>
                    <el-row :gutter="12" style="display: flex; flex-wrap: wrap;">
                        <el-col :span="8" v-for="meal in getDetailMeals()" :key="meal.key" style="display: flex; flex-direction: column;">
                            <div class="detail-meal-card">
                                <div class="dm-head">{{ meal.emoji }} {{ meal.label }}</div>
                                <p class="dm-name">{{ meal.data?.name || '--' }}</p>
                                <p class="dm-cal">{{ meal.data?.calories || 0 }} kcal</p>
                                <div class="dm-foods" v-if="meal.data?.foods?.length">
                                <el-tag v-for="(f, i) in meal.data.foods" :key="i" size="small" type="info" effect="plain">{{ f }}</el-tag>
                                </div>
                                <p class="dm-desc" v-if="meal.data?.description">{{ meal.data.description }}</p>
                            </div>
                        </el-col>
                    </el-row>
                    <div v-if="detailParsed.nutritionAdvice" class="dm-advice">
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
import { ref, onMounted, computed } from 'vue'
import { getAllRecommend} from '@/api/recommend'

const sourceFilter = ref('')
const dateFilter = ref('')

onMounted(() => {
    loadData()
})
const loadData = async () => {
 loading.value = true
 const res = await getAllRecommend({
    pageNum:pageNum.value,
    pageSize:pageSize.value,
    source:sourceFilter.value,
    recommendDate:dateFilter.value
 })
 tableData.value = res.data.records || []
 total.value = res.data.total || 0
 loading.value = false
}

const getSummary = (content) => {
  try {
    const c = JSON.parse(content)
    const parts = []
    if (c.breakfast?.name) parts.push('早: ' + c.breakfast.name)
    if (c.lunch?.name) parts.push('午: ' + c.lunch.name)
    if (c.dinner?.name) parts.push('晚: ' + c.dinner.name)
    return parts.join(' | ') || '无摘要'
  } catch {
    return content?.substring(0, 60) || '--'
  }
}

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const pageNum = ref(1)

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref(null)
const detailParsed = computed(() => {
  if (!detailData.value?.content) return null
  try { return JSON.parse(detailData.value.content) } catch { return null }
})
const viewDetail = (row) => {
    detailData.value = row
    detailVisible.value = true
}
// 处理反馈标签
const fbLabel = (fb) => {
  const map = { like: '喜欢', dislike: '不喜欢', adopted: '已采用', ignored: '已忽略' }
  return map[fb] || ''
}
// 处理反馈标签类型
const fbTagType = (fb) => {
  const map = { like: 'success', dislike: 'danger', adopted: '', ignored: 'info' }
  return map[fb] || 'info'
}
// 处理详情中的推荐内容
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
.page-heading { margin-bottom: 24px; }
.text-muted { font-size: 12px; color: var(--text-muted); }

.detail-meal-card {
  background: var(--c-sand-50); border-radius: var(--r-sm); padding: 14px;
  border: 1px solid var(--border-light); margin-bottom: 8px;
  flex: 1; display: flex; flex-direction: column;
}
.dm-head { font-size: 14px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px; }
.dm-name { font-size: 13px; color: var(--text-secondary); margin-bottom: 4px; }
.dm-cal { font-size: 12px; color: var(--c-gold-500); font-weight: 600; margin-bottom: 8px; }
.dm-foods { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 6px; }
.dm-desc { font-size: 12px; color: var(--text-muted); }
.dm-advice { margin-top: 12px; padding: 10px; background: var(--c-gold-100); border-radius: var(--r-sm); font-size: 13px; color: var(--text-secondary); }

.raw-json { margin-top: 12px; }
.raw-json pre { font-size: 12px; white-space: pre-wrap; word-break: break-all; color: var(--text-secondary); }
</style>
