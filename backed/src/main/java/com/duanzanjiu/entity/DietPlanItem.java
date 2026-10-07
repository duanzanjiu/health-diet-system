package com.duanzanjiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 饮食清单明细实体
 */
@Data
@TableName("diet_plan_item")
public class DietPlanItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 清单ID */
    private Long planId;

    /** 餐次（breakfast/lunch/dinner/snack） */
    private String mealType;

    /** 食材ID */
    private Long foodId;

    /** 食谱ID */
    private Long recipeId;

    /** 食物名称 */
    private String foodName;

    /** 用量 */
    private BigDecimal amount;

    /** 单位 */
    private String unit;

    /** 热量 */
    private BigDecimal calories;
}