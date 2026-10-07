import request from '@/utils/request'
export function getDietPlanList(params){
    return request({
        url:'/diet-plan/list',
        method:'get',
        params
    })
}

export function getDietPlanDetail(id){
    return request({
        url:`/diet-plan/${id}`,
        method:'get'
    })
}

export function deleteDietPlan(id){
    return request({
        url:`/diet-plan/${id}`,
        method:'delete'
    })
}

export function createDietPlan(recordId){
    return request({
        url:`/diet-plan/from-recommend/${recordId}`,
        method:'post'
    })
}

export function getMyDietPlan(params){
    return request({
        url:'/diet-plan/my',
        method:'get',
        params
    })
}
export function aiGenerateDietPlan(data){
    return request({
        url:'/diet-plan/ai-generate',
        method:'post',
        data
    })
}

export function createPlan(data){
    return request({
        url:'/diet-plan',
        method:'post',
        data
    })
}
export function completePlan(id){
    return request({
        url:`/diet-plan/${id}/complete`,
        method:'put',
        
    })
}
