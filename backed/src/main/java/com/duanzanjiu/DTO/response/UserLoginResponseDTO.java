package com.duanzanjiu.DTO.response;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserLoginResponseDTO {
    private List<Menu> menus;
    private String token;
    private UserInfo userInfo;
    @Data
    public static class Menu{
        private Long id;
        private Long parentId;
        private String menuName;
        private String path;
        private String component;
        private String icon;
        private Integer sort;
        private String menuType;
        private String permission;
        private Integer visible;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private List<Menu> children;
    }
    @Data
    public static class UserInfo{
        private Long id;
        private String username;
        private String nickname;
        private String phone;
        private String email;
        private String avatar;
        private Integer gender;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private List<String> roles;
    }
}
