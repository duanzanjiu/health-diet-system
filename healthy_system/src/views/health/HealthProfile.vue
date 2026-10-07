<template>
    <div class="health-profile-page">
        <!-- 页面标题 -->
        <div class="page-heading stagger-item" style="animation-delay: 0.05s">
            <div class="heading-text">
                <h2 class="font-display">健康档案</h2>
                <p>记录你的身体指标，获取个性化饮食建议</p>
            </div>
        </div>
        <el-row :gutter="24">
            <!-- 左侧：健康数据概览卡片 -->
             <el-col :xs="24" :sm="24" :md="8" :lg="8">
                <div class="overview-section stagger-item" style="animation-delay: 0.1s">
                    <!-- BMI 卡片 -->
                    <div class="metric-card bmi-card">
                        <div class="metric-card-bg"></div>
                        <div class="metric-card-content">
                        <div class="metric-header">
                            <span class="metric-label">BMI 指数</span>
                            <el-tag :type="bmiTagType" size="small" effect="plain">{{ bmiLevel }}</el-tag>
                        </div>
                        <div class="metric-value font-display">{{ displayBmi }}</div>
                        <div class="bmi-bar-wrap">
                            <div class="bmi-bar">
                            <div class="bmi-bar-fill" :style="{ width: bmiBarWidth }"></div>
                            <div class="bmi-pointer" :style="{ left: bmiBarWidth }"></div>
                            </div>
                            <div class="bmi-labels">
                            <span>偏瘦</span><span>正常</span><span>偏胖</span><span>肥胖</span>
                            </div>
                        </div>
                        </div>
                    </div>
                    <!-- 每日推荐热量卡片 -->
                    <div class="metric-card calorie-card">
                        <div class="metric-card-bg calorie-bg"></div>
                        <div class="metric-card-content">
                        <div class="metric-header">
                            <span class="metric-label">每日推荐热量</span>
                            <el-icon class="metric-icon"><Sunny /></el-icon>
                        </div>
                        <div class="metric-value font-display">
                            {{ displayCalories }}
                            <span class="metric-unit">kcal</span>
                        </div>
                        <p class="metric-hint">基于 Mifflin-St Jeor 公式计算</p>
                        </div>
                    </div>
                    <!-- 基础信息摘要 -->
                    <div class="info-summary-card">
                        <div class="summary-title">基础指标</div>
                        <div class="summary-grid">
                        <div class="summary-item">
                            <span class="summary-icon">📏</span>
                            <div class="summary-detail">
                            <span class="summary-val">{{ form.height || '--' }}</span>
                            <span class="summary-key">身高(cm)</span>
                            </div>
                        </div>
                        <div class="summary-item">
                            <span class="summary-icon">⚖️</span>
                            <div class="summary-detail">
                            <span class="summary-val">{{ form.weight || '--' }}</span>
                            <span class="summary-key">体重(kg)</span>
                            </div>
                        </div>
                        <div class="summary-item">
                            <span class="summary-icon">🎂</span>
                            <div class="summary-detail">
                            <span class="summary-val">{{ form.age || '--' }}</span>
                            <span class="summary-key">年龄</span>
                            </div>
                        </div>
                        <div class="summary-item">
                            <span class="summary-icon">🏃</span>
                            <div class="summary-detail">
                            <span class="summary-val">{{ activityLabel }}</span>
                            <span class="summary-key">活动量</span>
                            </div>
                        </div>
                        </div>
                    </div>
                </div>
            </el-col>
            <!-- 右侧：表单分步骤填写 -->
            <el-col :xs="24" :sm="24" :md="16" :lg="16">
                <div class="form-section stagger-item" style="animation-delay: 0.2s">
                    <div class="page-card form-card">
                        <!-- 步骤条 -->
                        <el-steps :active="currentStep" finish-status="success" simple class="form-steps">
                            <el-step title="基础信息" />
                            <el-step title="身体数据" />
                            <el-step title="饮食偏好" />
                        </el-steps>
                        <div class="step-content">
                            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top" size="large">
                                <!-- 步骤一：基础信息 -->
                                <div v-show="currentStep === 0" class="step-panel">
                                    <div class="step-panel-header">
                                        <h4 class="font-display">基础信息</h4>
                                        <p>填写您的基本身体参数</p>
                                    </div>
                                    <el-row :gutter="20">
                                        <el-col :span="12">
                                            <el-form-item label="年龄" prop="age">
                                                <el-input-number v-model="form.age" :min="1" :max="150"
                                                                placeholder="请输入年龄" controls-position="right" />
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="12">
                                            <el-form-item label="性别" prop="gender">
                                                <el-radio-group v-model="form.gender">
                                                <el-radio-button :label="1">男</el-radio-button>
                                                <el-radio-button :label="2">女</el-radio-button>
                                                </el-radio-group>
                                            </el-form-item>
                                        </el-col>
                                    </el-row>
                                    <el-row :gutter="20">
                                        <el-col :span="12">
                                            <el-form-item label="身高 (cm)" prop="height">
                                                <el-input-number v-model="form.height" :min="50" :max="250"
                                                                :precision="1" :step="0.5"
                                                                placeholder="请输入身高" controls-position="right" />
                                            </el-form-item>
                                        </el-col>
                                        <el-col :span="12">
                                            <el-form-item label="体重 (kg)" prop="weight">
                                                <el-input-number v-model="form.weight" :min="20" :max="300"
                                                                :precision="1" :step="0.5"
                                                                placeholder="请输入体重" controls-position="right" />
                                            </el-form-item>
                                        </el-col>
                                    </el-row>
                                </div>
                                <!-- 步骤二：身体数据 -->
                                <div v-show="currentStep === 1" class="step-panel">
                                    <div class="step-panel-header">
                                        <h4 class="font-display">身体数据</h4>
                                        <p>用于精准计算每日推荐摄入量</p>
                                    </div>
                                    <el-form-item label="活动量" prop="activityLevel">
                                        <el-select v-model="form.activityLevel" placeholder="请选择活动量">
                                            <el-option label="久坐不动 (办公室工作)" value="sedentary" />
                                            <el-option label="轻度活动 (每周1-3次运动)" value="light" />
                                            <el-option label="中等活动 (每周3-5次运动)" value="moderate" />
                                            <el-option label="高度活动 (每周6-7次运动)" value="active" />
                                            <el-option label="极高活动 (专业运动员)" value="very_active" />
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="疾病史">
                                        <el-input v-model="form.diseaseHistory" type="textarea" :rows="3"
                                                placeholder="如有慢性疾病请填写，如：高血压、糖尿病等（选填）" />
                                    </el-form-item>
                                </div>
                                <!-- 步骤三：饮食偏好 -->
                                <div v-show="currentStep === 2" class="step-panel">
                                    <div class="step-panel-header">
                                        <h4 class="font-display">饮食偏好</h4>
                                        <p>帮助系统为您生成更合适的推荐</p>
                                    </div>
                                    <el-form-item label="饮食偏好">
                                        <el-select v-model="dietPreferenceList" multiple
                                                placeholder="选择您的饮食偏好（可多选）" style="width: 100%;">
                                        <el-option label="清淡饮食" value="清淡饮食" />
                                        <el-option label="高蛋白" value="高蛋白" />
                                        <el-option label="低碳水" value="低碳水" />
                                        <el-option label="低脂肪" value="低脂肪" />
                                        <el-option label="素食" value="素食" />
                                        <el-option label="地中海饮食" value="地中海饮食" />
                                        <el-option label="无辣" value="无辣" />
                                        <el-option label="喜辣" value="喜辣" />
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="过敏源">
                                        <el-select v-model="allergenList" multiple
                                                placeholder="选择您的过敏源（可多选）" style="width: 100%;">
                                        <el-option label="牛奶" value="牛奶" />
                                        <el-option label="鸡蛋" value="鸡蛋" />
                                        <el-option label="花生" value="花生" />
                                        <el-option label="坚果" value="坚果" />
                                        <el-option label="大豆" value="大豆" />
                                        <el-option label="小麦" value="小麦" />
                                        <el-option label="鱼类" value="鱼类" />
                                        <el-option label="甲壳类" value="甲壳类" />
                                        <el-option label="芝麻" value="芝麻" />
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="每日目标热量 (kcal)">
                                        <el-input-number v-model="form.targetCalories" :min="800" :max="5000"
                                                        :step="50" controls-position="right"
                                                        placeholder="留空则自动计算" />
                                        <div class="form-tip">留空时系统将根据您的身体数据自动计算</div>
                                    </el-form-item>
                                </div>
                            </el-form>
                            <!-- 操作按钮 -->
                            <div class="step-actions">
                                <el-button v-if="currentStep > 0" @click="currentStep--">
                                上一步
                                </el-button>
                                <el-button v-if="currentStep < 2" type="primary" @click="nextStep">
                                下一步
                                </el-button>
                                <el-button v-if="currentStep === 2" type="primary" class="btn-accent"
                                        :loading="saving" @click="handleSave">
                                保存档案
                                </el-button>
                            </div>
                        </div>
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
</template>
<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { getMyHealth, saveMyHealth} from '@/api/health'
import { ElMessage } from 'element-plus'

const currentStep = ref(0)
const dietPreferenceList = ref([])
const allergenList = ref([])
const saving = ref(false)
const form = reactive({
  height: null,
  weight: null,
  age: null,
  gender: 1,
  activityLevel: 'moderate',
  dietPreference: '',
  allergens: '',
  diseaseHistory: '',
  targetCalories: null
})

const rules = {
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  height: [{ required: true, message: '请输入身高', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  activityLevel: [{ required: true, message: '请选择活动量', trigger: 'change' }]
}


const displayBmi = computed(() => calculatedBmi.value || '--')

const bmiLevel = computed(() => {
  const v = parseFloat(calculatedBmi.value || '0')
  if (!v) return '未知'
  if (v < 18.5) return '偏瘦'
  if (v < 24) return '正常'
  if (v < 28) return '偏胖'
  return '肥胖'
})

const bmiTagType = computed(() => {
  const v = parseFloat(calculatedBmi.value || '0')
  if (!v) return 'info'
  if (v < 18.5) return 'warning'
  if (v < 24) return 'success'
  if (v < 28) return 'warning'
  return 'danger'
})

const bmiBarWidth = computed(() => {
  const v = parseFloat(calculatedBmi.value || '0')
  if (!v) return '0%'
  // 映射 BMI 15-35 到 0%-100%
  const pct = Math.max(0, Math.min(100, ((v - 15) / 20) * 100))
  return pct + '%'
})

/** BMI 计算 */
const calculatedBmi = computed(() => {
  if (form.height && form.weight && form.height > 0) {
    const hm = form.height / 100
    return (form.weight / (hm * hm)).toFixed(2)
  }
  return null
})

/** 推荐热量计算 */
const calculatedCalories = computed(() => {
  if (!form.height || !form.weight || !form.age || form.gender == null) return null
  // Mifflin-St Jeor
  let bmr
  const base = 10 * form.weight + 6.25 * form.height - 5 * form.age
  bmr = form.gender === 2 ? base - 161 : base + 5

  const factors = {
    sedentary: 1.2, light: 1.375, moderate: 1.55, active: 1.725, very_active: 1.9
  }
  const factor = factors[form.activityLevel] || 1.55
  return Math.round(bmr * factor)
})


const displayCalories = computed(() => {
  if (form.targetCalories) return form.targetCalories
  return calculatedCalories.value || '--'
})

const activityLabel = computed(() => {
  const map = {
    sedentary: '久坐', light: '轻度', moderate: '中等', active: '高度', very_active: '极高'
  }
  return map[form.activityLevel] || '--'
})

/** 同步多选到字符串字段 */
watch(dietPreferenceList, (v) => { form.dietPreference = v.join(',') })
watch(allergenList, (v) => { form.allergens = v.join(',') })

const formRef = ref(null)
const nextStep = async ()  => {
  if(!formRef.value){return}
  //根据当前步骤校验相关字段
  const validFields = {
    0: ['age', 'gender', 'height', 'weight', 'activityLevel'],
    1: ['dietPreference', 'allergens']
  }
  const fields = validFields[currentStep.value] || []
  await formRef.value.validateField(fields)
  currentStep.value++
  
}
const handleSave = async () => {
  saving.value = true
  await saveMyHealth(form)
  saving.value = false
  ElMessage.success('档案保存成功')
}

onMounted(async() => {
  const res = await getMyHealth()
  if(res.data){
    const d = res.data
    form.height = d.height || null
    form.weight = d.weight || null
    form.age = d.age || null
    form.gender = d.gender || 1
    form.activityLevel = d.activityLevel || 'moderate'
    form.dietPreference = d.dietPreference || ''
    form.allergens = d.allergens || ''
    form.diseaseHistory = d.diseaseHistory || ''
    form.targetCalories = d.targetCalories || null
    dietPreferenceList.value =d.dietPreference ? d.dietPreference.split(',') : []
    allergenList.value =d.allergens ? d.allergens.split(',') : []
  }
})

</script>
<style scoped>
/* ─── Page Heading ─── */
.page-heading {
  margin-bottom: 28px;
}

.heading-text h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.heading-text p {
  font-size: 14px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* ─── Overview Section ─── */
.overview-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ─── Metric Cards ─── */
.metric-card {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  border: 1px solid var(--border-light);
  padding: 24px;
  position: relative;
  overflow: hidden;
  transition: all var(--dur-normal) var(--ease-out);
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--border);
}

.metric-card-bg {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--c-forest-50) 0%, transparent 70%);
  opacity: 0.8;
}

.calorie-bg {
  background: radial-gradient(circle, var(--c-gold-100) 0%, transparent 70%) !important;
}

.metric-card-content {
  position: relative;
  z-index: 1;
}

.metric-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.metric-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.metric-icon {
  font-size: 20px;
  color: var(--c-gold-400);
}

.metric-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.03em;
  line-height: 1.1;
  margin-bottom: 12px;
}

.metric-unit {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-muted);
  letter-spacing: 0;
}

.metric-hint {
  font-size: 12px;
  color: var(--text-muted);
}

/* ─── BMI Bar ─── */
.bmi-bar-wrap {
  margin-top: 4px;
}

.bmi-bar {
  height: 6px;
  border-radius: var(--r-pill);
  background: linear-gradient(90deg, var(--info) 0%, var(--success) 25%, var(--warning) 60%, var(--danger) 100%);
  position: relative;
}

.bmi-bar-fill {
  display: none;
}

.bmi-pointer {
  position: absolute;
  top: -4px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--bg-card);
  border: 3px solid var(--c-forest-600);
  transform: translateX(-50%);
  box-shadow: var(--shadow-sm);
  transition: left var(--dur-normal) var(--ease-out);
}

.bmi-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 10px;
  color: var(--text-muted);
  font-weight: 500;
}

/* ─── Info Summary Card ─── */
.info-summary-card {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  border: 1px solid var(--border-light);
  padding: 20px;
}

.summary-title {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.summary-detail {
  display: flex;
  flex-direction: column;
}

.summary-val {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.summary-key {
  font-size: 11px;
  color: var(--text-muted);
}

/* ─── Form Section ─── */
.form-card {
  padding: 32px;
}

.form-steps {
  margin-bottom: 32px;
}

.form-steps :deep(.el-step__title) {
  font-weight: 600;
  font-size: 13px;
}

.step-panel {
  min-height: 280px;
}

.step-panel-header {
  margin-bottom: 24px;
}

.step-panel-header h4 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.step-panel-header p {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

.form-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}

/* ─── Responsive ─── */
@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .form-card {
    padding: 20px;
  }
}
</style>
