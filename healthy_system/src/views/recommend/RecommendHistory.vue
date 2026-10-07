<template>
  <div class="recommend-history-page">
    <div class="page-heading stagger-item" style="animation-delay: 0.05s">
      <h2 class="font-display">推荐历史</h2>
      <p>查看您过往的饮食推荐记录</p>
    </div>

    <div class="page-card stagger-item" style="animation-delay: 0.1s">
      <div class="search-bar">
        <el-date-picker v-model="filterDate" type="date" placeholder="筛选日期"
                        value-format="YYYY-MM-DD" clearable @change="loadData" style="width: 180px;" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <div v-loading="loading">
        <div v-if="records.length === 0 && !loading" class="empty-state">
          <p>暂无推荐记录</p>
        </div>

        <div v-for="record in records" :key="record.id" class="history-card">
          <div class="history-header">
            <div class="history-meta">
              <span class="history-date">{{ record.recommendDate }}</span>
              <el-tag :type="record.source === 'AI' ? 'success' : 'info'" size="small" effect="plain">
                {{ record.source }}
              </el-tag>
              <el-tag v-if="record.feedback" size="small" :type="feedbackType(record.feedback)">
                {{ feedbackLabel(record.feedback) }}
              </el-tag>
            </div>
            <el-button type="primary" link size="small" @click="toggleExpand(record.id)">
              {{ expandedIds.has(record.id) ? '收起' : '展开' }}
            </el-button>
          </div>

          <div v-if="expandedIds.has(record.id)" class="history-body">
            <template v-if="parseContent(record.content)">
              <el-row :gutter="12">
                <el-col :span="8" v-for="meal in getMeals(record.content)" :key="meal.key">
                  <div class="mini-meal">
                    <div class="mini-meal-head">
                      <span>{{ meal.emoji }} {{ meal.label }}</span>
                      <span class="mini-cal">{{ meal.data?.calories || 0 }} kcal</span>
                    </div>
                    <p class="mini-meal-name">{{ meal.data?.name || '--' }}</p>
                    <div class="mini-meal-foods" v-if="meal.data?.foods?.length">
                      <el-tag v-for="(f, i) in meal.data.foods" :key="i" size="small" type="info" effect="plain">{{ f }}</el-tag>
                    </div>
                  </div>
                </el-col>
              </el-row>
              <p class="history-advice" v-if="parseContent(record.content)?.nutritionAdvice">
                {{ parseContent(record.content).nutritionAdvice }}
              </p>
            </template>
            <p v-else class="raw-content">{{ record.content }}</p>
          </div>
        </div>
      </div>

      <div class="pagination-wrapper">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" :current-page="pageNum"
                       @current-change="v => { pageNum = v; loadData() }" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRecommendRecordHistory } from '@/api/recommend'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filterDate = ref('')
const expandedIds = ref(new Set())

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  const res = await getRecommendRecordHistory({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    date: filterDate.value
  })
  loading.value = false
  records.value = res.data.records || []
  total.value = res.data.total || 0
}

function toggleExpand(id) {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
  } else {
    expandedIds.value.add(id)
  }
}

function parseContent(content) {
  try { return JSON.parse(content) } catch { return null }
}

function getMeals(content) {
  const c = parseContent(content)
  if (!c) return []
  return [
    { key: 'breakfast', label: '早餐', emoji: '🌅', data: c.breakfast },
    { key: 'lunch', label: '午餐', emoji: '☀️', data: c.lunch },
    { key: 'dinner', label: '晚餐', emoji: '🌙', data: c.dinner }
  ]
}

function feedbackLabel(fb) {
  const map = { like: '喜欢', dislike: '不喜欢', adopted: '已采用', ignored: '已忽略' }
  return map[fb] || fb
}

function feedbackType(fb) {
  const map = { like: 'success', dislike: 'danger', adopted: '', ignored: 'info' }
  return map[fb] || 'info'
}
</script>

<style scoped>
.page-heading { margin-bottom: 24px; }
.page-heading h2 { font-size: 28px; font-weight: 700; color: var(--text-primary); letter-spacing: -0.02em; }
.page-heading p { font-size: 14px; color: var(--text-muted); margin-top: 4px; }

.empty-state { text-align: center; padding: 40px; color: var(--text-muted); }

.history-card {
  background: var(--bg-card); border: 1px solid var(--border-light); border-radius: var(--r-md);
  padding: 16px 20px; margin-bottom: 12px;
  transition: border-color var(--dur-fast) ease;
}
.history-card:hover { border-color: var(--border); }

.history-header { display: flex; justify-content: space-between; align-items: center; }
.history-meta { display: flex; align-items: center; gap: 8px; }
.history-date { font-size: 14px; font-weight: 600; color: var(--text-primary); }

.history-body { margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border-light); }

.mini-meal {
  background: var(--c-sand-50); border-radius: var(--r-sm); padding: 12px;
  border: 1px solid var(--border-light); margin-bottom: 8px;
}
.mini-meal-head { display: flex; justify-content: space-between; font-size: 13px; font-weight: 600; color: var(--text-primary); margin-bottom: 6px; }
.mini-cal { font-size: 11px; color: var(--c-gold-500); }
.mini-meal-name { font-size: 12px; color: var(--text-secondary); margin-bottom: 6px; }
.mini-meal-foods { display: flex; flex-wrap: wrap; gap: 4px; }

.history-advice { font-size: 12px; color: var(--text-muted); margin-top: 12px; padding: 10px; background: var(--c-gold-100); border-radius: var(--r-sm); }

.raw-content { font-size: 12px; color: var(--text-secondary); white-space: pre-wrap; word-break: break-all; }
</style>
