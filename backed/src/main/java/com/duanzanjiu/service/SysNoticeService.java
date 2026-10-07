package com.duanzanjiu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.entity.SysNotice;
import com.duanzanjiu.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysNoticeService {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    public Page<SysNotice> listAll(Page<SysNotice>page,String keyword,Integer status){
       LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
       if(StringUtils.hasText(keyword)){
           wrapper.eq(SysNotice::getTitle,keyword);
       }
       if(status!=null){
            wrapper.eq(SysNotice::getStatus,status);
       }
       wrapper
               .orderByDesc(SysNotice::getSort)
               .orderByAsc(SysNotice::getCreateTime);
    return sysNoticeMapper.selectPage(page,wrapper);


    }

    public void addNotice(SysNotice notice){
        sysNoticeMapper.insert(notice);

    }
    public void updateNotice(SysNotice notice){
        sysNoticeMapper.updateById(notice);
    }
    public void deleteNotice(Long id){
        sysNoticeMapper.deleteById(id);
    }
    public SysNotice getById(Long id){
        return sysNoticeMapper.selectById(id);
    }
    public List<SysNotice> getActiveNotices(){
        return sysNoticeMapper.selectList(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus,0)
                .orderByAsc(SysNotice::getSort)
                .orderByDesc(SysNotice::getCreateTime)
        );

    }
}
