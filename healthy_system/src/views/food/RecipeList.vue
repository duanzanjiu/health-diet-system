<template>
  <div class="recipe-page">
    <div class="page-card">
      <div class="page-card-header">
        <div class="header-title-area">
          <h2 class="font-display section-title">食谱管理</h2>
          <p class="section-subtitle">管理食物搭配与营养食谱</p>
        </div>
      </div>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="queryParams.keyword" placeholder="搜索食谱名称" clearable prefix-icon="Search" style="width: 220px"
          @keyup.enter="loadData" />
        <el-select v-model="queryParams.mealType" placeholder="餐次类型" clearable style="width: 140px">
          <el-option label="早餐" value="breakfast" />
          <el-option label="午餐" value="lunch" />
          <el-option label="晚餐" value="dinner" />
          <el-option label="加餐" value="snack" />
        </el-select>
        <el-button type="primary" @click="loadData"><el-icon>
            <Search />
          </el-icon> 查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon>
            <Plus />
          </el-icon> 新增食谱
        </el-button>
      </div>
      <!-- 表格 -->
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="recipeName" label="食谱名称" min-width="150">
          <template #default="{ row }">
            <div class="recipe-cell">
              <el-image v-if="row.image" :src="row.image" fit="cover"
                style="width: 36px; height: 36px; border-radius: 6px; flex-shrink: 0;" />
              <div v-else class="recipe-placeholder">🍲</div>
              <span>{{ row.recipeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="mealType" label="餐次" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="mealTagType(row.mealType)" size="small" effect="plain">
              {{ mealTypeLabel(row.mealType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCalories" label="总热量(kcal)" width="120" align="center" />
        <el-table-column prop="suitableCrowd" label="适合人群" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" :width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
          @change="loadData" />
      </div>
    </div>
    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑食谱' : '新增食谱'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="食谱名称" prop="recipeName">
              <el-input v-model="formData.recipeName" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="餐次类型" prop="mealType">
              <el-select v-model="formData.mealType" placeholder="请选择" style="width: 100%">
                <el-option label="早餐" value="breakfast" />
                <el-option label="午餐" value="lunch" />
                <el-option label="晚餐" value="dinner" />
                <el-option label="加餐" value="snack" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适合人群">
          <el-input v-model="formData.suitableCrowd" placeholder="如：减脂人群、增肌人群、老年人" />
        </el-form-item>
        <el-form-item label="食谱图片">
          <el-upload class="recipe-image-upload" action="/api/common/upload" name="file" :show-file-list="false"
            :on-success="handleImageSuccess" accept="image/*">
            <div v-if="formData.image" class="upload-preview">
              <el-image :src="formData.image" fit="cover" style="width: 100px; height: 100px; border-radius: 8px;" />
              <div class="upload-mask">更换</div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon :size="24">
                <Plus />
              </el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
        <!-- 食材列表 -->
        <el-form-item label="食材搭配">
          <div class="items-area">
            <div v-for="(item, idx) in formData.items" :key="idx" class="item-row">
              <el-select v-model="item.foodId" placeholder="选择食材" filterable style="flex: 2; min-width: 180px">
                <el-option v-for="f in foodList" :key="f.id" :label="f.foodName" :value="f.id" />
              </el-select>
              <el-input-number v-model="item.amount" :min="1" :controls="false" placeholder="用量" style="width: 100px" />
              <el-select v-model="item.unit" style="width: 80px">
                <el-option label="g" value="g" />
                <el-option label="ml" value="ml" />
                <el-option label="个" value="个" />
                <el-option label="片" value="片" />
              </el-select>
              <el-button type="danger" link @click="removeItem(idx)">
                <el-icon>
                  <Delete />
                </el-icon>
              </el-button>
            </div>
            <el-button type="primary" link @click="addItem">
              <el-icon>
                <Plus />
              </el-icon> 添加食材
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="烹饪步骤">
          <el-input v-model="formData.cookingSteps" type="textarea" :rows="3" placeholder="详细烹饪步骤" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="食谱简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="食谱详情" width="600px">
      <div class="recipe-detail" v-if="detailData">
        <div class="detail-header">
          <el-image v-if="detailData.image" :src="detailData.image" fit="cover"
            style="width: 80px; height: 80px; border-radius: 12px;" />
          <div v-else class="detail-placeholder">🍲</div>
          <div class="detail-info">
            <h3 class="font-display">{{ detailData.recipeName }}</h3>
            <div class="detail-tags">
              <el-tag size="small" effect="plain" :type="mealTagType(detailData.mealType)">
                {{ mealTypeLabel(detailData.mealType) }}
              </el-tag>
              <el-tag v-if="detailData.suitableCrowd" size="small" effect="plain" type="info">
                {{ detailData.suitableCrowd }}
              </el-tag>
            </div>
          </div>
          <div class="detail-calories">
            <span class="cal-num font-display">{{ detailData.totalCalories || 0 }}</span>
            <span class="cal-unit">kcal</span>
          </div>
        </div>

        <!-- 食材列表 -->
        <div class="detail-section" v-if="detailData.items && detailData.items.length">
          <h4>食材清单</h4>
          <div class="ingredient-list">
            <div v-for="item in detailData.items" :key="item.id" class="ingredient-item">
              <span class="ing-name">{{ item.foodName || '未知食材' }}</span>
              <span class="ing-amount">{{ item.amount }}{{ item.unit }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="detailData.cookingSteps">
          <h4>烹饪步骤</h4>
          <p class="steps-text">{{ detailData.cookingSteps }}</p>
        </div>
        <div class="detail-section" v-if="detailData.description">
          <h4>食谱简介</h4>
          <p>{{ detailData.description }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecipeList,addRecipe,updateRecipe,getFoodList,getRecipeDetail,deleteRecipe } from '@/api/food'
import { onMounted } from 'vue'

onMounted(() => {
  loadData()
 loadFoodList()
})

const loadFoodList = async () => {
  const res = await getFoodList({
    pageNum: 1,
    pageSize: 1000
  })
  foodList.value = res.data.records || []
}

const loadData = async () => {
  loading.value = true
  const res = await getRecipeList(queryParams.value)
  tableData.value = res.data.records || []
  total.value = res.data.total || 0
  loading.value = false
}

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  mealType: ''
})

const resetQuery = () => { }
const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    recipeName: '',
    mealType: '',
    suitableCrowd: '',
    image: '',
    cookingSteps: '',
    description: '',
    items: []
  })
  dialogVisible.value = true
}
/* const loadData = () => {
  console.log(queryParams.value)
} */

// 表格
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
// 映射餐次类型
const mealTypeLabel = (type) => {
  const map = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }
  return map[type] || type
}
const handleDetail = async (row) => {
  const res = await getRecipeDetail(row.id)
  detailData.value = res.data
  detailVisible.value = true
  
 }
const handleEdit =async (row) => {
  isEdit.value = true
  const res = await getRecipeDetail(row.id)
 const items = (res.data.items || []).map(item => {
    return {
      amount: item.amount || 100,
      unit: item.unit || 'g',
      foodId: item.foodId || undefined
    }
   })
  Object.assign(formData,  {
    id:res.data.id,
    recipeName: res.data.recipeName || '',
    mealType: res.data.mealType || '',
    suitableCrowd: res.data.suitableCrowd || '',
    image: res.data.image || '',
    cookingSteps: res.data.cookingSteps || '',
    description: res.data.description || '',
    items: items
  })
   dialogVisible.value = true
  }
 
 
const handleDelete = async (row) => {
  await deleteRecipe(row.id)
  ElMessage.success('删除成功')
  loadData()
  }

// 新增编辑弹窗
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: undefined,
  recipeName: '',
  mealType: '',
  suitableCrowd: '',
  image: '',
  cookingSteps: '',
  description: '',
  items: []
})
const formRules = {
  recipeName: [{ required: true, message: '请输入食谱名称', trigger: 'blur' }],
  mealType: [{ required: true, message: '请选择餐次类型', trigger: 'change' }]
}
const foodList = ref([])
const mealTagType = (type) => {
  const map = { breakfast: 'success', lunch: 'primary', dinner: 'warning', snack: 'info' }
  return map[type] || ''
}

const handleImageSuccess = (res) => {
  if (res.code === 200) {
    formData.image = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const removeItem = (idx) => {
  formData.items.splice(idx, 1)
}
const addItem = () => {
  formData.items.push({ foodId: undefined, amount: 100, unit: 'g' })
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    // 编辑食谱
   await updateRecipe(formData)
   ElMessage.success('编辑食谱成功')
  } else {
    // 新增食谱
    await addRecipe(formData)
    ElMessage.success('新增食谱成功')
  }
  submitLoading.value = false 
  dialogVisible.value = false
  loadData()
 }

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref({})
</script>
<style scoped>
.recipe-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.recipe-cell span {
  font-weight: 600;
  font-size: 13px;
}

.recipe-placeholder {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-sand-100);
  border-radius: 6px;
  font-size: 18px;
  flex-shrink: 0;
}

/* Upload */
.recipe-image-upload :deep(.el-upload) {
  display: block;
}

.upload-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.upload-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s;
}

.upload-preview:hover .upload-mask {
  opacity: 1;
}

.upload-placeholder {
  width: 100px;
  height: 100px;
  border: 2px dashed var(--border-light);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--text-muted);
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-placeholder:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.upload-placeholder span {
  font-size: 12px;
}

/* Items area */
.items-area {
  width: 100%;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

/* Detail */
.recipe-detail {
  padding: 8px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-placeholder {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-sand-100);
  border-radius: 12px;
  font-size: 36px;
}

.detail-info {
  flex: 1;
}

.detail-info h3 {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.detail-tags {
  display: flex;
  gap: 6px;
}

.detail-calories {
  text-align: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(61, 139, 71, 0.08), rgba(61, 139, 71, 0.02));
  border-radius: var(--r-md);
}

.cal-num {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: var(--c-forest-700);
  letter-spacing: -0.02em;
}

.cal-unit {
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.ingredient-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
}

.ingredient-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--c-sand-50);
  border-radius: var(--r-sm);
}

.ing-name {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-primary);
}

.ing-amount {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-section h4 {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.detail-section p {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.steps-text {
  white-space: pre-line;
}
</style>
