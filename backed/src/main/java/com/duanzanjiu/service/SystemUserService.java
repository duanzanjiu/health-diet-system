package com.duanzanjiu.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.DTO.response.UserLoginResponseDTO;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.common.ResultCode;
import com.duanzanjiu.entity.SysMenu;
import com.duanzanjiu.entity.SysRole;
import com.duanzanjiu.entity.SysUser;
import com.duanzanjiu.entity.SysUserRole;
import com.duanzanjiu.exception.BusionessException;
import com.duanzanjiu.mapper.SysMenuMapper;
import com.duanzanjiu.mapper.SysRoleMapper;
import com.duanzanjiu.mapper.SysUserMapper;
import com.duanzanjiu.mapper.SysUserRoleMapper;
import com.duanzanjiu.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemUserService {
@Autowired
    private SysUserMapper sysUserMapper;

@Autowired
    private SysRoleMapper sysRoleMapper;

@Autowired
    private JwtUtils jwtUtils;

@Autowired
    private SysMenuMapper sysMenuMapper;

@Autowired
    private SysUserRoleMapper sysUserRoleMapper;



    public UserLoginResponseDTO login(String username, String password){

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username).eq(SysUser::getPassword, password);
        //调用mp的查询方法
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);

        //判断用户是否存在
        if(sysUser==null){
            throw new BusionessException(ResultCode.ERROR.getCode(), "用户不存在或密码错误");
        }

        //根据用户id查询角色列表
        List<String> roles = sysRoleMapper.selectRoleKeysByUser(sysUser.getId());
        sysUser.setRoles(roles);
        //System.out.println(roles);

        //生成jwt token
        String token = jwtUtils.generateToken(sysUser.getId(), sysUser.getUsername());
        System.out.println(token);

        //根据用户id查询菜单列表
        List<SysMenu> menus = sysMenuMapper.selectMenuByUserId(sysUser.getId());
        List<SysMenu> menuTree = buildMenuTree(menus,0L);
        UserLoginResponseDTO responseDTO = new UserLoginResponseDTO();
        responseDTO.setMenus(convertMenuToDTO(menuTree));
        responseDTO.setToken(token);
        responseDTO.setUserInfo(convertUserInfoToDTO(sysUser));

        return responseDTO;
    }
    //分页查询
    public Page<SysUser> listUser(Page<SysUser> page,String keyword,Integer status){
        //构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        //传入keyword
        if(StringUtils.hasText(keyword)){
            queryWrapper.and(
                            w -> w.like(SysUser::getUsername,keyword)
                            .or().like(SysUser::getNickname,keyword)
                            .or().like(SysUser::getPhone,keyword)
            );

    }
        //传入status
        if(status!=null){
            queryWrapper.eq(SysUser::getStatus,status);
        }
        //创建时间倒序
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = sysUserMapper.selectPage(page,queryWrapper);

        result.getRecords().forEach(u->{
            u.setPassword(null);
            u.setRoles(sysRoleMapper.selectRoleKeysByUser(u.getId()));
        });
        return result;
    }

    public void addUser(SysUser user,String roleKey){
        //构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername,user.getUsername());
        //调用mp api
        Long count = sysUserMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusionessException("用户名已存在");
        }
        //设置密码
        user.setPassword("123456");
        //插入用户
        sysUserMapper.insert(user);

        //关联角色
        assignRole(user.getId(),roleKey);

    }

    //更新用户
    public void updateUser(SysUser user,String roleKey){
       SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId,user.getId()));
       if(sysUser==null){
           throw new BusionessException("用户不存在");
       }
       //更新用户信息
       sysUser.setNickname(user.getNickname());
       sysUser.setPhone(user.getPhone());
       sysUser.setEmail(user.getEmail());
       //sysUser.setAvatar(user.getAvatar());
       sysUser.setGender(user.getGender());
       sysUser.setStatus(user.getStatus());

        //更新用户表
        sysUserMapper.updateById(sysUser);
        //更新角色
        if(StringUtils.hasText(roleKey)){
            sysUserRoleMapper.delete(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId,user.getId())
            );
            assignRole(user.getId(),roleKey);
        }

    }

    //删除
    public void deleteUser(Long id){
        sysUserMapper.deleteById(id);
        //删除用户角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId,id)
        );
    }
    //重置密码
    public void resetPassword(Long id){
        SysUser sysUser = sysUserMapper.selectById(id);
        if(sysUser==null){
            throw new BusionessException("用户不存在");
        }
        //重置密码
        sysUser.setPassword("1234567");
        //更新用户表
        sysUserMapper.updateById(sysUser);
    }

    public void updateProfile(SysUser user){
        //查询
        SysUser sysUser = sysUserMapper.selectById(user.getId());
        if(sysUser==null){
            throw new BusionessException("用户不存在");
        }
        //更新用户信息
        sysUser.setNickname(user.getNickname());
        sysUser.setPhone(user.getPhone());
        sysUser.setEmail(user.getEmail());
        sysUser.setGender(user.getGender());
        sysUser.setAvatar(user.getAvatar());
        sysUserMapper.updateById(sysUser);
    }

    //更新密码
    public void updatePassword(Long userId, String oldPassword,String newPassword){
        //查询用户
        SysUser sysUser = sysUserMapper.selectById(userId);
        if(sysUser==null){
            throw new BusionessException("用户不存在");
        }
      if(!oldPassword.equals(sysUser.getPassword())){
        throw new BusionessException("原密码错误");
      }
      sysUser.setPassword(newPassword);
      sysUserMapper.updateById(sysUser);
    }


    //转换菜单列表为DTO列表
    private List<UserLoginResponseDTO.Menu> convertMenuToDTO(List<SysMenu> menus) {
        return menus.stream().map(this::convertSignleMenu).collect(Collectors.toList());
    }

    //递归转换单个子菜单
    private UserLoginResponseDTO.Menu convertSignleMenu(SysMenu menu) {
        UserLoginResponseDTO.Menu dto = new UserLoginResponseDTO.Menu();
        dto.setId(menu.getId());
        dto.setMenuName(menu.getMenuName());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setIcon(menu.getIcon());
        dto.setSort(menu.getSort());
        dto.setMenuType(menu.getMenuType());
        dto.setPermission(menu.getPermission());
        dto.setVisible(menu.getVisible());
        dto.setStatus(menu.getStatus());
        dto.setCreateTime(menu.getCreateTime());
        dto.setUpdateTime(menu.getUpdateTime());

        //关于递归子菜单
        if (menu.getChildren()!=null){
            dto.setChildren(convertMenuToDTO(menu.getChildren()));
        }
        return dto;
    }
    //处理用户的数据
    private UserLoginResponseDTO.UserInfo convertUserInfoToDTO(SysUser sysUser) {
        UserLoginResponseDTO.UserInfo dto = new UserLoginResponseDTO.UserInfo();
        dto.setId(sysUser.getId());
        dto.setUsername(sysUser.getUsername());
        dto.setNickname(sysUser.getNickname());
        dto.setPhone(sysUser.getPhone());
        dto.setEmail(sysUser.getEmail());
        dto.setAvatar(sysUser.getAvatar());
        dto.setGender(sysUser.getGender());
        dto.setStatus(sysUser.getStatus());
        dto.setCreateTime(sysUser.getCreateTime());
        dto.setUpdateTime(sysUser.getUpdateTime());
        dto.setRoles(sysUser.getRoles());
        return dto;
    }

    //构建菜单树
    private List<SysMenu> buildMenuTree(List<SysMenu> menus,Long parentId) {
       return menus.stream()
                //只保留父节点下的子菜单
                .filter(menu -> parentId.equals(menu.getParentId()))
                //递归构建子菜单树
                .peek(menu -> menu.setChildren(buildMenuTree(menus,menu.getId())))
                .sorted(Comparator.comparing(m->m.getSort()==null?0:m.getSort()))
                .collect(Collectors.toList());
    }
    //关联角色
    public void assignRole(Long userId,String roleKey){
        //构建关联关系
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey,roleKey));
        if (role!=null){
           SysUserRole userRole = new SysUserRole();
           userRole.setUserId(userId);
           userRole.setRoleId(role.getId());
           sysUserRoleMapper.insert(userRole);
        }
    }
}
