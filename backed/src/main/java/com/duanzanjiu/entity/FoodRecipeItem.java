package com.duanzanjiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 食谱食材关联实体
 */
@Data
@TableName("food_recipe_item")
public class FoodRecipeItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 食谱ID */
    private Long recipeId;

    /** 食材ID */
    private Long foodId;

    /** 用量 */
    private BigDecimal amount;

    /** 单位 */
    private String unit;

    /** 食材名称（非数据库字段） */
    @TableField(exist = false)
    private String foodName;

    /** 食材热量/100g（非数据库字段） */
    @TableField(exist = false)
    private BigDecimal calories;

    /** 蛋白质/100g（非数据库字段） */
    @TableField(exist = false)
    private BigDecimal protein;

    /** 脂肪/100g（非数据库字段） */
    @TableField(exist = false)
    private BigDecimal fat;

    /** 碳水/100g（非数据库字段） */
    @TableField(exist = false)
    private BigDecimal carbohydrate;
}
