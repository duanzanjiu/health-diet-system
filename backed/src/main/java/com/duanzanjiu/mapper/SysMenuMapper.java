package com.duanzanjiu.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanzanjiu.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_role r ON rm.role_id = r.id " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 0 " +
            "ORDER BY m.sort")
    List<SysMenu> selectMenuByUserId(Long userId);

}
