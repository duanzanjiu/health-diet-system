package com.duanzanjiu.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 食谱实体
 */
@Data
@TableName("food_recipe")
public class FoodRecipe {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 食谱名称 */
    private String recipeName;

    /** 餐次类型 breakfast/lunch/dinner/snack */
    private String mealType;

    /** 总热量 */
    private BigDecimal totalCalories;

    /** 适合人群 */
    private String suitableCrowd;

    /** 烹饪步骤 */
    private String cookingSteps;

    /** 食谱图片 */
    private String image;

    /** 描述 */
    private String description;

    /** 状态 0正常 1禁用 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 食材列表（非数据库字段） */
    @TableField(exist = false)
    private List<FoodRecipeItem> items;
}