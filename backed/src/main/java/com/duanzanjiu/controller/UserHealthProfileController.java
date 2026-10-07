package com.duanzanjiu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.UserHealthProfile;
import com.duanzanjiu.service.UserHealthProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/health-profile")

public class UserHealthProfileController {

    @Autowired
    private UserHealthProfileService userHealthProfileService;

    @GetMapping("/list")
    public Result<Page<UserHealthProfile>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    )
    {
        Page<UserHealthProfile> page = new Page<>(pageNum, pageSize);
        Page<UserHealthProfile> res = userHealthProfileService.list(page);
        return Result.ok(res);

    }

    @GetMapping("/mine")
    public Result<UserHealthProfile> getMine() {
        Long userId = getCurrentUserId();
        UserHealthProfile profile = userHealthProfileService.getByUserId(userId);
        return Result.ok(profile);

    }

    @PostMapping("/mine")
    public Result<?> updateMine(@RequestBody UserHealthProfile profile) {
         profile.setUserId(getCurrentUserId());
         userHealthProfileService.saveOrUpdateProfile(profile);
         return Result.ok("保存成功");
    }

    //获取用户id
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (Long) principal;
    }
}
