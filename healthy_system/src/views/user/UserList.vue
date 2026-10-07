<template>
    <div class="user-list-page">
        <div class="page-card">
            <!-- 页面头部 -->
            <div class="page-card-header">
                <div class="header-title-area">
                    <h2 class="font-display section-title">用户管理</h2>
                    <p class="section-subtitle">管理系统用户账号与权限分配</p>
                </div>
            </div>
            <!-- 搜索栏 -->
            <div class="search-bar">
                <el-input v-model="queryParams.keyword" placeholder="搜索用户名/昵称/手机号" clearable prefix-icon="Search"
                    style="width: 280px" @keyup.enter="loadData" />
                <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 140px">
                    <el-option label="正常" :value="0" />
                    <el-option label="禁用" :value="1" />
                </el-select>
                <el-button type="primary" @click="loadData">
                <el-icon>
                    <Search />
                </el-icon> 查询</el-button>
                <el-button @click="resetQuery">重置</el-button>
                <el-button type="primary" @click="handleAdd">
                    <el-icon>
                        <Plus />
                    </el-icon> 新增用户
                </el-button>
            </div>
            <!-- 表格 -->
            <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="70" align="center" />
                <el-table-column prop="username" label="用户名" width="120">
                    <template #default="{ row }">
                        <div class="user-cell">
                            <el-avatar :size="30" :src="row.avatar || ''"
                                style="background: var(--c-forest-100); color: var(--c-forest-700); font-weight: 700; font-size: 12px;">
                                {{ row.nickname?.charAt(0) || 'U' }}
                            </el-avatar>
                            <span>{{ row.username }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="nickname" label="昵称" min-width="120" />
                <el-table-column prop="phone" label="手机号" min-width="130" />
                <el-table-column label="角色" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.roles?.includes('ADMIN') ? 'warning' : 'success'" size="small"
                            effect="plain">
                            {{ row.roles?.includes('ADMIN') ? '管理员' : '用户' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="80" align="center">
                    <template #default="{ row }">
                        <div class="status-badge" :class="row.status === 0 ? 'is-active' : 'is-disabled'">
                            <span class="status-dot"></span>
                            {{ row.status === 0 ? '正常' : '禁用' }}
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="170" />
                <el-table-column label="操作" width="220" align="center">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                        <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
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
        <el-dialog width="520px" v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'">
            <el-form :model="formData" :rules="rules" ref="formRef" label-width="80px">
                <el-form-item label="用户名" prop="username" >
                    <el-input v-model="formData.username":placeholder="isEdit ? '' : '请输入用户名'" :disabled="isEdit" />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="formData.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <!-- <el-form-item label="手机号" prop="phone">
                    <el-input v-model="formData.phone" placeholder="请输入手机号"" />
                </el-form-item> -->
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="手机号" prop="phone">
                            <el-input v-model="formData.phone" placeholder="请输入手机号" />
                        </el-form-item>
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="formData.email" placeholder="请输入邮箱" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="16">
                    <el-col :span="12">
                        <el-form-item label="性别" prop="gender">
                            <el-radio-group v-model="formData.gender">
                                <el-radio label="1">男</el-radio>
                                <el-radio label="2">女</el-radio>
                                <el-radio label="0">未知</el-radio>
                            </el-radio-group>
                        </el-form-item>

                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="角色" prop="roleKey">
                            <el-select v-model="formData.roleKey" placeholder="请选择角色">
                                <el-option label="管理员" value="ADMIN" />
                                <el-option label="用户" value="USER" />
                            </el-select>
<!--                             <el-radio-group v-model="formData.roleKey">
                                <el-radio label="管理员" value="ADMIN">管理员</el-radio>
                                <el-radio label="用户" value="USER">用户</el-radio>
                            </el-radio-group> -->
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="状态" prop="status">
                    <el-switch v-model="formData.status" active-value="0" inactive-value="1" active-text="正常" inactive-text="禁用" />

                </el-form-item>
            </el-form>
            <template #footer>
                <el-button type="primary" @click="handleSubmit">确定</el-button>
                <el-button @click="dialogVisible = false">取消</el-button>
            </template>
        </el-dialog >
    </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, addUser, updateUser, resetPassword, deleteUser } from '@/api/user'
import { ElMessage } from 'element-plus'



const queryParams = reactive({
    keyword: '',
    status: ''
})

const loadData =async () => {
    console.log('加载用户列表', queryParams)
    loading.value = true
    const res = await getUserList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total|| 0
    loading.value = false
}
// 重置查询参数
const resetQuery = () => {
    queryParams.keyword = ''
    queryParams.status = ''
    queryParams.pageNum = 1
    loadData()
}
// 新增用户
const handleAdd = () => {
    console.log('新增用户')
    dialogVisible.value = true
    isEdit.value = false
   Object.assign(formData, {
        id: undefined,
        username: '',
        password: '',
        nickname: '',
        email: '',
        phone: '',
        status: 0,
        gender:0,
        roleKey: "ADMIN"
    })

}

// 列表
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
// 编辑
const handleEdit = (row) => {
    console.log(row)
    isEdit.value = true
    Object.assign(formData, {
        id: row.id,
        username: row.username,
        password: row.password,
        nickname: row.nickname,
        email: row.email,
        phone: row.phone,
        status: row.status,
        gender: row.gender,
        roleKey: row.roles?.includes("ADMIN")?"ADMIN":"USER"
    })
    dialogVisible.value = true
}
// 重置密码
const handleResetPwd = async (row) => {
    try {
        await resetPassword(row.id)
        loadData()
        ElMessage.success('重置密码成功')
    } catch (error) {
        console.error('重置密码失败:', error)
        ElMessage.error('重置密码失败')
    }
}
// 删除
const handleDelete = async (row) => {
    try {
        await deleteUser(row.id)
        loadData()
        ElMessage.success('删除成功')
    } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
    }
 }

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)

const formRef = ref(null)

// 表单数据
//const roleKey = ref("ADMIN")
const formData = reactive({
    id: '',
    username: '',
    password: '',
    nickname: '',
    phone: '',
    status: 0,
    gender:0,
    roleKey: "ADMIN"
})



// 表单验证规则
const rules = reactive({
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
})

const handleSubmit = async () => {
    formRef.value.validate(async (valid) => {
        if(valid){
           const payload={user:formData,roleKey:formData.roleKey}
           if(isEdit.value){
            await updateUser(payload)
            loadData()
           }else{
            await addUser(payload)
            
           }
           dialogVisible.value = false
           loadData()
        }
    })
  
}


onMounted(() => {
    // 初始化加载用户列表
    loadData()
})
</script>
<style scoped>
.page-card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 24px;
}

.header-title-area .section-title {
    margin-bottom: 0;
}

/* User cell with avatar */
.user-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.user-cell span {
    font-weight: 600;
    font-size: 13px;
}

/* Custom status badge */
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

.status-badge.is-active .status-dot {
    animation: pulseGlow 2s ease-in-out infinite;
}
</style>
