import request from '@/utils/request'
export const login = (data)=>{
    return request({
        url:'/auth/login',
        method:'post',
        data 
    })
}

export const profile = (data) =>{
    return request({
        url:'/auth/profile',
        method:'put',
        data
    })
}

export const changePassword = (data) =>{
    return request({
        url:'/auth/change-password',
        method:'post',
        data
    })
}

