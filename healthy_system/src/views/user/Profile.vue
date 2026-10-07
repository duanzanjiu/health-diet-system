<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <!-- 左侧个人卡片 -->
      <el-col :span="8">
        <div class="profile-card stagger-item" style="animation-delay: 0.1s">
          <!-- 头部渐变区域 -->
          <div class="profile-cover">
            <div class="cover-pattern"></div>
            <div class="cover-orb"></div>
          </div>
          <div class="profile-avatar-area">
            <div class="avatar-ring" @click="triggerUpload">
              <el-avatar :size="88" :src="avatarUrl || ''"
                         style="background: var(--c-forest-100); color: var(--c-forest-700); font-size: 36px; font-weight: 800;">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="22" color="#fff"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
              <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleAvatarUpload" />
            </div>
            <h3 class="font-display profile-name">{{ userStore.userInfo?.nickname || '用户' }}</h3>
            <p class="profile-username">@{{ userStore.userInfo?.username }}</p>
            <el-tag size="small" effect="plain" class="role-tag"
                    :type="userStore.isAdmin ? 'warning' : 'success'">
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
          </div>

          <!-- 联系信息 -->
          <div class="profile-meta">
            <div class="meta-item">
              <div class="meta-icon-wrap">
                <el-icon><Phone /></el-icon>
              </div>
              <div class="meta-detail">
                <span class="meta-label">手机号</span>
                <span class="meta-value">{{ userStore.userInfo?.phone || '未设置' }}</span>
              </div>
            </div>
            <div class="meta-item">
              <div class="meta-icon-wrap">
                <el-icon><Message /></el-icon>
              </div>
              <div class="meta-detail">
                <span class="meta-label">邮箱</span>
                <span class="meta-value">{{ userStore.userInfo?.email || '未设置' }}</span>
              </div>
            </div>
          </div>

          <!-- 统计摘要 -->
          <div class="profile-stats">
            <div class="pstat-item">
              <span class="pstat-num font-display">0</span>
              <span class="pstat-label">饮食清单</span>
            </div>
            <div class="pstat-sep"></div>
            <div class="pstat-item">
              <span class="pstat-num font-display">0</span>
              <span class="pstat-label">收藏食谱</span>
            </div>
            <div class="pstat-sep"></div>
            <div class="pstat-item">
              <span class="pstat-num font-display">0</span>
              <span class="pstat-label">推荐记录</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧设置区 -->
      <el-col :span="16">
        <el-tabs v-model="activeTab" class="profile-tabs stagger-item" style="animation-delay: 0.2s">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="info">
            <div class="page-card settings-card">
              <div class="settings-header">
                <h4 class="font-display">个人资料</h4>
                <p class="settings-desc">更新您的基本信息</p>
              </div>
              <el-form ref="infoFormRef" :model="infoForm" label-width="80px" size="large">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="昵称">
                      <el-input v-model="infoForm.nickname" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-radio-group v-model="infoForm.gender">
                        <el-radio :label="1">男</el-radio>
                        <el-radio :label="2">女</el-radio>
                        <el-radio :label="0">未知</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="手机号">
                      <el-input v-model="infoForm.phone" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="邮箱">
                      <el-input v-model="infoForm.email" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item>
                  <el-button type="primary" :loading="infoLoading" @click="saveInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 修改密码 -->
          <el-tab-pane label="修改密码" name="password">
            <div class="page-card settings-card">
              <div class="settings-header">
                <h4 class="font-display">安全设置</h4>
                <p class="settings-desc">定期修改密码以保障账号安全</p>
              </div>
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules"
                       label-width="100px" size="large" style="max-width: 460px;">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdLoading" @click="savePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { profile, changePassword } from '@/api/auth'

const userStore = useUserStore()
const activeTab = ref('info')
const infoLoading = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref()
const fileInput = ref()

const infoForm = reactive({
  nickname: '', phone: '', email: '', gender: 0, avatar: ''
})

const avatarUrl = computed(() => infoForm.avatar || userStore.userInfo?.avatar || '')

function triggerUpload() {
  fileInput.value?.click()
}

async function handleAvatarUpload(e) {
    const input = e.target
    const file = input.files?.[0]
    if (!file) return
    if (file.size > 5 * 1024 * 1024) {
        ElMessage.warning('图片大小不能超过 5MB')
        return
    }
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post('/common/upload', formData)
    infoForm.avatar = res.data
    // 立即保存头像到后端
    await request.put('/auth/profile', { avatar: res.data })
    // 同步更新本地用户信息到 store，所有引用位置自动刷新
    userStore.updateUserInfo({ avatar: res.data })
    ElMessage.success('头像更新成功')
    input.value = ''
}

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (_r, v, cb) => {
  if (v !== pwdForm.newPassword) { cb(new Error('两次密码不一致')) } else { cb() }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

onMounted(() => {
  const u = userStore.userInfo
  if (u) {
    infoForm.nickname = u.nickname || ''
    infoForm.phone = u.phone || ''
    infoForm.email = u.email || ''
    infoForm.gender = u.gender || 0
    infoForm.avatar = u.avatar || ''
  }
})
async function saveInfo() {
  infoLoading.value = true
  try {
    await request.put('/auth/profile', infoForm)
    ElMessage.success('修改成功')
    // 同步更新本地用户信息到 store，所有引用位置自动刷新
     userStore.updateUserInfo(infoForm)
  } catch {} finally { infoLoading.value = false }
}

async function savePassword() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdLoading.value = true
    try {
      await request.post('/auth/change-password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } catch {} finally { pwdLoading.value = false }
  })
}
</script>

<style scoped>
/* ─── Profile Card ─── */
.profile-card {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

/* Cover area with gradient and pattern */
.profile-cover {
  height: 80px;
  background: linear-gradient(135deg, var(--c-forest-800), var(--c-forest-600));
  position: relative;
  overflow: hidden;
}

.cover-pattern {
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='40' height='40' viewBox='0 0 40 40' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23ffffff' fill-opacity='0.04'%3E%3Cpath d='M0 40L40 0H20L0 20M40 40V20L20 40'/%3E%3C/g%3E%3C/svg%3E");
}

.cover-orb {
  position: absolute;
  width: 160px; height: 160px;
  border-radius: 50%;
  top: -60%; right: -10%;
  background: radial-gradient(circle, rgba(196,147,58,0.2) 0%, transparent 70%);
}

.profile-avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px 24px;
  margin-top: -44px;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.avatar-ring {
  padding: 4px;
  border-radius: 50%;
  background: var(--bg-card);
  box-shadow: var(--shadow-sm);
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.avatar-overlay {
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  opacity: 0;
  transition: opacity var(--dur-fast) ease;
}

.avatar-overlay span {
  font-size: 11px;
  color: #fff;
  font-weight: 600;
}

.avatar-ring:hover .avatar-overlay {
  opacity: 1;
}

.profile-name {
  font-size: 22px;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  margin-top: 4px;
}

.profile-username {
  font-size: 13px;
  color: var(--text-muted);
}

.role-tag {
  margin-top: 2px;
}

/* Meta info */
.profile-meta {
  padding: 4px 24px 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}

.meta-item:last-child {
  border-bottom: none;
}

.meta-icon-wrap {
  width: 36px; height: 36px;
  border-radius: var(--r-md);
  background: var(--c-sand-100);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--text-muted);
}

.meta-detail {
  display: flex;
  flex-direction: column;
}

.meta-label {
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 600;
}

.meta-value {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Stats bar */
.profile-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 18px 24px;
  border-top: 1px solid var(--border-light);
  background: var(--c-sand-50);
}

.pstat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pstat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.pstat-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  font-weight: 500;
}

.pstat-sep {
  width: 1px;
  height: 30px;
  background: var(--border-light);
}

/* ─── Settings ─── */
.settings-card {
  padding: 28px;
}

.settings-header {
  margin-bottom: 24px;
}

.settings-header h4 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.settings-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* Tab refinement */
.profile-tabs :deep(.el-tabs__item) {
  font-weight: 600;
  font-size: 14px;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background: var(--primary);
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--border-light);
}
</style>
