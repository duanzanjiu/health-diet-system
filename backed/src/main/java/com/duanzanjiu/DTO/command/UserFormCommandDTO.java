package com.duanzanjiu.DTO.command;

import com.duanzanjiu.entity.SysUser;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 用户创建请求参数实体
@Data
public class UserFormCommandDTO {
    private SysUser user;
    @NotBlank(message = "角色不能为空")
    private String roleKey;
}
