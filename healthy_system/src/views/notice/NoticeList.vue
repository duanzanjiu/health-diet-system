<template>
    <div class="notice-list-page">
        <div class="page-card">
            <div class="page-card-header">
                <div class="header-title-area">
                <h2 class="font-display section-title">系统公告管理</h2>
                <p class="section-subtitle">管理系统公告，用户可在首页查看</p>
                </div>
            </div>
            <div class="search-bar">
                <el-input v-model="keyword" placeholder="搜索公告标题" clearable style="width: 240px;"
                        @clear="loadData" @keyup.enter="loadData">
                <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;" @change="loadData">
                <el-option label="正常" :value="0" />
                <el-option label="关闭" :value="1" />
                </el-select>
                <el-button type="primary" @click="loadData">查询</el-button>
                <el-button type="primary" class="btn-accent" @click="openDialog(null)">新增公告</el-button>
            </div>
            <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
                <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
                <el-table-column label="类型" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="typeTagMap[row.noticeType]?.type || 'info'" size="small">
                        {{ typeTagMap[row.noticeType]?.label || row.noticeType }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="80" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
                        {{ row.status === 0 ? '正常' : '关闭' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sort" label="排序" width="70" align="center" />
                <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
                <el-table-column label="创建时间" width="160" align="center">
                    <template #default="{ row }">{{ row.createTime || '--' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="140" align="center">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
                        <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
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
        <!-- 新增/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '新增公告'" width="600px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="top">
                <el-form-item label="公告标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
                </el-form-item>
                <el-form-item label="公告内容" prop="content">
                    <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入公告内容" />
                </el-form-item>
                <el-row :gutter="16">
                    <el-col :span="8">
                        <el-form-item label="类型" prop="noticeType">
                            <el-select v-model="form.noticeType" style="width: 100%;">
                                <el-option label="通知" value="notice" />
                                <el-option label="新功能" value="feature" />
                                <el-option label="提示" value="tip" />
                                <el-option label="警告" value="warning" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="状态" prop="status">
                            <el-select v-model="form.status" style="width: 100%;">
                                <el-option label="正常" :value="0" />
                                <el-option label="关闭" :value="1" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="排序">
                            <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" style="width: 100%;" />
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getNoticeList, addNotice, updateNotice, deleteNotice } from '@/api/notice'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const statusFilter = ref(null)
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const pageNum = ref(1)

const typeTagMap = {
  notice: { label: '通知', type: '' },
  feature: { label: '新功能', type: 'success' },
  tip: { label: '提示', type: 'warning' },
  warning: { label: '警告', type: 'danger' }
}


const loadData = async () => {
    loading.value = true
    const res = await getNoticeList({
        pageNum:pageNum.value,
        pageSize:pageSize.value,
        keyword:keyword.value,
        status:statusFilter.value
    })
    loading.value = false
    tableData.value = res.data.records
    total.value = res.data.total
}

onMounted(() => {
    loadData()
})

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const form = reactive({
  id: null,
  title: '',
  content: '',
  noticeType: 'notice',
  status: 0,
  sort: 0
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 打开新增/编辑公告弹窗
const openDialog = (row) => {
    isEdit.value = !!row
    if(row){
        Object.assign(form, {
            id:row.id,
            title:row.title,
            content:row.content,
            noticeType:row.noticeType,
            status:row.status,
            sort:row.sort
        })
    }else{
        Object.assign(form, {
            id:null,
            title:'',
            content:'',
            noticeType:'notice',
            status:0,
            sort:0
        })
    }
    dialogVisible.value = true
}
// 删除公告
const handleDelete = async (id) => {
    await deleteNotice(id)
    ElMessage.success('删除成功')
    loadData()
}
// 新增/编辑公告

const formRef = ref(null)
const handleSave = async () => {
    if(!formRef.value)return
    await formRef.value.validate()
    if(isEdit.value){
        // 编辑公告
        await updateNotice(form)
        ElMessage.success('编辑成功')
       
    }else{
        // 新增公告
        await addNotice(form)
        ElMessage.success('新增成功')
       
    }
    dialogVisible.value = false
     loadData()
     saving.value = false
}
</script>