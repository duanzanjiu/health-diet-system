import request from '@/utils/request'


export function getHealthProfileList(params){
    return request({
        url:'/health-profile/list',
        method:'get',
        params
    })
}
export function getMyHealth(){
    return request({
        url:'/health-profile/mine',
        method:'get'
    })
}

export function saveMyHealth(data){
    return request({
        url:'/health-profile/mine',
        method:'post',
        data
    })
}

export function completePlan(data){
    return request({
        url:`/diet-plan/${id}/complete`,
        method:'put',
        data
    })
}
