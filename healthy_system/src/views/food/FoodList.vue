<template>
    <div class="food-list-page">
        <div class="page-card">
            <div class="page-card-header">
                <div class="header-title-area">
                    <h2 class="font-display section-title">食材信息</h2>
                    <p class="section-subtitle">管理食材及其营养成分数据</p>
                </div>
            </div>
            <!-- 搜索栏 -->
            <div class="search-bar">
                <el-input v-model="queryParams.keyword" placeholder="搜索食材名称" clearable prefix-icon="Search"
                    style="width: 220px" @keyup.enter="loadData" />
                <el-select v-model="queryParams.categoryId" placeholder="食材分类" clearable style="width: 160px">
                    <el-option v-for="c in categoryList" :key="c.id" :label="c.categoryName" :value="c.id" />
                </el-select>
                <el-button type="primary" @click="loadData"><el-icon>
                        <Search />
                    </el-icon> 查询</el-button>
                <el-button @click="resetQuery">重置</el-button>
                <el-button type="primary" @click="handleAdd">
                    <el-icon>
                        <Plus />
                    </el-icon> 新增食材
                </el-button>
            </div>
            <!-- 表格 -->
            <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="70" align="center" />
                <el-table-column prop="foodName" label="食材名称" min-width="130">
                    <template #default="{ row }">
                        <div class="food-cell">
                            <el-image v-if="row.image" :src="row.image" fit="cover"
                                style="width: 36px; height: 36px; border-radius: 6px; flex-shrink: 0;" />
                            <div v-else class="food-placeholder">🥗</div>
                            <span>{{ row.foodName }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="categoryName" label="分类" width="100" />
                <el-table-column prop="calories" label="热量(kcal)" width="100" align="center" />
                <el-table-column prop="protein" label="蛋白质(g)" width="100" align="center" />
                <el-table-column prop="fat" label="脂肪(g)" width="90" align="center" />
                <el-table-column prop="carbohydrate" label="碳水(g)" width="90" align="center" />
                <el-table-column prop="createTime" label="创建时间" min-width="170" />
                <el-table-column label="操作" width="180" align="center">
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
        <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑食材' : '新增食材'" width="640px" destroy-on-close>
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="食材名称" prop="foodName">
                            <el-input v-model="formData.foodName" placeholder="请输入名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="所属分类" prop="categoryId">
                            <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
                                <el-option v-for="c in categoryList" :key="c.id" :label="c.categoryName"
                                    :value="c.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="热量(kcal)">
                            <el-input-number v-model="formData.calories" :min="0" :precision="2" :controls="false"
                                style="width: 100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="蛋白质(g)">
                            <el-input-number v-model="formData.protein" :min="0" :precision="2" :controls="false"
                                style="width: 100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="脂肪(g)">
                            <el-input-number v-model="formData.fat" :min="0" :precision="2" :controls="false"
                                style="width: 100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="碳水(g)">
                            <el-input-number v-model="formData.carbohydrate" :min="0" :precision="2" :controls="false"
                                style="width: 100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="图片">
                    <el-upload class="food-image-upload" action="/api/common/upload"
                        name="file" :show-file-list="false" :on-success="handleImageSuccess" accept="image/*">
                        <div v-if="formData.image" class="upload-preview">
                            <el-image :src="formData.image" fit="cover"
                                style="width: 100px; height: 100px; border-radius: 8px;" />
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
                <el-form-item label="维生素">
                    <el-input v-model="formData.vitamin" placeholder="如：维C 20mg、维B 0.5mg" />
                </el-form-item>
                <el-form-item label="矿物质">
                    <el-input v-model="formData.mineral" placeholder="如：钙 100mg、铁 2mg" />
                </el-form-item>
                <el-form-item label="烹饪方法">
                    <el-input v-model="formData.cookingMethod" type="textarea" :rows="2" placeholder="烹饪建议" />
                </el-form-item>
                <el-form-item label="食材描述">
                    <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="食材简介" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
            </template>
        </el-dialog>
        <!-- 详情弹窗 -->
        <el-dialog v-model="detailVisible" title="食材详情" width="560px">
        <div class="food-detail" v-if="detailData">
            <div class="detail-header">
            <el-image v-if="detailData.image" :src="detailData.image" fit="cover"
                        style="width: 80px; height: 80px; border-radius: 12px;" />
            <div v-else class="detail-placeholder">🥗</div>
            <div class="detail-info">
                <h3 class="font-display">{{ detailData.foodName }}</h3>
                <el-tag size="small" effect="plain">{{ detailData.categoryName || '未分类' }}</el-tag>
            </div>
            </div>
            <div class="nutrition-grid">
            <div class="nutrition-item" style="--nut-color: #3d8b47">
                <span class="nut-value">{{ detailData.calories }}</span>
                <span class="nut-label">热量(kcal)</span>
            </div>
            <div class="nutrition-item" style="--nut-color: #3a7ec4">
                <span class="nut-value">{{ detailData.protein }}</span>
                <span class="nut-label">蛋白质(g)</span>
            </div>
            <div class="nutrition-item" style="--nut-color: #c4933a">
                <span class="nut-value">{{ detailData.fat }}</span>
                <span class="nut-label">脂肪(g)</span>
            </div>
            <div class="nutrition-item" style="--nut-color: #c4453a">
                <span class="nut-value">{{ detailData.carbohydrate }}</span>
                <span class="nut-label">碳水(g)</span>
            </div>
            </div>
            <div class="detail-section" v-if="detailData.vitamin">
            <h4>维生素</h4><p>{{ detailData.vitamin }}</p>
            </div>
            <div class="detail-section" v-if="detailData.mineral">
            <h4>矿物质</h4><p>{{ detailData.mineral }}</p>
            </div>
            <div class="detail-section" v-if="detailData.cookingMethod">
            <h4>烹饪方法</h4><p>{{ detailData.cookingMethod }}</p>
            </div>
            <div class="detail-section" v-if="detailData.description">
            <h4>食材描述</h4><p>{{ detailData.description }}</p>
            </div>
        </div>
        </el-dialog>
    </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFoodList, deleteFood, addFood, getCategoryList, updateFood, getFoodDetail } from '@/api/food'
import { ElMessage } from 'element-plus'
// 食材分类
const categoryList = ref([])

onMounted(() => {
    CategoryTree()
    loadData()
})

// 请求参数
const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    categoryId: undefined
})

const CategoryTree = async () => {
   const res =await getCategoryList()
   categoryList.value = res.data || []
}

// 总条数
const total = ref(0)

// 请求列表数据
const loadData =async () => {
    loading.value = true
    const res = await getFoodList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    loading.value = false
 }
// 重置查询参数
const resetQuery = () => {
    queryParams.keyword = ''
    queryParams.categoryId = undefined
    queryParams.pageNum = 1
    loadData()
}

// 表格数据
const tableData = ref([])
// 加载状态
const loading = ref(false)
const handleAdd = () => {
    isEdit.value = false
    Object.assign(formData, {
        id: undefined,
        foodName: '',
        categoryId: undefined,
        image: '',
        calories: 0,
        protein: 0,
        fat: 0,
        carbohydrate: 0,
        vitamin: '',
        mineral: '',
        cookingMethod: '',
        description: ''
    })
     dialogVisible.value = true
     
     loadData()
}
const handleDetail = async (row) => {
    const res = await getFoodDetail(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
 }
const handleEdit = (row) => { 
    isEdit.value = true
    Object.assign(formData, {
        id: row.id,
        foodName: row.foodName,
        categoryId: row.categoryId,
        image: row.image,
        calories: row.calories,
        protein: row.protein,
        fat: row.fat,
        carbohydrate: row.carbohydrate,
        vitamin: row.vitamin,
        mineral: row.mineral,
        cookingMethod: row.cookingMethod,
        description: row.description
    })
    
    dialogVisible.value = true
    loadData()
    }
const handleDelete = async (row) => {
    await deleteFood(row.id)
    ElMessage.success('删除成功')
    loadData()
    }

// 新增编辑弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
// 表单数据
const formData = reactive({
  id: undefined,
  foodName: '',
  categoryId: undefined,
  image: '',
  calories: 0,
  protein: 0,
  fat: 0,
  carbohydrate: 0,
  vitamin: '',
  mineral: '',
  cookingMethod: '',
  description: ''
})
// 表单校验
const formRules = {
  foodName: [{ required: true, message: '请输入食材名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}
const handleImageSuccess = (res) => {
 if (res.code === 200) {
    formData.image = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

//const submitLoading = ref(false)
const formRef = ref(null)

const handleSubmit = async () => {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
        // 编辑逻辑
        await updateFood(formData)
        ElMessage.success('编辑成功')
  
    } else {
        // 新增逻辑
        await addFood(formData)
        ElMessage.success('新增成功')
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
.food-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.food-cell span {
    font-weight: 600;
    font-size: 13px;
}

.food-placeholder {
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
.food-image-upload :deep(.el-upload) {
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

/* Detail */
.food-detail {
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

.detail-info h3 {
    font-size: 20px;
    color: var(--text-primary);
    margin-bottom: 6px;
}

.nutrition-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 24px;
}

.nutrition-item {
    padding: 16px;
    background: color-mix(in srgb, var(--nut-color) 6%, transparent);
    border-radius: var(--r-md);
    text-align: center;
}

.nut-value {
    display: block;
    font-size: 22px;
    font-weight: 700;
    color: var(--nut-color);
    letter-spacing: -0.02em;
}

.nut-label {
    display: block;
    font-size: 11px;
    color: var(--text-muted);
    margin-top: 4px;
    text-transform: uppercase;
    letter-spacing: 0.04em;
}

.detail-section {
    margin-bottom: 16px;
}

.detail-section h4 {
    font-size: 13px;
    font-weight: 700;
    color: var(--text-secondary);
    margin-bottom: 6px;
    text-transform: uppercase;
    letter-spacing: 0.04em;
}

.detail-section p {
    font-size: 14px;
    color: var(--text-primary);
    line-height: 1.6;
}
</style>
