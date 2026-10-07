package com.duanzanjiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户健康档案实体
 */
@Data
@TableName("user_health_profile")
public class UserHealthProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 身高（cm） */
    private BigDecimal height;

    /** 体重（kg） */
    private BigDecimal weight;

    /** BMI指数 */
    private BigDecimal bmi;

    /** 年龄 */
    private Integer age;

    /** 性别（0未知 1男 2女） */
    private Integer gender;

    /** 活动量 sedentary/light/moderate/active/very_active */
    private String activityLevel;

    /** 饮食偏好 */
    private String dietPreference;

    /** 过敏源 */
    private String allergens;

    /** 疾病史 */
    private String diseaseHistory;

    /** 每日目标热量 */
    private BigDecimal targetCalories;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 用户昵称（非数据库字段） */
    @TableField(exist = false)
    private String nickname;

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String username;
}
