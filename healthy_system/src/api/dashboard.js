import request from '@/utils/request'

export function getDashboardData(){
    return request({
        url:'/dashboard/admin/stats',
        method:'get'
    })
}

export function getDashboardTrend(){
    return request({
        url:'/dashboard/admin/charts',
        method:'get'
    })
}

export function getDashboardOverviewData(){
    return request({
        url:'/dashboard/user/overview',
        method:'get'
    })
}