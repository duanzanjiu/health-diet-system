import { defineStore } from 'pinia'
import { ref,computed } from 'vue'
import { login, profile, changePassword } from '@/api/auth'  

export const useUserStore = defineStore('user',()=>{
    const token = ref(localStorage.getItem('token')||'')
    const userInfo = ref(null)
    const menuList = ref([])

   const saveUserInfo = localStorage.getItem('userInfo')
   if(saveUserInfo){
    userInfo.value = JSON.parse(saveUserInfo)
   }
   const saveMenuList = localStorage.getItem('menuList')
   if(saveMenuList){
    menuList.value = JSON.parse(saveMenuList)
   }
   
    const setLoginInfo = (data)=>{
        token.value = data.token
        userInfo.value = data.userInfo
        menuList.value = data.menuList
        localStorage.setItem('token',data.token)
        localStorage.setItem('userInfo',JSON.stringify(data.userInfo))
        localStorage.setItem('menuList',JSON.stringify(data.menuList))
    }
    const updateUserInfo = (payload)=>{
        if(userInfo.value){
            Object.assign(userInfo.value,payload)
            localStorage.setItem('userInfo',JSON.stringify(userInfo.value))
        }
        
    }
    //退出登录
    const logout = ()=>{
        token.value = ''
        userInfo.value = null
        menuList.value = []
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('menuList')
    }

    //判断是否是管理员
    const isAdmin = computed(() => {
    return userInfo.value?.roles?.includes('ADMIN') || false
})

    return{
        token,
        userInfo,
        menuList,
        setLoginInfo,
        isAdmin,
        logout,
        updateUserInfo
    }
})
