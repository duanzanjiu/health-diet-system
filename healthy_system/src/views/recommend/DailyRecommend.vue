<template>
    <div class="daily-recommend-page">
        <div class="page-heading stagger-item" style="animation-delay: 0.05s">
            <h2 class="font-display">AI 饮食推荐</h2>
            <p>根据你的健康档案，智能生成个性化三餐方案</p>
        </div>
        <!-- 今日推荐区域 -->
        <div class="recommend-section stagger-item" style="animation-delay: 0.1s">
            <div class="section-bar">
                <h3 class="font-display section-label">今日推荐</h3>
                <el-button type="primary" :loading="generating" @click="handleRegenerate">
                    <el-icon style="margin-right: 4px;">
                        <Refresh />
                    </el-icon> 重新生成
                </el-button>
            </div>
            <div v-if="loading" class="loading-state">
                <el-skeleton :rows="6" animated />
            </div>
            <div v-else-if="parsedContent" class="meal-cards">
                <el-row :gutter="20">
                    <el-col :xs="24" :sm="8" v-for="meal in meals" :key="meal.key">
                        <div class="meal-card" :class="meal.key">
                            <div class="meal-card-deco"></div>
                            <div class="meal-card-inner">
                                <div class="meal-card-header">
                                    <span class="meal-emoji">{{ meal.emoji }}</span>
                                    <div>
                                        <h4 class="font-display meal-type">{{ meal.label }}</h4>
                                        <p class="meal-name">{{ meal.data?.name || '暂无' }}</p>
                                    </div>
                                </div>
                                <div class="meal-calories">
                                    <span class="cal-num font-display">{{ meal.data?.calories || 0 }}</span>
                                    <span class="cal-unit">kcal</span>
                                </div>
                                <div class="meal-foods" v-if="meal.data?.foods?.length">
                                    <el-tag v-for="(f, i) in meal.data.foods" :key="i" size="small" effect="plain"
                                        class="food-tag">{{ f }}</el-tag>
                                </div>
                                <p class="meal-desc" v-if="meal.data?.description">{{ meal.data.description }}</p>
                            </div>
                        </div>
                    </el-col>
                </el-row>
                <!-- 总热量 + 营养建议 -->
                <div class="advice-bar">
                    <div class="total-cal">
                        <span class="total-label">全天总热量</span>
                        <span class="total-num font-display">{{ parsedContent.totalCalories || 0 }}</span>
                        <span class="total-unit">kcal</span>
                    </div>
                    <div class="advice-text" v-if="parsedContent.nutritionAdvice">
                        <el-icon style="color: var(--c-gold-500); margin-right: 6px;">
                            <InfoFilled />
                        </el-icon>
                        {{ parsedContent.nutritionAdvice }}
                    </div>
                </div>
                <!-- 反馈 -->
                <div class="feedback-bar" v-if="currentRecord">
                    <span class="feedback-label">这个推荐方案怎么样？</span>
                    <div class="feedback-btns">
                        <el-button :type="currentRecord.feedback === 'like' ? 'success' : 'default'" size="small" round
                            @click="handleFeedback('like')">
                            👍 喜欢
                        </el-button>
                        <el-button :type="currentRecord.feedback === 'dislike' ? 'danger' : 'default'" size="small"
                            round @click="handleFeedback('dislike')">
                            👎 不喜欢
                        </el-button>
                        <el-button :type="currentRecord.feedback === 'adopted' ? 'primary' : 'default'" size="small"
                            round @click="handleFeedback('adopted')">
                            ✅ 已采用
                        </el-button>
                        <el-button type="warning" size="small" round @click="handleCreatePlan">
                            📋 生成清单
                        </el-button>
                    </div>
                </div>
            </div>
            <div v-else class="empty-state">
                <p>暂无推荐数据，点击上方按钮生成</p>
            </div>
        </div>
        <!-- AI 自定义推荐 -->
        <div class="ai-section stagger-item" style="animation-delay: 0.2s">
            <div class="page-card ai-card">
                <h3 class="font-display section-label" style="margin-bottom: 16px;">自定义AI推荐</h3>
                <p class="ai-hint">描述你的饮食需求，AI将为你生成个性化方案</p>
                <div class="ai-input-area">
                    <el-input v-model="userRequest" type="textarea" :rows="3"
                    placeholder="例如：今天想吃清淡点的，低碳水，不要辣的..." maxlength="500"
                    show-word-limit />
                    <el-button type="primary" class="btn-accent ai-btn"
                                :loading="aiGenerating" :disabled="!userRequest.trim()"
                                @click="handleAiRecommend">
                        <el-icon style="margin-right: 4px;"><MagicStick /></el-icon> 生成推荐
                    </el-button>
                </div>
                <!-- AI推荐结果 -->
                <div v-if="aiResult" class="ai-result">
                    <div class="ai-result-header">
                        <h4 class="font-display">AI 推荐结果</h4>
                        <el-tag type="info" size="small" effect="plain">自定义</el-tag>
                    </div>
                    <el-row :gutter="16">
                        <el-col :xs="24" :sm="8" v-for="meal in aiMeals" :key="meal.key">
                            <div class="ai-meal-item">
                                <div class="ai-meal-head">
                                    <span>{{ meal.emoji }} {{ meal.label }}</span>
                                    <span class="ai-meal-cal">{{ meal.data?.calories || 0 }} kcal</span>
                                </div>
                                <p class="ai-meal-name">{{ meal.data?.name || '--' }}</p>
                                <div class="ai-meal-foods" v-if="meal.data?.foods?.length">
                                <el-tag v-for="(f, i) in meal.data.foods" :key="i" size="small"
                                        type="info" effect="plain">{{ f }}</el-tag>
                                </div>
                                <p class="ai-meal-desc" v-if="meal.data?.description">{{ meal.data.description }}</p>
                            </div>
                        </el-col>
                    </el-row>
                    <div class="ai-advice" v-if="aiParsed?.nutritionAdvice">
                        <el-icon style="color: var(--c-gold-500);"><InfoFilled /></el-icon>
                        {{ aiParsed.nutritionAdvice }}
                    </div>
                </div>
            </div>
        </div> 
    </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTodayRecommend, generateRecommend, feedbackRecommendRecord, getRecommendRecordDetail } from '@/api/recommend'
import { ElMessage } from 'element-plus'
import { createDietPlan } from '@/api/dietPlan'

onMounted(async()=>{
    const res =await getTodayRecommend()
    currentRecord.value = res.data || {}
    loading.value = false
})

const loading = ref(true)
const generating = ref(false)
const currentRecord = ref(null)
const aiGenerating = ref(false)
const userRequest = ref('')
const aiResult = ref(null)

/** 解析推荐内容JSON */
const parsedContent = computed(() => {
    if (!currentRecord.value?.content) return null
    try {
        return JSON.parse(currentRecord.value.content)
    } catch {
        return null
    }
})
const aiParsed = computed(() => {
  if (!aiResult.value?.content) return null
  try {
    return JSON.parse(aiResult.value.content)
  } catch {
    return null
  }
})
const meals = computed(() => {
    const c = parsedContent.value
    if (!c) return []
    return [
        { key: 'breakfast', label: '早餐', emoji: '🌅', data: c.breakfast },
        { key: 'lunch', label: '午餐', emoji: '☀️', data: c.lunch },
        { key: 'dinner', label: '晚餐', emoji: '🌙', data: c.dinner }
    ]
})
const aiMeals = computed(() => {
  const c = aiParsed.value
  if (!c) return []
  return [
    { key: 'breakfast', label: '早餐', emoji: '🌅', data: c.breakfast },
    { key: 'lunch', label: '午餐', emoji: '☀️', data: c.lunch },
    { key: 'dinner', label: '晚餐', emoji: '🌙', data: c.dinner }
  ]
})
// 重新生成推荐
const handleRegenerate = async () => {
    generating.value = true
   const res = await generateRecommend()
   currentRecord.value = res.data || {}
    generating.value = false
    ElMessage.success('重新生成成功')
}
// 处理反馈
const handleFeedback =async (type) => {
    const res = await feedbackRecommendRecord({
        recordId:currentRecord.value.id,
        feedback:type
    })
    currentRecord.value.feedback = type
    ElMessage.success('反馈成功')
}
// 生成清单
const handleCreatePlan = async() => {
    if(!currentRecord.value.id)
    return
    await createDietPlan(currentRecord.value.id)
    ElMessage.success('清单生成成功')

}
// 处理AI推荐
const handleAiRecommend = async () => {
    if(!userRequest.value.trim()){
        ElMessage.error('请输入自定义需求')
        return
    }
        aiGenerating.value = true
        const res = await getRecommendRecordDetail({
            userRequest:userRequest.value
        })
        aiResult.value = res.data || {}
        aiGenerating.value = false
        ElMessage.success('AI推荐成功')
}
</script>
<style scoped>
.page-heading {
    margin-bottom: 28px;
}

.page-heading h2 {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.02em;
}

.page-heading p {
    font-size: 14px;
    color: var(--text-muted);
    margin-top: 4px;
}

/* Section bar */
.section-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
}

.section-label {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.01em;
}

/* Meal cards */
.meal-cards :deep(.el-row) {
    display: flex;
    flex-wrap: wrap;
}

.meal-cards :deep(.el-col) {
    display: flex;
    flex-direction: column;
}

.meal-card {
    background: var(--bg-card);
    border-radius: var(--r-lg);
    border: 1px solid var(--border-light);
    position: relative;
    overflow: hidden;
    margin-bottom: 16px;
    transition: all var(--dur-normal) var(--ease-out);
    flex: 1;
    display: flex;
    flex-direction: column;
}

.meal-card:hover {
    transform: translateY(-3px);
    box-shadow: var(--shadow-md);
    border-color: var(--border);
}

.meal-card-deco {
    height: 4px;
    width: 100%;
}

.meal-card.breakfast .meal-card-deco {
    background: linear-gradient(90deg, var(--c-gold-400), var(--c-gold-300));
}

.meal-card.lunch .meal-card-deco {
    background: linear-gradient(90deg, var(--c-forest-400), var(--c-forest-300));
}

.meal-card.dinner .meal-card-deco {
    background: linear-gradient(90deg, #6366f1, #818cf8);
}

.meal-card-inner {
    padding: 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.meal-desc {
    flex: 1;
}

.meal-card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
}

.meal-emoji {
    font-size: 32px;
}

.meal-type {
    font-size: 16px;
    font-weight: 700;
    color: var(--text-primary);
}

.meal-name {
    font-size: 13px;
    color: var(--text-muted);
    margin-top: 2px;
}

.meal-calories {
    display: flex;
    align-items: baseline;
    gap: 4px;
    margin-bottom: 12px;
}

.cal-num {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.02em;
}

.cal-unit {
    font-size: 12px;
    color: var(--text-muted);
    font-weight: 500;
}

.meal-foods {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 10px;
}

.food-tag {
    font-size: 12px;
}

.meal-desc {
    font-size: 12px;
    color: var(--text-muted);
    line-height: 1.5;
    margin-top: auto;
}

/* Advice bar */
.advice-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 16px;
    padding: 20px 24px;
    background: var(--c-sand-50);
    border-radius: var(--r-lg);
    border: 1px solid var(--border-light);
    margin-bottom: 16px;
}

.total-cal {
    display: flex;
    align-items: baseline;
    gap: 8px;
}

.total-label {
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 600;
}

.total-num {
    font-size: 32px;
    font-weight: 700;
    color: var(--c-forest-600);
    letter-spacing: -0.02em;
}

.total-unit {
    font-size: 13px;
    color: var(--text-muted);
}

.advice-text {
    font-size: 13px;
    color: var(--text-secondary);
    display: flex;
    align-items: center;
    max-width: 60%;
}

/* Feedback */
.feedback-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
    padding: 16px 24px;
    background: var(--bg-card);
    border-radius: var(--r-lg);
    border: 1px solid var(--border-light);
}

.feedback-label {
    font-size: 13px;
    color: var(--text-muted);
    font-weight: 500;
}

.feedback-btns {
    display: flex;
    gap: 8px;
}

/* Loading & empty */
.loading-state,
.empty-state {
    padding: 40px;
    text-align: center;
}

.empty-state p {
    color: var(--text-muted);
    font-size: 14px;
}

/* AI Section */
.ai-section {
    margin-top: 32px;
}

.ai-card {
    padding: 28px;
}

.ai-hint {
    font-size: 13px;
    color: var(--text-muted);
    margin-bottom: 16px;
}

.ai-input-area {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.ai-btn {
    align-self: flex-end;
}

/* AI result */
.ai-result {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid var(--border-light);
}

.ai-result-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;
}

.ai-result-header h4 {
    font-size: 17px;
    font-weight: 700;
    color: var(--text-primary);
}

.ai-result :deep(.el-row) {
    display: flex;
    flex-wrap: wrap;
}

.ai-result :deep(.el-col) {
    display: flex;
    flex-direction: column;
}

.ai-meal-item {
    background: var(--c-sand-50);
    border-radius: var(--r-md);
    padding: 16px;
    border: 1px solid var(--border-light);
    margin-bottom: 12px;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.ai-meal-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
}

.ai-meal-cal {
    font-size: 12px;
    color: var(--c-gold-500);
    font-weight: 700;
}

.ai-meal-name {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 8px;
}

.ai-meal-foods {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-bottom: 8px;
}

.ai-meal-desc {
    font-size: 12px;
    color: var(--text-muted);
    margin-top: auto;
}

.ai-advice {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 16px;
    font-size: 13px;
    color: var(--text-secondary);
    padding: 12px 16px;
    background: var(--c-gold-100);
    border-radius: var(--r-md);
}

.recommend-section {
    margin-bottom: 8px;
}
</style>
