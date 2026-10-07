import request from '@/utils/request'

export function getNoticeList(params){
    return request({
        url:'/notice/list',
        method:'get',
        params
    })
}

export function addNotice(data){
    return request({
        url:'/notice',
        method:'post',
        data
    })
}

export function updateNotice(data){
    return request({
        url:'/notice',
        method:'put',
        data
    })
}

export function deleteNotice(id){
    return request({
        url:`/notice/${id}`,
        method:'delete'
    })
}

export function getNoticeActive(){
    return request({
        url:'/notice/active',
        method:'get'
    })
}