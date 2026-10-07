package com.duanzanjiu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.entity.*;
import com.duanzanjiu.exception.BusionessException;
import com.duanzanjiu.mapper.FoodInfoMapper;
import com.duanzanjiu.mapper.FoodRecipeMapper;
import com.duanzanjiu.mapper.RecommendRecordMapper;
import com.duanzanjiu.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendRecordService {
   @Autowired
   private RecommendRecordMapper recommendRecordMapper;

   @Autowired
   private UserHealthProfileService userHealthProfileService;

   @Autowired
   private AiService aiService;

   @Autowired
   private FoodInfoMapper foodInfoMapper;

   @Autowired
   private FoodRecipeMapper foodRecipeMapper;

   @Autowired
   private SysUserMapper sysUserMapper;


    public RecommendRecord getTodayRecommend(Long userId) {
        RecommendRecord recommendRecord = recommendRecordMapper.selectOne(new LambdaQueryWrapper<RecommendRecord>()
                .eq(RecommendRecord::getUserId, userId)
                .eq(RecommendRecord::getRecommendDate, LocalDate.now())
                .eq(RecommendRecord::getSource,"AI")
                .orderByDesc(RecommendRecord::getCreateTime)
                .last("limit 1"));
        if (recommendRecord !=null){
            return recommendRecord;
        }
       return generateRecommend(userId);

    }

    public RecommendRecord generateRecommend(Long userId) {

        //获取用户健康档案
        UserHealthProfile userHealthProfile = userHealthProfileService.getByUserId(userId);
        //构建用户提示词
        String prompt = buildDailyPrompt(userHealthProfile);
        //构建系统提示词

        String systemPrompt = "你是一名专业的注册营养师，擅长根据用户的身体数据和饮食偏好设计一日三餐方案。"
                + "你必须严格按用户要求的 JSON 格式输出，foods 只能从给定的食材库/食谱库里挑选，不要输出任何多余的解释文字。";
        //调用ai大模型推荐
        String jsonResult = aiService.chatForJson(systemPrompt,prompt);
        RecommendRecord record= new RecommendRecord();
        record.setUserId(userId);
        record.setRecommendDate(LocalDate.now());
        record.setContent(jsonResult);
        record.setSource("AI");
        record.setStatus(0);
        recommendRecordMapper.insert(record);
        return record;
    }

    public RecommendRecord aiGenerateRecommend(Long userId,String userRequest) {
        if (!StringUtils.hasText(userRequest)){
            throw new BusionessException("交出你的白饭");
        }
        //获取用户档案
        UserHealthProfile userHealthProfile = userHealthProfileService.getByUserId(userId);
        //构建用户提示词
        String prompt = buildCustomPrompt(userHealthProfile,userRequest);
        //构建系统提示词
        String systemPrompt = "你是一个专业的营养师。请根据用户的健康档案和自定义需求推荐饮食方案。请严格按照指定的JSON格式返回，不要添加任何额外文字说明。";
        //调用ai大模型推荐
        String jsonResult = aiService.chatForJson(systemPrompt,prompt);
        RecommendRecord record= new RecommendRecord();
        record.setUserId(userId);
        record.setRecommendDate(LocalDate.now());
        record.setContent(jsonResult);
        record.setSource("AI");
        record.setStatus(0);
        recommendRecordMapper.insert(record);
        return record;
    }

    public void feedback(Long userId,String feedback,Long recordId){
        RecommendRecord recommendRecord = recommendRecordMapper.selectById(recordId);
        if (recommendRecord == null){
            throw new BusionessException("推荐记录不存在");
        }
        if(!recommendRecord.getUserId().equals(userId)){
            throw new BusionessException("您没有权限操作该推荐记录");
        }
        recommendRecord.setFeedback(feedback);
       // recommendRecord.setStatus(1);
        recommendRecordMapper.updateById(recommendRecord);
    }

    public Page<RecommendRecord> listRecords(Page<RecommendRecord> page,String date,String source){
      LambdaQueryWrapper<RecommendRecord> wrapper =  new LambdaQueryWrapper<>();
      if(StringUtils.hasText(date)){
          wrapper.eq(RecommendRecord::getRecommendDate,LocalDate.parse(date));
      }
      if(StringUtils.hasText(source)){
          wrapper.eq(RecommendRecord::getSource,source);
      }
      wrapper.orderByDesc(RecommendRecord::getCreateTime);
        Page<RecommendRecord> result = recommendRecordMapper.selectPage(page,wrapper);
        //填充用户信息
        result.getRecords().forEach( r -> {
            if(r.getUserId()!=null){
                SysUser user = sysUserMapper.selectById(r.getUserId());
                if(user!=null){
                    r.setNickname(user.getNickname());
                    r.setUsername(user.getUsername());
                    r.setAvatar(user.getAvatar());
                }
            }
        });
        return result;
    }

    public Page<RecommendRecord> listMyRecords(Page<RecommendRecord> page, Long userId, String date) {
        LambdaQueryWrapper<RecommendRecord> wrapper =  new LambdaQueryWrapper<>();
        if(StringUtils.hasText(date)){
            wrapper.eq(RecommendRecord::getRecommendDate,LocalDate.parse(date));
        }
        wrapper.eq(RecommendRecord::getUserId,userId);
        wrapper.orderByDesc(RecommendRecord::getCreateTime);
        //填充用户信息

        return recommendRecordMapper.selectPage(page,wrapper);
    }

    private String buildDailyPrompt(UserHealthProfile profile){
        //根据用户健康档案构建提示词
        StringBuilder sb = new StringBuilder();
        sb.append("基于以下用户信息推荐今日三餐饮食方案：\n");

        if (profile != null) {
            sb.append("- 身高：").append(profile.getHeight()).append("cm，体重：").append(profile.getWeight()).append("kg");
            if (profile.getBmi() != null) {
                sb.append("，BMI：").append(profile.getBmi());
            }
            sb.append("\n");
            sb.append("- 年龄：").append(profile.getAge()).append("，性别：").append(profile.getGender() == 1 ? "男" : "女").append("\n");
            sb.append("- 活动量：").append(getActivityLabel(profile.getActivityLevel())).append("\n");
            if (profile.getTargetCalories() != null) {
                sb.append("- 每日目标热量：").append(profile.getTargetCalories()).append("kcal\n");
            }
            if (StringUtils.hasText(profile.getDietPreference())) {
                sb.append("- 饮食偏好：").append(profile.getDietPreference()).append("\n");
            }
            if (StringUtils.hasText(profile.getAllergens())) {
                sb.append("- 过敏源（需避免）：").append(profile.getAllergens()).append("\n");
            }
        } else {
            sb.append("- 用户未填写健康档案，请提供一份通用的均衡饮食方案\n");
        }

        // 附带食材库信息
        appendFoodList(sb);

        sb.append("\n请按以下JSON格式返回推荐方案：\n");
        sb.append("{\n");
        sb.append("  \"breakfast\": {\"name\": \"\", \"foods\": [], \"calories\": 0, \"description\": \"\"},\n");
        sb.append("  \"lunch\": {\"name\": \"\", \"foods\": [], \"calories\": 0, \"description\": \"\"},\n");
        sb.append("  \"dinner\": {\"name\": \"\", \"foods\": [], \"calories\": 0, \"description\": \"\"},\n");
        sb.append("  \"totalCalories\": 0,\n");
        sb.append("  \"nutritionAdvice\": \"\"\n");
        sb.append("}");

        return sb.toString();
    }



    /** 构建自定义需求Prompt */
    private String buildCustomPrompt(UserHealthProfile profile, String userRequest) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户的自定义需求：").append(userRequest).append("\n\n");
        sb.append("用户健康档案：\n");

        if (profile != null) {
            sb.append("- 身高：").append(profile.getHeight()).append("cm，体重：").append(profile.getWeight()).append("kg\n");
            sb.append("- 年龄：").append(profile.getAge()).append("，性别：").append(profile.getGender() == 1 ? "男" : "女").append("\n");
            if (profile.getTargetCalories() != null) {
                sb.append("- 每日目标热量：").append(profile.getTargetCalories()).append("kcal\n");
            }
            if (StringUtils.hasText(profile.getDietPreference())) {
                sb.append("- 饮食偏好：").append(profile.getDietPreference()).append("\n");
            }
            if (StringUtils.hasText(profile.getAllergens())) {
                sb.append("- 过敏源（需避免）：").append(profile.getAllergens()).append("\n");
            }
        }

        appendFoodList(sb);

        sb.append("\n请结合用户需求和健康档案，按以下JSON格式返回推荐方案：\n");
        sb.append("{\n");
        sb.append("  \"breakfast\": {\"name\": \"\", \"foods\": [], \"calories\": 0, \"description\": \"\"},\n");
        sb.append("  \"lunch\": {\"name\": \"\", \"foods\": [], \"calories\": 0, \"description\": \"\"},\n");
        sb.append("  \"dinner\": {\"name\": \"\", \"foods\": [], \"calories\": 0, \"description\": \"\"},\n");
        sb.append("  \"totalCalories\": 0,\n");
        sb.append("  \"nutritionAdvice\": \"\"\n");
        sb.append("}");

        return sb.toString();
    }

    private String getActivityLabel(String level) {
        if (level == null) return "中等";
        switch (level) {
            case "sedentary":   return "久坐不动";
            case "light":       return "轻度活动";
            case "moderate":    return "中等活动";
            case "active":      return "高度活动";
            case "very_active": return "极高活动";
            default:            return "中等活动";
        }
    }

    /** 附带食材库信息 */
    private void appendFoodList(StringBuilder sb) {
        // 取出部分食材名称供AI参考
        List<FoodInfo> foods = foodInfoMapper.selectList(new LambdaQueryWrapper<FoodInfo>()
                .eq(FoodInfo::getStatus, 0)
                .last("LIMIT 50"));
        if (foods != null && !foods.isEmpty()) {
            String foodNames = foods.stream().map(FoodInfo::getFoodName).collect(Collectors.joining("、"));
            sb.append("- 可选食材库：").append(foodNames).append("\n");
            //取出部分食谱供ai参考
            List<FoodRecipe> recipes = foodRecipeMapper.selectList(new LambdaQueryWrapper<FoodRecipe>()
                    .eq(FoodRecipe::getStatus, 0)
                    .last("LIMIT 20"));
            if (!recipes.isEmpty()) {
                String recipeNames = recipes.stream().map(FoodRecipe::getRecipeName).collect(Collectors.joining("、"));
                sb.append("- 可选食谱库：").append(recipeNames).append("\n");
            }
        }


    }
}
