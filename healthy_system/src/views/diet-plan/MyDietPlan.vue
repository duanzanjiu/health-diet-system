<template>
    <div class="my-diet-page">
        <div class="page-heading stagger-item" style="animation-delay: 0.05s">
            <h2 class="font-display section-title">我的饮食清单</h2>
            <p class="section-subtitle">规划每日三餐，科学管理饮食</p>
        </div>
        <!-- 操作栏 -->
        <div class="action-strip stagger-item" style="animation-delay: 0.1s">
            <div class="action-left">
                <el-date-picker v-model="filterDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" clearable
                    @change="loadData" style="width: 180px;" />
                <el-button type="primary" @click="loadData">查询</el-button>
            </div>
            <el-button type="primary" @click="openCreateDialog">
                <el-icon>
                    <Plus />
                </el-icon> 新建清单
            </el-button>
        </div>
        <!-- 清单卡片列表 -->
        <div v-loading="loading" class="plan-list stagger-item" style="animation-delay: 0.15s">
            <div v-if="!loading && plans.length === 0" class="empty-block">
                <el-empty description="暂无饮食清单，快来创建一份吧" />
            </div>
            <div v-for="plan in plans" :key="plan.id" class="plan-card">
                <div class="plan-card-header">
                    <div class="plan-date-badge">
                        <span class="plan-date-day font-display">{{ dayOfDate(plan.planDate) }}</span>
                        <span class="plan-date-month">{{ monthOfDate(plan.planDate) }}</span>
                    </div>
                    <div class="plan-header-info">
                        <div class="plan-header-top">
                            <span class="plan-date-full">{{ plan.planDate }}</span>
                            <el-tag :type="plan.source === 'recommend' ? 'success' : 'primary'" size="small" effect="plain">
                                {{ plan.source === 'recommend' ? 'AI推荐' : '手动创建' }}
                            </el-tag>
                            <el-tag :type="plan.status === 1 ? 'success' : 'info'" size="small">
                                {{ plan.status === 1 ? '已完成' : '进行中' }}
                            </el-tag>
                        </div>
                        <div class="plan-cal-bar">
                            <span class="plan-cal-label">总热量</span>
                            <span class="plan-cal-val font-display">{{ plan.totalCalories || 0 }}</span>
                            <span class="plan-cal-unit">kcal</span>
                            <template v-if="plan.matchScore != null">
                                <span class="plan-score-sep">·</span>
                                <span class="plan-score" :class="scoreClass(plan.matchScore)">
                                匹配度 {{ plan.matchScore }}%
                                </span>
                            </template>
                        </div>
                    </div>
                    <div class="plan-card-actions">
                        <el-button type="success" link size="small" v-if="plan.status !== 1" @click="handleComplete(plan)">
                        <el-icon><Check /></el-icon> 完成
                        </el-button>
                        <el-button type="danger" link size="small" @click="handleDelete(plan)">
                        <el-icon><Delete /></el-icon>
                        </el-button>
                    </div>
                </div>
                <!-- 三餐概要 -->
                <div class="plan-meals" v-if="plan.items && plan.items.length > 0">
                    <div v-for="mt in ['breakfast', 'lunch', 'dinner', 'snack']" :key="mt"
                        class="plan-meal-chip"
                        v-show="mealItems(plan.items, mt).length > 0">
                        <span class="meal-emoji">{{ mealEmoji(mt) }}</span>
                        <div class="meal-chip-body">
                        <span class="meal-chip-label">{{ mealLabel(mt) }}</span>
                        <span class="meal-chip-foods">
                            {{ mealItems(plan.items, mt).map(i => i.foodName).join('、') }}
                        </span>
                        <span class="meal-chip-cal">
                            {{ mealItems(plan.items, mt).reduce((s, i) => s + (Number(i.calories) || 0), 0) }} kcal
                        </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="pagination-wrapper">
        <el-pagination background layout="total, prev, pager, next, sizes"
                        :total="total" :page-size="pageSize" :current-page="pageNum"
                        :page-sizes="[10, 20, 50]"
                        @current-change="(v) => { pageNum = v; loadData() }"
                        @size-change="(v) => { pageSize = v; pageNum = 1; loadData() }" />
        </div>
        <!-- 新建/编辑清单弹窗 -->
        <el-dialog v-model="dialogVisible" :title="editingPlan?.id ? '编辑饮食清单' : '新建饮食清单'"
                width="720px" top="5vh" destroy-on-close>
            <el-form label-width="80px" size="large">
                <el-form-item label="计划日期">
                <el-date-picker v-model="editingPlan.planDate" type="date"
                                value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 200px;" />
                </el-form-item>
                <!-- AI智能生成区域 -->
                <div class="ai-generate-section">
                    <div class="ai-section-head">
                        <span class="ai-badge">🤖 AI智能生成</span>
                        <span class="ai-hint">输入食材，AI帮你搞定三餐食谱</span>
                    </div>
                    <div class="ai-input-row">
                        <el-input v-model="aiInput" type="textarea" :rows="2"
                                placeholder="例如：鸡胸肉、西兰花、糙米、鸡蛋、牛奶、番茄，AI会用这些食材生成完整食谱"
                                maxlength="300" show-word-limit />
                        <el-button type="primary" class="btn-accent" :loading="aiGenerating"
                                :disabled="!aiInput.trim()" @click="handleAiGenerate">
                        <el-icon style="margin-right: 4px;"><MagicStick /></el-icon> 生成清单
                        </el-button>
                    </div>
                </div>
                <el-divider content-position="center">
                    <span style="font-size: 12px; color: var(--text-muted);">或手动编辑</span>
                </el-divider>

                <!-- 餐次分组 -->
                <div v-for="mt in ['breakfast', 'lunch', 'dinner', 'snack']" :key="mt" class="meal-section">
                    <div class="meal-section-head">
                        <span>{{ mealEmoji(mt) }} {{ mealLabel(mt) }}</span>
                        <el-button type="primary" link size="small" @click="addItem(mt)">
                        <el-icon><Plus /></el-icon> 添加
                        </el-button>
                    </div>
                    <div v-for="(item, idx) in editMealItems(mt)" :key="idx" class="meal-item-row">
                        <el-input v-model="item.foodName" placeholder="食谱/菜名" class="meal-food-input" />
                        <el-input-number v-model="item.calories" :min="0" :precision="0" placeholder="热量"
                                        class="meal-cal-input" />
                        <span class="meal-item-unit">kcal</span>
                        <el-input-number v-model="item.amount" :min="0" :precision="0" placeholder="用量"
                                        class="meal-amt-input" />
                        <el-input v-model="item.unit" placeholder="单位" class="meal-unit-input" />
                        <el-button type="danger" link @click="removeItem(mt, idx)">
                        <el-icon><Delete /></el-icon>
                        </el-button>
                    </div>
                    <div v-if="editMealItems(mt).length === 0" class="meal-empty-hint">暂无食物，点击添加</div>
                </div> 
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="saving" @click="savePlan">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyDietPlan, aiGenerateDietPlan, createPlan, completePlan, deleteDietPlan } from '@/api/dietPlan'
import { ElMessage } from 'element-plus'

const filterDate = ref('')
const loading = ref(false)
const plans = ref([])
const total = ref(0)
const pageSize = ref(10)
const pageNum = ref(1)

const dayOfDate = (d) => {
  return d ? d.split('-')[2] : ''
}
const monthOfDate = (d) => {
  if (!d) return ''
  const m = parseInt(d.split('-')[1])
  const months = ['', '一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
  return months[m] || ''
}
const formatDate = (d) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
const scoreClass = (score) => {
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-ok'
  return 'score-low'
}
const mealItems = (items, mt) => {
  return items.filter((i) => i.mealType === mt)
}
const mealLabel = (mt) => {
  const map = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }
  return map[mt] || mt
}
const mealEmoji = (mt) => {
  const map = { breakfast: '🌅', lunch: '☀️', dinner: '🌙', snack: '🍪' }
  return map[mt] || '🍽'
}


const openCreateDialog = () => {
    //默认打开
    Object.assign(editingPlan, { id:null, planDate: formatDate(new Date()), items: [] })
    for(const mt of ['breakfast', 'lunch', 'dinner']){
        editingPlan.items.push({ 
            mealType: mt,
            foodName:'',
            amount:100,unit:'g',
            calories:0 })
    }
    aiInput.value = ''
    dialogVisible.value = true
}
const loadData = async () => {
    loading.value = true
    const res = await getMyDietPlan({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        date: filterDate.value
    })
    if(res.code ==200){
        plans.value = res.data.records
        total.value = res.data.total
        loading.value = false
    }
}

onMounted(() => {
    loadData()
})

// 列表操作
const handleComplete = async (plan) => {
   await completePlan(plan.id)
   ElMessage.success({
        message: '完成成功'
    })
    loadData()
}
const handleDelete = async (plan) => {
    await deleteDietPlan(plan.id)
    ElMessage.success({
        message: '删除成功'
    })
    loadData()
}

// 新建/编辑清单弹窗
const dialogVisible = ref(false)
const editingPlan = reactive({ planDate: '', items: [] })
const aiInput = ref('')
const aiGenerating = ref(false)
const saving = ref(false)
// 编辑餐次项
const editMealItems = (mealType) => {
  return editingPlan.items.filter((i) => i.mealType === mealType)
}
// 删除餐次项
const removeItem = (mealType, idx) => {
  const items = editMealItems(mealType)
  const target = items[idx]
  const realIdx = editingPlan.items.indexOf(target)
  if (realIdx >= 0) editingPlan.items.splice(realIdx, 1)
}


const handleAiGenerate = async () => {
    if(!aiInput.value.trim()) return
    aiGenerating.value = true
    const res = await aiGenerateDietPlan({
        foodDescription: aiInput.value
    })
    const plan = res.data
    if(plan&&plan.items&&plan.items.length>0){
        //将ai生成的结果填充到表单
        editingPlan.items = plan.items.map((item) => {
        ElMessage.success({
            message: 'AI生成成功'
        })
            return {
            mealType: item.mealType,
            foodName: item.foodName,
            amount: item.amount,
            unit: item.unit || 'g',
            calories: item.calories || 0
        }

        })
    }
    aiGenerating.value = false
}
const addItem = (mealType) => {}
const savePlan = async () => {
    if(!editingPlan.planDate) return ElMessage.error({
        message: '请选择日期'
    })
    const validItems = editingPlan.items.filter((i) => i.foodName.trim() !== '')
    if(validItems.length === 0) return ElMessage.error({
        message: '请添加食物'
    })
    saving.value = true
    const data = {
        ...editingPlan,
        items: validItems
    }
    const res = await createPlan(data)
    if(res.code ==200){
        ElMessage.success({
            message: '创建成功'
        })
        dialogVisible.value = false
        loadData()
        saving.value = false
    }
}
</script>
<style scoped>
.page-heading {
    margin-bottom: 24px;
}

/* ─── Action Strip ─── */
.action-strip {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.action-left {
    display: flex;
    gap: 10px;
    align-items: center;
}

/* ─── Plan Card ─── */
.plan-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-height: 200px;
}

.plan-card {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--r-lg);
    padding: 20px 24px;
    transition: all var(--dur-fast) var(--ease-out);
}

.plan-card:hover {
    border-color: var(--border);
    box-shadow: var(--shadow-sm);
}

.plan-card-header {
    display: flex;
    align-items: center;
    gap: 18px;
}

.plan-date-badge {
    width: 56px;
    height: 56px;
    border-radius: var(--r-md);
    background: linear-gradient(135deg, var(--c-forest-600), var(--c-forest-700));
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.plan-date-day {
    font-size: 22px;
    font-weight: 800;
    color: #fff;
    line-height: 1;
}

.plan-date-month {
    font-size: 10px;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 2px;
}

.plan-header-info {
    flex: 1;
}

.plan-header-top {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
}

.plan-date-full {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
}

.plan-cal-bar {
    display: flex;
    align-items: baseline;
    gap: 4px;
}

.plan-cal-label {
    font-size: 12px;
    color: var(--text-muted);
}

.plan-cal-val {
    font-size: 22px;
    font-weight: 700;
    color: var(--c-forest-600);
}

.plan-cal-unit {
    font-size: 12px;
    color: var(--text-muted);
}

.plan-score-sep {
    color: var(--border);
    margin: 0 6px;
}

.plan-score {
    font-size: 12px;
    font-weight: 600;
}

.score-good {
    color: var(--c-forest-600);
}

.score-ok {
    color: var(--c-gold-500);
}

.score-low {
    color: #d4544e;
}

.plan-card-actions {
    display: flex;
    gap: 4px;
    flex-shrink: 0;
}

/* ─── Meal Chips ─── */
.plan-meals {
    display: flex;
    gap: 12px;
    margin-top: 14px;
    padding-top: 14px;
    border-top: 1px solid var(--border-light);
    flex-wrap: wrap;
}

.plan-meal-chip {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 10px 14px;
    background: var(--c-sand-50);
    border: 1px solid var(--border-light);
    border-radius: var(--r-md);
    min-width: 160px;
    flex: 1;
}

.meal-emoji {
    font-size: 20px;
    margin-top: 1px;
}

.meal-chip-label {
    font-size: 11px;
    color: var(--text-muted);
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.04em;
}

.meal-chip-foods {
    font-size: 13px;
    color: var(--text-secondary);
    font-weight: 500;
    display: block;
    margin: 2px 0;
}

.meal-chip-cal {
    font-size: 11px;
    color: var(--c-gold-500);
    font-weight: 600;
}

/* ─── Dialog Meal Editor ─── */
.meal-section {
    margin-bottom: 20px;
    padding: 16px;
    background: var(--c-sand-50);
    border-radius: var(--r-md);
    border: 1px solid var(--border-light);
}

.meal-section-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 12px;
}

.meal-item-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
    padding: 10px 12px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--r-sm);
}

.meal-food-input {
    flex: 1;
    min-width: 140px;
    max-width: 240px;
}

.meal-cal-input :deep(.el-input-number) {
    width: 130px;
}

.meal-cal-input {
    width: 130px;
    flex-shrink: 0;
}

.meal-amt-input {
    width: 120px;
    flex-shrink: 0;
}

.meal-unit-input {
    width: 72px;
    flex-shrink: 0;
}

.meal-item-unit {
    font-size: 12px;
    color: var(--text-muted);
    flex-shrink: 0;
    white-space: nowrap;
}

.meal-empty-hint {
    font-size: 12px;
    color: var(--text-muted);
    text-align: center;
    padding: 8px;
}

/* ─── Detail ─── */
.detail-meals {
    margin-top: 20px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.detail-meal-card {
    border-radius: var(--r-md);
    border: 1px solid var(--border-light);
    overflow: hidden;
    background: var(--bg-card);
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

.detail-meal-card.snack .dm-deco {
    background: linear-gradient(90deg, #f97316, #fb923c);
}

.dm-inner {
    padding: 14px;
}

.dm-head {
    font-size: 14px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 10px;
}

.dm-food-row {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 6px 0;
    border-bottom: 1px dashed var(--border-light);
}

.dm-food-row:last-child {
    border-bottom: none;
}

.dm-food-name {
    flex: 1;
    font-size: 13px;
    color: var(--text-secondary);
    font-weight: 500;
}

.dm-food-amount {
    font-size: 12px;
    color: var(--text-muted);
}

.dm-food-cal {
    font-size: 12px;
    color: var(--c-gold-500);
    font-weight: 600;
    min-width: 70px;
    text-align: right;
}

/* ─── Analysis ─── */
.analysis-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.analysis-score-ring {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.score-circle {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 4px solid var(--border-light);
}

.score-circle.score-good {
    border-color: var(--c-forest-400);
    background: rgba(61, 139, 71, 0.05);
}

.score-circle.score-ok {
    border-color: var(--c-gold-400);
    background: rgba(196, 147, 58, 0.05);
}

.score-circle.score-low {
    border-color: #d4544e;
    background: rgba(212, 84, 78, 0.05);
}

.score-num {
    font-size: 32px;
    font-weight: 800;
    line-height: 1;
}

.score-good .score-num {
    color: var(--c-forest-600);
}

.score-ok .score-num {
    color: var(--c-gold-500);
}

.score-low .score-num {
    color: #d4544e;
}

.score-unit {
    font-size: 12px;
    color: var(--text-muted);
}

.score-label {
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 500;
}

.analysis-details {
    width: 100%;
    max-width: 320px;
}

.analysis-row {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid var(--border-light);
}

.analysis-row:last-child {
    border-bottom: none;
}

.analysis-key {
    font-size: 13px;
    color: var(--text-secondary);
}

.analysis-val {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-primary);
}

.analysis-advice {
    width: 100%;
    padding: 12px 14px;
    background: var(--c-gold-100);
    border-radius: var(--r-md);
    font-size: 13px;
    color: var(--text-secondary);
    display: flex;
    align-items: flex-start;
    gap: 8px;
}

/* ─── Empty ─── */
.empty-block {
    padding: 40px 0;
}

/* ─── AI Generate ─── */
.ai-generate-section {
    padding: 18px;
    background: linear-gradient(135deg, rgba(61, 139, 71, 0.04), rgba(196, 147, 58, 0.04));
    border: 1px dashed var(--c-forest-300);
    border-radius: var(--r-md);
    margin-bottom: 4px;
}

.ai-section-head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
}

.ai-badge {
    font-size: 14px;
    font-weight: 700;
    color: var(--c-forest-600);
}

.ai-hint {
    font-size: 12px;
    color: var(--text-muted);
}

.ai-input-row {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.ai-input-row .btn-accent {
    align-self: flex-end;
}
</style>
