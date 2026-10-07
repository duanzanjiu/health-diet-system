<template>
    <div>
        <div class="layout-container">
            <aside class="layout-sidebar" :class="{'collapsed': isCollapsed}">
              <div class="sidebar-logo" >
                <h1 v-if="!isCollapsed" class="font-display">Healthy<span>Diet</span></h1>
                <el-icon class="logo-icon" v-else><Sunny /></el-icon>
              </div> 
              <!--用户卡片-->
              <div class="sidebar-user" v-if="!isCollapsed">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{userStore.userInfo?.nickname?.charAt(0)}}
              </el-avatar>
              <div class="sidebar-user-info">
                <span class="sidebar-user-name">{{userStore.userInfo?.nickname}}</span>
                <span class="sidebar-user-role">{{ userStore.isAdmin?'管理员':'普通用户' }}</span>
          </div>
              </div>
              <!--导航菜单-->
                <el-menu
                  :default-active="route.path"
                  class="el-menu-vertical-demo"
                  :collapse="isCollapsed"
                  router
                >
                  <template v-for="item in filterMenus" :key="item.id">
                    <el-menu-item v-if ="!item.children || item.children.length === 0" :index="item.path" >
                      <el-icon><component :is="item.icon" /></el-icon>
                      <span>{{item.menuName}}</span>
                    </el-menu-item>
                    <el-sub-menu v-else :index="item.path">
                      <template #title>
                        <el-icon><component :is="item.icon" /></el-icon>
                        <span>{{item.menuName}}</span>
                      </template>
                        <el-menu-item v-for="child in item.children" 
                        :key="child.id" :index="child.path">
                          <span>{{child.menuName}}</span>
                        </el-menu-item>
                    </el-sub-menu>
                  </template>
                </el-menu>
           <div class="sidebar-footer">
              <div class="sidebar-collapse-btn" @click="toggleCollapse">
                <el-icon><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
               
                <span v-if="!isCollapsed">收起</span>
                <span v-else>展开</span>
              </div>
            </div>
    
            </aside>
            <div class="layout-main" :class="{ collapsed: isCollapsed }">
              <header class="layout-header">
                <div class="header-left">
                  <el-breadcrumb separator="/">
                    <el-breadcrumb-item v-for="item in breadcrumbs" 
                    :key="item.path" >
                      {{item.title}}

                    </el-breadcrumb-item>

                  </el-breadcrumb>
                </div>
                <div class="header-right">

                     <el-dropdown @command="handleCommand">
                          <span class="user-trigger">
                            <el-avatar :size="32" :src="userStore.userInfo?.avatar">{{ userStore.userInfo?.nickname?.charAt(0) }}</el-avatar>
                            <sapn class="user-name">{{userStore.userInfo?.nickname}}</sapn>
                            <el-icon><ArrowDown /></el-icon>
                          </span>
                            
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="profile">
                            <el-icon><User /></el-icon>
                            个人中心
                          </el-dropdown-item>
                          <el-dropdown-item command="logout">
                            <el-icon>
                            <SwitchButton /></el-icon>
                            退出登录
                          </el-dropdown-item>


                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                </div>
              </header>
              <main class="layout-content">
                <router-view v-slot="{Component}">
                  <transition name="side-fade" mode="out-in">
                    <component :is="Component" />
                  </transition>
                </router-view>
              </main>

            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import avatar from '@/avators/301fff0fd5cb4eea8856994e63caa2d6.jpg'
import defaultMenus from '@/data/menuData.js'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
console.log('menuList:', userStore.menuList)  

//通过递归给菜单添加添加前缀路径
function prefixMenuPath(menus) {
  return menus.map((m) => {
    const path = m.path && !m.path.startsWith('/admin') ? '/admin' + m.path : m.path
    const children = m.children?.length ? prefixMenuPath(m.children) : m.children
    return { ...m, path, children }        // 新对象，不碰 store 数据
  })
}

const filterMenus = computed(() => {
  if (userStore.menuList.length > 0) 
  {
    const menus = JSON.parse(JSON.stringify(userStore.menuList))
    return prefixMenuPath(menus)
  }
  return defaultMenus
})
const isCollapsed = ref(false)
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

//面包屑导航
const breadcrumbs = computed(() => {
  return route.matched
    .filter(item => item.meta && item.meta.title)
    .map(item => ({ path: item.path, title: item.meta.title }))
})

//处理下拉菜单点击事件
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/admin/user/profile')     // ← 新增：管理端的个人中心
  } else if (command === 'logout') {
    // 处理退出登录逻辑
    userStore.logout()
    router.push('/login')
  }
}




</script>

<style scoped>

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  margin: 4px 10px 8px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: var(--r-md);
  border: 1px solid rgba(255, 255, 255, 0.04);
  position: relative;
  z-index: 1;
}

.sidebar-user-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-user-name {
  font-size: 13px;
  font-weight: 600;
  color: rgba(250, 249, 246, 0.8);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-user-role {
  font-size: 11px;
  color: rgba(250, 249, 246, 0.3);
  letter-spacing: 0.04em;
}

/* Sidebar footer */
.sidebar-footer {
  padding: 12px 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}

.sidebar-collapse-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--r-md);
  cursor: pointer;
  font-size: 13px;
  color: rgba(250, 249, 246, 0.35);
  transition: all var(--dur-fast) ease;
}

.sidebar-collapse-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(250, 249, 246, 0.6);
}

.sidebar-collapse-btn .el-icon {
  font-size: 16px;
}

/* Header action buttons */
.header-action-btn {
  font-size: 18px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--r-sm);
  transition: all var(--dur-fast) ease;
}

.header-action-btn:hover {
  color: var(--primary);
  background: var(--c-forest-50);
}

/* User trigger */
.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--r-md);
  transition: background var(--dur-fast) ease;
}

.user-trigger:hover {
  background: var(--c-forest-50);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}


</style>