import { createRouter, createWebHistory } from 'vue-router'

// 是否管理员（守卫和 / 的 redirect 都要用，提到外面）
function isAdminUser() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return Array.isArray(userInfo.roles) && userInfo.roles.includes('ADMIN')
}

const routes = [
    {
        path: '/admin',
        name: 'AdminLayout',
        meta: { requiredAuth: true, title: '管理员后台', roles: ['ADMIN'] },
        component: () => import('@/layouts/MainLayout.vue'),
        redirect: '/admin/dashboard',        // ← 把注释打开，否则 /admin 是个空骨架
        children: [
            { path: 'dashboard', 
              name: 'AdminDashboard', 
              component: () => import('@/views/dashboard/DashboardPage.vue'), 
              meta: { title: '首页' } 
            },
            { path: 'user/list', 
              name: 'UserList', 
              component: () => import('@/views/user/UserList.vue'), 
              meta: { title: '用户管理' } 
            },
            {
                path:'user/profile',
                name:'AdminUserProfile',
                component: () => import('@/views/user/Profile.vue'),
                meta: { title: '用户信息' } 
            },
            {
                path:'food/category',
                name:'FoodCategory',
                component: () => import('@/views/food/CategoryList.vue'),
                meta: { title: '食物分类' } 
            },
            {
                path:'food/list',
                name:'FoodList',
                component: () => import('@/views/food/FoodList.vue'),
                meta: { title: '食材信息' } 
            },
            {
                path:'food/recipe',
                name:'RecipeList',
                component:()=>import('@/views/food/RecipeList.vue'),
                meta: { title: '食谱管理' } 
            },
            {
                path:'recommend/daily',
                name:'DailyRecommend',
                component:()=>import('@/views/recommend/AdminDailyOverview.vue'),
                meta: { title: '每日推荐' } 
            },
            {
                path:'recommend/record',
                name:'RecommendRecord',
                component:()=>import('@/views/recommend/RecommendRecordList.vue'),
                meta: { title: '推荐记录' } 
            },
            {
                path:'recommend/ai',
                name:'AIRecommend',
                component:()=>import('@/views/recommend/DailyRecommend.vue'),
                meta: { title: 'AI推荐' } 
            },
            {
                path:'diet-plan/list',
                name:'DietPlanList',
                component:()=>import('@/views/diet-plan/DietPlanList.vue'),
                meta: { title: '饮食计划' } 
            },
            {
                path:'health/list',
                name:'HealthList',
                component:()=>import('@/views/health/HealthProfileList.vue'),
                meta: { title: '健康记录' } 
            },
            {
                path:'notice/list',
                name:'NoticeList',
                component:()=>import('@/views/notice/NoticeList.vue'),
                meta: { title: '通知列表' } 
            }

        ]
    },

     {
        // 登录后的统一入口：全前端只有这一处决定"落地页"
        path: '/',
        name: 'Root',
        redirect: () => (isAdminUser() ? '/admin/dashboard' : '/home/index')
    }, 

    {
        // HomeIndex.vue 之前没注册路由，补上（守卫里引用过 /home/index）
        path: '/home',
        name: 'HomeIndex',
        component: () => import('@/layouts/UserLayout.vue'),
        redirect: '/home/index',
        children: [
            { path: 'index', 
              name: 'UserHome', 
              component: () => import('@/views/home/UserHome.vue'), 
              meta: { title: '用户首页' } 
            },
            {
                path:'food',
                name:'UserFood',
                component: () => import('@/views/food/FoodList.vue'),
                meta: { title: '用户食材' } 
            },
            {
                path:'recipe',
                name:'UserRecipe',
                component: () => import('@/views/food/RecipeList.vue'),
                meta: { title: '用户食谱' } 
            },
            {
                path:'health',
                name:'UserHealth',
                component: () => import('@/views/health/HealthProfile.vue'),
                meta: { title: '用户健康记录' } 
            },
            {
                path:'recommend',
                name:'UserRecommend',
                component: () => import('@/views/recommend/AdminDailyOverview.vue'),
                meta: { title: '用户推荐' } 
            },
            {
                path:'diet-plan',
                name:'UserDietPlan',
                component: () => import('@/views/diet-plan/MyDietPlan.vue'),
                meta: { title: '用户饮食计划' } 
            },
            {
                path:'recommend/history',
                name:'UserHistory',
                component: () => import('@/views/recommend/RecommendHistory.vue'),
                meta: { title: '用户饮食计划历史' } 
            },
            {
                path:'profile',
                name:'UserProfile',
                component: () => import('@/views/user/Profile.vue'),
                meta: { title: '用户个人中心' } 
            }
        ]
    },

    { path: '/login', 
      name: 'Login', 
      component: () => import('@/views/login/LoginPage.vue') 
    },

    { path: '/403',
      name: '403', 
      component: () => import('@/views/error/Forbidden.vue')
     },
    { path: '/:pathMatch(.*)*',
      name: 'NotFound', 
      component: () => import('@/views/error/NotFound.vue') }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 前置导航守卫（把注释恢复，逻辑简化成 3 条）
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')

    // 1) 未登录：除登录页外一律去登录页
    if (!token) {
        if (to.path === '/login') return next()
        return next({ path: '/login', replace: true })
    }

    // 2) 已登录还想去登录页：按角色送回各自首页
    if (to.path === '/login') {
        return next(isAdminUser() ? '/admin/dashboard' : '/home/index')
    }

    // 3) 需要管理员身份的路由：非管理员去 403
    if (to.matched.some(record => record.meta.requiredAuth) && !isAdminUser()) {
        return next('/403')
    }

    next()
})

export default router