import request from '@/utils/request'
export const getUserList = (queryParams) => {
    return request({
        url: '/user/list',
        method: 'get',
        params:queryParams
    })
}
export const addUser = (data) => {
    return request({
        url: '/user',
        method: 'post',
        data
    })
}

export const updateUser = (data) => {
    return request({
        url: '/user',
        method: 'put',
        data
    })
}

export const resetPassword = (id) => {
    return request({
        url: `/user/reset-password/${id}`,
        method: 'post',
       
    })
}

export const deleteUser = (id) => {
    return request({
        url: `/user/${id}`,
        method: 'delete',
       
    })
}
