import request from '@/utils/request'

export function getAllRecommend(params) {
    return request({
        url: '/recommend/record/list',
        method: 'get',
        params
    })
}

export function getTodayRecommend() {
    return request({
        url: '/recommend/record/today',
        method: 'get'
    })
}

export function generateRecommend() {
    return request({
        url: '/recommend/record/generate',
        method: 'post'
    })
}
export function feedbackRecommendRecord(data) {
    return request({
        url: '/recommend/record/feedback',
        method: 'post',
        data
    })
}
export function getRecommendRecordDetail(data) {
    return request({
        url: `/recommend/record/ai`,
        method: 'post',
        data
    })
}

export function getRecommendRecordHistory(params) {
    return request({
        url: '/recommend/record/my',
        method: 'get',
        params
    })
}