package com.duanzanjiu.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 饮食清单实体
 */
@Data
@TableName("diet_plan")
public class DietPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 计划日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /** 计划类型（day/week） */
    private String planType;

    /** 总热量 */
    private BigDecimal totalCalories;

    /** 总蛋白质 */
    private BigDecimal totalProtein;

    /** 总脂肪 */
    private BigDecimal totalFat;

    /** 总碳水 */
    private BigDecimal totalCarbohydrate;

    /** 匹配度评分 */
    private BigDecimal matchScore;

    /** 来源（manual/recommend） */
    private String source;

    /** 状态（0进行中 1已完成） */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 清单明细（非数据库字段） */
    @TableField(exist = false)
    private List<DietPlanItem> items;

    /** 用户昵称（非数据库字段） */
    @TableField(exist = false)
    private String nickname;

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String username;
}
