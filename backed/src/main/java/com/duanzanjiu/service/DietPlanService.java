package com.duanzanjiu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.entity.*;
import com.duanzanjiu.exception.BusionessException;
import com.duanzanjiu.mapper.DietPlanItemMapper;
import com.duanzanjiu.mapper.DietPlanMapper;
import com.duanzanjiu.mapper.RecommendRecordMapper;
import com.duanzanjiu.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DietPlanService {

    @Autowired
    private DietPlanMapper dietPlanMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private DietPlanItemMapper dietPlanItemMapper;

    @Autowired
    private RecommendRecordMapper recommendRecordMapper;

    @Autowired
    private UserHealthProfileService userHealthProfileService;

    @Autowired
    private AiService aiService;

    public Page<DietPlan> listAll(Page<DietPlan> page, String keyword, String source, String date) {
        LambdaQueryWrapper<DietPlan> wrapper = new LambdaQueryWrapper<DietPlan>();
        if(StringUtils.hasText(date)){
            wrapper.eq(DietPlan::getPlanDate, LocalDate.parse(date));
        }
        if(StringUtils.hasText(source)){
            wrapper.eq(DietPlan::getSource, source);
        }
        wrapper.orderByDesc(DietPlan::getPlanDate);
       Page<DietPlan> result = dietPlanMapper.selectPage(page, wrapper);
       result.getRecords().forEach(plan->{
            attachUserInfo(plan);
            attachPlanDetail(plan);

       });
       if(StringUtils.hasText(keyword)){
          result.getRecords().removeIf(plan->
              (plan.getNickname() == null || !plan.getNickname().contains(keyword))
                      && (plan.getUsername() == null || !plan.getUsername().contains(keyword)));
       }
       return result;
    }

    public DietPlan createFromRecommend(Long recordId,Long userId) {
        RecommendRecord record = recommendRecordMapper.selectById(recordId);
        if(record==null){
            throw new BusionessException("推荐记录不存在");
        }
        if(!record.getUserId().equals(userId)){
            throw new BusionessException("您没有权限操作该推荐记录");
        }
        //解析推荐内容
        String content = record.getContent();
        Map<String,Object> parsed;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            parsed = mapper.readValue(content, Map.class);
        } catch (Exception e) {
            throw new BusionessException("推荐记录内容格式错误");
        }
        //提取推荐内容中的字段
        DietPlan plan = new DietPlan();
        plan.setUserId(userId);
        plan.setPlanDate(record.getRecommendDate());
        plan.setPlanType("day");
        plan.setSource("recommend");
        plan.setStatus(0);

        List<DietPlanItem> items = new ArrayList<>();
        parseMeal(parsed, "breakfast", items);
        parseMeal(parsed, "lunch", items);
        parseMeal(parsed, "dinner", items);
        plan.setItems(items);
        calculateNutrition(plan);
        dietPlanMapper.insert(plan);
        saveItems(plan.getId(), items);


        return plan;
    }

    public DietPlan getDetail(Long id){
        DietPlan plan = dietPlanMapper.selectById(id);
        if(plan==null){
            throw new BusionessException("计划不存在");
        }
       List<DietPlanItem> items = dietPlanItemMapper.selectList(
                new LambdaQueryWrapper<DietPlanItem>()
                        .eq(DietPlanItem::getPlanId, id)
        );
        plan.setItems(items);
        attachUserInfo(plan);

        return plan;
    }

    public void delete(Long id,Long userId){
        DietPlan plan = dietPlanMapper.selectById(id);
        if(plan==null){
            throw new BusionessException("计划不存在");
        }
        //只能删除自己的数据
        if(userId!=null&&!plan.getUserId().equals(userId)){
            throw new BusionessException("您没有权限操作该计划");
        }
        dietPlanMapper.deleteById(id);
        dietPlanItemMapper.delete(new LambdaQueryWrapper<DietPlanItem>()
                .eq(DietPlanItem::getPlanId, id));
    }

    public Page<DietPlan>  listMyPlans(Page<DietPlan> page, Long userId, String date) {
        LambdaQueryWrapper<DietPlan> wrapper = new LambdaQueryWrapper<DietPlan>();
        if(StringUtils.hasText(date)){
            wrapper.eq(DietPlan::getPlanDate, LocalDate.parse(date));
        }
        wrapper.orderByDesc(DietPlan::getPlanDate);
       Page<DietPlan> result = dietPlanMapper.selectPage(page, wrapper);
       result.getRecords().forEach(this::attachPlanDetail);
       return result;
    }


    //用户信息
    private void attachUserInfo(DietPlan plan){
        if(plan.getUserId()!=null){
            SysUser user = sysUserMapper.selectById(plan.getUserId());
            if(user!=null){
                plan.setNickname(user.getNickname());
                plan.setUsername(user.getUsername());
            }

        }
    }

    // 根据食材智能生成饮食清单方法
    public DietPlan aiGeneratePlan(Long userId, String foodDescription) {
        if (!StringUtils.hasText(foodDescription)) {
            throw new BusionessException("请输入食材或需求描述");
        }

        UserHealthProfile profile = userHealthProfileService.getByUserId(userId);

        StringBuilder prompt = new StringBuilder();
        prompt.append("用户想用以下食材/需求来安排今日三餐：").append(foodDescription).append("\n\n");

        if (profile != null) {
            prompt.append("用户健康档案：\n");
            prompt.append("- 身高：").append(profile.getHeight()).append("cm，体重：").append(profile.getWeight()).append("kg\n");
            prompt.append("- 年龄：").append(profile.getAge()).append("，性别：").append(profile.getGender() == 1 ? "男" : "女").append("\n");
            if (profile.getTargetCalories() != null) {
                prompt.append("- 每日目标热量：").append(profile.getTargetCalories()).append("kcal\n");
            }
            if (StringUtils.hasText(profile.getDietPreference())) {
                prompt.append("- 饮食偏好：").append(profile.getDietPreference()).append("\n");
            }
            if (StringUtils.hasText(profile.getAllergens())) {
                prompt.append("- 过敏源（需避免）：").append(profile.getAllergens()).append("\n");
            }
        }

        prompt.append("\n请用这些食材搭配设计具体的菜谱/食谱（如番茄炒蛋、西兰花炒鸡胸肉），而不是直接列出食材。");
        prompt.append("每餐安排2-3道菜，合理搭配荤素。");
        prompt.append("请严格按照以下JSON格式返回，items是一个数组，每个元素包含mealType、foodName（具体菜谱名称）、calories、amount、unit：\n");
        prompt.append("{\n");
        prompt.append("  \"items\": [\n");
        prompt.append("    {\"mealType\": \"breakfast\", \"foodName\": \"牛奶燕麦粥\", \"calories\": 280, \"amount\": 1, \"unit\": \"份\"},\n");
        prompt.append("    {\"mealType\": \"lunch\", \"foodName\": \"西兰花炒鸡胸肉\", \"calories\": 350, \"amount\": 1, \"unit\": \"份\"},\n");
        prompt.append("    {\"mealType\": \"lunch\", \"foodName\": \"番茄蛋汤\", \"calories\": 120, \"amount\": 1, \"unit\": \"份\"},\n");
        prompt.append("    {\"mealType\": \"dinner\", \"foodName\": \"糙米饭配清蒸鱼\", \"calories\": 400, \"amount\": 1, \"unit\": \"份\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"totalCalories\": 0\n");
        prompt.append("}");

        String systemPrompt = "你是一个专业的营养师和中式家常菜厨师。请根据用户提供的食材和健康档案，"
                + "用这些食材设计具体的菜谱食谱（不要直接列出食材），合理安排三餐。"
                + "请严格按照指定的JSON格式返回，不要添加任何额外文字说明。";

        String jsonResult = aiService.chatForJson(systemPrompt, prompt.toString());

        // 解析AI返回结果
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Map<String, Object> parsed;
        try {
            parsed = mapper.readValue(jsonResult, Map.class);
        } catch (Exception e) {
            throw new BusionessException("AI生成结果解析失败，请重试");
        }

        DietPlan plan = new DietPlan();
        plan.setUserId(userId);
        plan.setPlanDate(LocalDate.now());
        plan.setPlanType("day");
        plan.setSource("recommend");
        plan.setStatus(0);

        List<DietPlanItem> items = new ArrayList<>();
        Object itemsList = parsed.get("items");
        if (itemsList instanceof List) {
            for (Object obj : (List<?>) itemsList) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) obj;
                    DietPlanItem item = new DietPlanItem();
                    item.setMealType(String.valueOf(m.getOrDefault("mealType", "lunch")));
                    item.setFoodName(String.valueOf(m.getOrDefault("foodName", "")));
                    Object cal = m.get("calories");
                    item.setCalories(cal instanceof Number ? BigDecimal.valueOf(((Number) cal).doubleValue()) : BigDecimal.ZERO);
                    Object amt = m.get("amount");
                    item.setAmount(amt instanceof Number ? BigDecimal.valueOf(((Number) amt).doubleValue()) : BigDecimal.valueOf(100));
                    item.setUnit(String.valueOf(m.getOrDefault("unit", "g")));
                    items.add(item);
                }
            }
        }

        plan.setItems(items);
        calculateNutrition(plan);
        dietPlanMapper.insert(plan);
        saveItems(plan.getId(), items);
        attachPlanDetail(plan);


        return plan;
    }

    public void createPlan(DietPlan plan,Long userId){
        plan.setUserId(userId);
        plan.setStatus(0);
        if(plan.getPlanType()==null){
            plan.setPlanType("day");
        }
        if(plan.getSource()==null){
            plan.setSource("manual");
        }
        //计算营养汇总
        calculateNutrition(plan);
        dietPlanMapper.insert(plan);
        //保存明细
        saveItems(plan.getId(), plan.getItems());
    }

    public void completePlan(Long id,Long userId){
        DietPlan plan = dietPlanMapper.selectById(id);
        if(plan==null){
            throw new BusionessException("计划不存在");
        }
        if(!plan.getUserId().equals(userId)){
            throw new BusionessException("计划不属于您");
        }
        if(plan.getStatus()==1){ throw new BusionessException("计划已完成"); }
        dietPlanMapper.updateById(plan);
    }




    //清单明细
    public void attachPlanDetail(DietPlan plan){
        if(plan.getId()!=null){
         List<DietPlanItem> items = dietPlanItemMapper.selectList(new LambdaQueryWrapper<DietPlanItem>()
                    .eq(DietPlanItem::getPlanId, plan.getId()));
         plan.setItems(items);
        }
    }
    //三餐解析方法
    private void parseMeal(Map<String, Object> parsed, String mealType, List<DietPlanItem> items) {
        Object meal = parsed.get(mealType);
        if (meal instanceof Map) {
            Map<String, Object> mealData = (Map<String, Object>) meal;
            DietPlanItem item = new DietPlanItem();
            item.setMealType(mealType);
            item.setFoodName(String.valueOf(mealData.getOrDefault("name", "")));
            Object cal = mealData.get("calories");
            if (cal instanceof Number) {
                item.setCalories(BigDecimal.valueOf(((Number) cal).doubleValue()));
            } else {
                item.setCalories(BigDecimal.ZERO);
            }
            item.setAmount(BigDecimal.ONE);
            item.setUnit("份");
            items.add(item);
        }
    }



    //计算营养汇总方法
    private void calculateNutrition(DietPlan plan) {
        BigDecimal totalCal = BigDecimal.ZERO;
        BigDecimal totalPro = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalCarb = BigDecimal.ZERO;

        if (plan.getItems() != null) {
            for (DietPlanItem item : plan.getItems()) {
                if (item.getCalories() != null) {
                    totalCal = totalCal.add(item.getCalories());
                }
            }
        }
        plan.setTotalCalories(totalCal);
        plan.setTotalProtein(totalPro);
        plan.setTotalFat(totalFat);
        plan.setTotalCarbohydrate(totalCarb);
    }

    //保存清单明细
    private void saveItems(Long planId, List<DietPlanItem> items) {
        if (items == null) return;
        for (DietPlanItem item : items) {
            item.setId(null);
            item.setPlanId(planId);
            dietPlanItemMapper.insert(item);
        }
    }
}



