package com.duanzanjiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 推荐记录实体
 */
@Data
@TableName("recommend_record")
public class RecommendRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 推荐日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recommendDate;

    /** 推荐内容（JSON） */
    private String content;

    /** 来源 RULE/AI */
    private String source;

    /** 用户反馈 like/dislike/adopted/ignored */
    private String feedback;

    /** 状态 0正常 1已过期 */
    private Integer status;

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

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String avatar;
}

