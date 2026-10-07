package com.duanzanjiu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.SysNotice;
import com.duanzanjiu.service.SysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeService sysNoticeService;

    @GetMapping("/list")
    public Result<Page<SysNotice>> ListAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ){
        Page<SysNotice> page = new Page<>(pageNum,pageSize);
        Page<SysNotice> result = sysNoticeService.listAll(page,keyword,status);
        return Result.ok(result);

    }

    @PostMapping
    public Result<?> add(@RequestBody SysNotice notice){
        sysNoticeService.addNotice(notice);
        return Result.ok("添加成功");

    }

    @PutMapping
    public Result<?>update(@RequestBody SysNotice notice){
        sysNoticeService.updateNotice(notice);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<?>delete(@PathVariable Long id){
        sysNoticeService.deleteNotice(id);
        return Result.ok("删除成功");
    }

    @GetMapping("/{id}")
    public Result<?>getDetail(@PathVariable Long id){
        SysNotice notice = sysNoticeService.getById(id);
        return Result.ok(notice);
    }

    @GetMapping("/active")
    public Result<?>active(){
      return Result.ok(sysNoticeService.getActiveNotices());
    }
}


