package com.duanzanjiu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.DTO.command.UserFormCommandDTO;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.SysUser;
import com.duanzanjiu.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    @Autowired
    private SystemUserService systemUserService;

    @RequestMapping("/list")
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
     //构建分页对象，设置当前页和每页数量
     Page<SysUser> page = new Page<>(pageNum, pageSize);
     //调用服务层方法查询分页数据
     Page<SysUser> result = systemUserService.listUser(page, keyword, status);
         return Result.ok(result);
    }
    @PostMapping
    public Result<?> addUser(@RequestBody UserFormCommandDTO request){
       System.out.println((request.getUser()));
       System.out.println(request.getRoleKey());
       systemUserService.addUser(request.getUser(),request.getRoleKey());
      return Result.ok("添加成功");

    }
    @PutMapping
    public Result<?> updateUser(@RequestBody UserFormCommandDTO request){
        systemUserService.updateUser(request.getUser(),request.getRoleKey());
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id){
        systemUserService.deleteUser(id);
        return Result.ok("删除成功");

    }

    @PostMapping("/reset-password/{id}")
    public Result<?> resetPassword(@PathVariable Long id){
        systemUserService.resetPassword(id);
        return Result.ok("重置密码成功");
    }

}
