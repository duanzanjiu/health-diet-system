package com.duanzanjiu.service;

import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.entity.FoodCategory;
import com.duanzanjiu.entity.FoodInfo;
import com.duanzanjiu.exception.BusionessException;
import com.duanzanjiu.mapper.FoodCategoryMapper;
import com.duanzanjiu.mapper.FoodInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FoodInfoService {
    @Autowired
    private FoodInfoMapper foodInfoMapper;

    @Autowired
    private FoodCategoryMapper foodCategoryMapper;


    public Page<FoodInfo> list(Page<FoodInfo> page,String keyword,String categoryId){
        //构建查询条件
        LambdaQueryWrapper<FoodInfo> queryWrapper = new LambdaQueryWrapper<>();

       //根据关键词查询
        if(StringUtils.hasText(keyword)){
            queryWrapper.like(FoodInfo::getFoodName,keyword);
        }
        //根据分类查询
        if(categoryId!=null){
            queryWrapper.eq(FoodInfo::getCategoryId,Long.parseLong(categoryId));
        }
        queryWrapper.orderByAsc(FoodInfo::getId);

        Page<FoodInfo> result = foodInfoMapper.selectPage(page,queryWrapper);

        result.getRecords().forEach(this::fillCategoryName);
        return result;
    }

    public void addFood(FoodInfo foodInfo){
      long count = foodInfoMapper.selectCount(new LambdaQueryWrapper<FoodInfo>()
                .eq(FoodInfo::getFoodName,foodInfo.getFoodName()));
        if(count>0){
            throw new BusionessException("食物名称已存在");
        }

        foodInfoMapper.insert(foodInfo);
    }

    public void updateFood(FoodInfo foodInfo){
        long count = foodInfoMapper.selectCount(new LambdaQueryWrapper<FoodInfo>()
                .eq(FoodInfo::getFoodName,foodInfo.getFoodName())
                .ne(FoodInfo::getId,foodInfo.getId())
        );
        if(count>0){
            throw new BusionessException("食物名称已存在");
        }
        foodInfoMapper.updateById(foodInfo);
    }

    public FoodInfo getDetail(Long id){
        FoodInfo foodInfo = foodInfoMapper.selectById(id);
        if(foodInfo==null){
            throw new BusionessException("食物不存在");
        }
        fillCategoryName(foodInfo);
        return foodInfo;
    }

    public void deleteFood(Long id){
        FoodInfo foodInfo = foodInfoMapper.selectById(id);
        if(foodInfo==null){
            throw new BusionessException("食物不存在");
        }
        foodInfoMapper.deleteById(id);
    }

    //填充分类名称
    private void fillCategoryName(FoodInfo foodinfo){
        if(foodinfo.getCategoryId()!=null){
            FoodCategory category = foodCategoryMapper.selectById(foodinfo.getCategoryId());
            if(category!=null){
                foodinfo.setCategoryName(category.getCategoryName());
            }
        }
    }
}
