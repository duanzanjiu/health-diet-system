package com.duanzanjiu.service;


import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.entity.FoodRecipe;
import com.duanzanjiu.entity.FoodRecipeItem;
import com.duanzanjiu.exception.BusionessException;
import com.duanzanjiu.mapper.FoodInfoMapper;
import com.duanzanjiu.mapper.FoodRecipeItemMapper;
import com.duanzanjiu.mapper.FoodRecipeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FoodRecipeService {
    @Autowired
    private FoodRecipeMapper foodRecipeMapper;

    @Autowired
    private FoodRecipeItemMapper foodRecipeItemMapper;



    public Page<FoodRecipe> listRecipes(Page<FoodRecipe> page, String keyword, String mealType) {
        LambdaQueryWrapper<FoodRecipe> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(FoodRecipe::getRecipeName, keyword);
        }
        if (StringUtils.hasText(mealType)) {
            wrapper.eq(FoodRecipe::getMealType, mealType);
        }
        wrapper.orderByDesc(FoodRecipe::getCreateTime);
        return foodRecipeMapper.selectPage(page, wrapper);
    }

    public void addRecipe(FoodRecipe foodRecipe) {

        //唯一名称检查
       long count = foodRecipeMapper.selectCount(new LambdaQueryWrapper<FoodRecipe>()
                .eq(FoodRecipe::getRecipeName, foodRecipe.getRecipeName()));
        if (count > 0) {
            throw new BusionessException("食谱名称已存在");
        }
        //新增食谱
        foodRecipeMapper.insert(foodRecipe);
        //保存食谱食材关联
        saveRecipeItems(foodRecipe.getId(), foodRecipe.getItems());
       }

    public void updateRecipe(FoodRecipe foodRecipe) {
       long count = foodRecipeMapper.selectCount(new LambdaQueryWrapper<FoodRecipe>()
                .eq(FoodRecipe::getRecipeName, foodRecipe.getRecipeName())
               .ne(FoodRecipe::getId, foodRecipe.getId())
       );

       if (count > 0) {
            throw new BusionessException("食谱名称已存在");
       }
        //更新食谱
        foodRecipeMapper.updateById(foodRecipe);

        //先删除旧的食谱食材关联
        foodRecipeItemMapper.delete(new LambdaQueryWrapper<FoodRecipeItem>()
                .eq(FoodRecipeItem::getRecipeId, foodRecipe.getId()));

        saveRecipeItems(foodRecipe.getId(), foodRecipe.getItems());
    }

    //删除食谱
    public void deleteRecipe(Long id) {
        foodRecipeMapper.deleteById(id);
        //删除食谱食材关联
        foodRecipeItemMapper.delete(new LambdaQueryWrapper<FoodRecipeItem>()
                .eq(FoodRecipeItem::getRecipeId, id));
    }

    //根据食谱ID查询食谱详情
    public FoodRecipe getById(Long id) {

        FoodRecipe recipe = foodRecipeMapper.selectById(id);
        //查询食谱是否存在
        if (recipe == null) {
            throw new BusionessException("食谱不存在");
        }
        //查询食谱食材关联
      List<FoodRecipeItem> items = foodRecipeItemMapper.selectRecipeItems(id);
        recipe.setItems(items);

        //计算食谱中食材的总热量
        BigDecimal totalCalories = BigDecimal.ZERO;
        for (FoodRecipeItem item : items) {
            if(item.getCalories() != null&&item.getAmount() != null) {
              BigDecimal ratio = item.getAmount().divide(BigDecimal.valueOf(100),4, RoundingMode.HALF_UP);
              totalCalories = totalCalories.add(item.getCalories().multiply(ratio));

            }
        }
        recipe.setTotalCalories(totalCalories.setScale(2, RoundingMode.HALF_UP));
        return recipe;
    }

    //批量保存食材关联
    private void saveRecipeItems(Long recipeId, List<FoodRecipeItem> items) {
        if (items != null && !items.isEmpty()) {
            for(FoodRecipeItem item : items) {
                item.setId(null);
                item.setRecipeId(recipeId);
                foodRecipeItemMapper.insert(item);
            }
        }
    }
}