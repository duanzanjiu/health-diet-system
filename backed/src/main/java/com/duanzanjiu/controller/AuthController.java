package com.duanzanjiu.controller;

import com.duanzanjiu.DTO.command.UserLoginCommandDTO;
import com.duanzanjiu.DTO.response.UserLoginResponseDTO;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.SysUser;
import com.duanzanjiu.service.SystemUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private SystemUserService systemUserService;

    @GetMapping("/test")
    public String test() {
        return "test";

    }
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody UserLoginCommandDTO request){
        System.out.println(request.getUsername());
        System.out.println(request.getPassword());
        UserLoginResponseDTO responseDTO = systemUserService.login(request.getUsername(), request.getPassword());
        return Result.ok(responseDTO);
    }

    @PutMapping("profile")
    public Result<?> updateProfile(@RequestBody SysUser user){
        Long userId = getCurrentUserId();
        user.setId(userId);
        systemUserService.updateProfile(user);
        return Result.ok("更新成功");
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        Long userId = getCurrentUserId();
        systemUserService.updatePassword(userId,request.getOldPassword(),request.getNewPassword());
        return Result.ok("密码更新成功");
    }

    @Data
    static class ChangePasswordRequest{
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }


    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (Long) principal;
    }

}