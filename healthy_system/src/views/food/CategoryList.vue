<template>
    <div class="category-page">
        <div class="page-card">
            <div class="page-card-header">
                <div class="header-title-area">
                    <h2 class="font-display section-title">食材分类</h2>
                    <p class="section-subtitle">管理食材的分类体系</p>
                </div>
            </div>
            <div class="search-bar">
                <el-button type="primary" @click="handleAdd(0)">
                    <el-icon>
                        <Plus />
                    </el-icon> 新增分类
                </el-button>
            </div>
            <!-- 列表 -->
            <el-table :data="tableData" row-key="id" default-expand-all v-loading="loading" style="width: 100%"
                :tree-props="{ children: 'children' }">
                <el-table-column prop="categoryName" label="分类名称" min-width="200" />
                <el-table-column prop="icon" label="图标" width="80" align="center">
                    <template #default="{ row }">
                        <span v-if="row.icon" style="font-size: 20px;">{{ row.icon }}</span>
                        <span v-else class="text-muted">-</span>
                    </template>
                </el-table-column>
                <el-table-column prop="sort" label="排序" width="80" align="center" />
                <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
                <el-table-column label="状态" width="80" align="center">
                    <template #default="{ row }">
                        <div class="status-badge" :class="row.status === 0 ? 'is-active' : 'is-disabled'">
                            <span class="status-dot"></span>
                            {{ row.status === 0 ? '正常' : '禁用' }}
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" align="center">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="handleAdd(row.id)">新增子级</el-button>
                        <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <!-- 弹窗 -->
        <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
                <el-form-item label="分类名称" prop="categoryName">
                    <el-input v-model="formData.categoryName" placeholder="请输入分类名称" />
                </el-form-item>
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="图标">
                            <el-input v-model="formData.icon" placeholder="emoji图标" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="排序">
                            <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="描述">
                    <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="分类描述" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-switch v-model="formData.status" :active-value="0" :inactive-value="1" active-text="正常" inactive-text="禁用" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/food'
import { ElMessage } from 'element-plus' 

const tableData = ref([])
const loading = ref(false)
const isEdit = ref(false)

// 新增分类
const handleAdd = ()=>{
    isEdit.value = false
    Object.assign(formData, {
        id: undefined,
        parentId: 0,
        categoryName: '',
        icon: '',
        sort: 0,
        description: '',
        status: 0
    })
    dialogVisible.value = true
}
// 编辑分类
const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(formData, {
         id: row.id,
        parentId: row.parentId,
        categoryName: row.categoryName,
        icon: row.icon,
        sort: row.sort,
        description: row.description,
        status: row.status
    })
    dialogVisible.value = true
}
// 删除分类
const handleDelete = (row) => {
    deleteCategory(row.id).then(res => {
        if (res.code === 200) {
            ElMessage.success('删除分类成功')
            loadData()
        }
    })
}

// 弹窗
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formData = reactive({
    id: undefined,
    parentId: 0,
    categoryName: '',
    icon: '',
    sort: 0,
    description: '',
    status: 0
})

const formRules = {
    categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}
const formRef = ref(null)
const handleSubmit = () => {
    formRef.value.validate().then(async () => {
        submitLoading.value = true
        if (isEdit.value) {
            // 编辑分类
           await updateCategory(formData)

            ElMessage.success('编辑分类成功')
 
        } else {
            // 新增分类
        await addCategory(formData)
            ElMessage.success('新增分类成功')
        }
            submitLoading.value = false 
            dialogVisible.value = false
            loadData()
        
    })
}

const loadData = () => {
    loading.value = true
    getCategoryTree().then(res => {
        tableData.value = res.data
        loading.value = false
    })
}

onMounted(() => {
    loadData()
})
</script>
<style scoped>
.status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    font-weight: 600;
    padding: 3px 10px;
    border-radius: var(--r-pill);
}

.status-badge.is-active {
    background: rgba(61, 139, 71, 0.08);
    color: #3d8b47;
}

.status-badge.is-disabled {
    background: rgba(196, 69, 58, 0.08);
    color: #c4453a;
}

.status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: currentColor;
}
</style>
