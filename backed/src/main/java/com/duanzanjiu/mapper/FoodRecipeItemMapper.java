package com.duanzanjiu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanzanjiu.entity.FoodRecipeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FoodRecipeItemMapper extends BaseMapper<FoodRecipeItem> {
    //查询食谱食材信息
    @Select("select ri.*, fi.food_name, fi.calories, fi.protein, fi.fat, fi.carbohydrate  from food_recipe_item ri " +
            "left join food_info fi ON ri.food_id = fi.id " +
            "WHERE ri.recipe_id = #{recipeId}")
    List<FoodRecipeItem> selectRecipeItems(Long recipeId);
}
