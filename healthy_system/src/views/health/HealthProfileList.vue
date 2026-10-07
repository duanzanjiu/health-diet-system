<template>
    <div class="health-list-page">
        <div class="page-card">
            <div class="page-card-header">
                <div class="header-title-area">
                <h2 class="font-display section-title">健康档案管理</h2>
                <p class="section-subtitle">查看和管理所有用户的健康档案数据</p>
                </div>
            </div>
            <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" :table-layout="'fixed'">
                <el-table-column prop="nickname" label="昵称" />
                <el-table-column label="性别" align="center">
                    <template #default="{ row }">
                        {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
                    </template>
                </el-table-column>
                <el-table-column prop="age" label="年龄" align="center" />
                <el-table-column label="身高" align="center">
                    <template #default="{ row }">{{ row.height ? row.height + ' cm' : '--' }}</template>
                </el-table-column>
                <el-table-column label="体重" align="center">
                    <template #default="{ row }">{{ row.weight ? row.weight + ' kg' : '--' }}</template>
                </el-table-column>
                <el-table-column label="BMI" align="center">
                    <template #default="{ row }">
                        <el-tag v-if="row.bmi" :type="getBmiTagType(row.bmi)" size="small">
                        {{ row.bmi }}
                        </el-tag>
                        <span v-else>--</span>
                    </template>
                </el-table-column>
                <el-table-column prop="dietPreference" label="饮食偏好" show-overflow-tooltip />
                <el-table-column label="操作" align="center">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
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
        <el-dialog v-model="detailVisible" title="健康档案详情" width="560px" destroy-on-close>
            <div v-if="detailData" class="detail-content">
                <el-descriptions :column="2" border>
                <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
                <el-descriptions-item label="昵称">{{ detailData.nickname }}</el-descriptions-item>
                <el-descriptions-item label="性别">
                    {{ detailData.gender === 1 ? '男' : detailData.gender === 2 ? '女' : '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="年龄">{{ detailData.age || '--' }}</el-descriptions-item>
                <el-descriptions-item label="身高">{{ detailData.height ? detailData.height + ' cm' : '--' }}</el-descriptions-item>
                <el-descriptions-item label="体重">{{ detailData.weight ? detailData.weight + ' kg' : '--' }}</el-descriptions-item>
                <el-descriptions-item label="BMI">
                    <el-tag v-if="detailData.bmi" :type="getBmiTagType(detailData.bmi)" size="small">
                    {{ detailData.bmi }} ({{ getBmiLevel(detailData.bmi) }})
                    </el-tag>
                    <span v-else>--</span>
                </el-descriptions-item>
                <el-descriptions-item label="活动量">{{ activityMap[detailData.activityLevel] || '--' }}</el-descriptions-item>
                <el-descriptions-item label="目标热量">{{ detailData.targetCalories ? detailData.targetCalories + ' kcal' : '--' }}</el-descriptions-item>
                <el-descriptions-item label="饮食偏好" :span="2">{{ detailData.dietPreference || '无' }}</el-descriptions-item>
                <el-descriptions-item label="过敏源" :span="2">{{ detailData.allergens || '无' }}</el-descriptions-item>
                <el-descriptions-item label="疾病史" :span="2">{{ detailData.diseaseHistory || '无' }}</el-descriptions-item>
                <el-descriptions-item label="更新时间" :span="2">{{ detailData.updateTime || '--' }}</el-descriptions-item>
                </el-descriptions>
            </div>
        </el-dialog>
    </div>
</template>
<script setup>
import { ref,onMounted } from 'vue'
import { getHealthProfileList } from '@/api/health'

//此处与源代码有差异，因为好像源代码有bug
const loadData = async () => {
  loading.value = true
  try {
    const res = await getHealthProfileList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
const getBmiTagType = (bmi) => {
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

const getBmiLevel = (bmi) => {
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '偏胖'
  return '肥胖'
}

const activityMap = {
  sedentary: '久坐', light: '轻度', moderate: '中等', active: '高度', very_active: '极高'
}

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageSize = ref(10)
const pageNum = ref(1)
const detailVisible = ref(false)

const detailData = ref({})

const viewDetail = async (row) => {
    detailData.value = row
    detailVisible.value = true
}
</script>
<style scoped>
.detail-content {
  padding: 8px 0;
}
</style>
