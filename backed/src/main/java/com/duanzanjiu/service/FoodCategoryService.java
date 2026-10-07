package com.duanzanjiu.service;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanzanjiu.entity.FoodCategory;
import com.duanzanjiu.entity.FoodInfo;
import com.duanzanjiu.exception.BusionessException;
import com.duanzanjiu.mapper.FoodCategoryMapper;
import com.duanzanjiu.mapper.FoodInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FoodCategoryService {
    @Autowired
    private FoodCategoryMapper foodCategoryMapper;
    @Autowired
    private FoodInfoMapper foodInfoMapper;

    public List<FoodCategory> tree() {
       //构建查询条件
       LambdaQueryWrapper<FoodCategory> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.orderByAsc(FoodCategory::getSort);
       List<FoodCategory> allList=foodCategoryMapper.selectList(queryWrapper);
       return buildTree(allList, 0L);

    }
    //排除已存在的分类


    public void addCategory(FoodCategory category){
        //判断分类是否存在
        LambdaQueryWrapper<FoodCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FoodCategory::getParentId,category.getParentId() != null?category.getParentId():0)
                .eq(FoodCategory::getCategoryName,category.getCategoryName());
       Long count = foodCategoryMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusionessException("分类名称已存在");
        }
        //插入分类
        foodCategoryMapper.insert(category);

     //   foodCategoryMapper.insert(category);
    }

    public void updateCategory(FoodCategory category){
        //排除重名
        LambdaQueryWrapper<FoodCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FoodCategory::getParentId,category.getParentId() != null?category.getParentId():0)
                .eq(FoodCategory::getCategoryName,category.getCategoryName())
                .ne(FoodCategory::getId,category.getId());
        Long count = foodCategoryMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusionessException("分类名称已存在");
        }
        //更新分类
        foodCategoryMapper.updateById(category);
    }

    //删除分类
    public void deleteCategory(Long id){
        //检查是否存在子分类
        LambdaQueryWrapper<FoodCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FoodCategory::getParentId,id);
        Long count = foodCategoryMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusionessException("存在子分类，不能删除");
        }
     Long foodCount = foodInfoMapper.selectCount(new LambdaQueryWrapper<FoodInfo>()
        .eq(FoodInfo::getCategoryId,id));
        if(foodCount>0){
            throw new BusionessException("存在食材，不能删除");}

        foodCategoryMapper.deleteById(id);
    }

    //通过递归构建树形结构
    private List<FoodCategory> buildTree(List<FoodCategory> allList, Long parentId) {
       List<FoodCategory> treeList = new ArrayList<>();
       for (FoodCategory item : allList) {
           if (parentId.equals(item.getParentId())) {
                item.setChildren(buildTree(allList, item.getId()));
                treeList.add(item);
           }

    }
       return treeList;
}

    public List<FoodCategory> list() {
        return foodCategoryMapper.selectList(null);
    }
}
