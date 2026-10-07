package com.duanzanjiu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanzanjiu.entity.FoodCategory;
import com.duanzanjiu.entity.FoodInfo;
import com.duanzanjiu.entity.RecommendRecord;
import com.duanzanjiu.entity.SysUser;
import com.duanzanjiu.mapper.FoodCategoryMapper;
import com.duanzanjiu.mapper.FoodInfoMapper;
import com.duanzanjiu.mapper.FoodRecipeMapper;
import com.duanzanjiu.mapper.RecommendRecordMapper;
import com.duanzanjiu.mapper.SysUserMapper;
import com.duanzanjiu.mapper.UserHealthProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.duanzanjiu.entity.DietPlan;
import com.duanzanjiu.entity.DietPlanItem;
import com.duanzanjiu.entity.UserHealthProfile;
import com.duanzanjiu.mapper.DietPlanItemMapper;
import com.duanzanjiu.mapper.DietPlanMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private FoodInfoMapper foodInfoMapper;

    @Autowired
    private FoodRecipeMapper foodRecipeMapper;

    @Autowired
    private FoodCategoryMapper foodCategoryMapper;

    @Autowired
    private RecommendRecordMapper recommendRecordMapper;

    @Autowired
    private UserHealthProfileMapper userHealthProfileMapper;

    @Autowired
    private DietPlanMapper dietPlanMapper;

    @Autowired
    private DietPlanItemMapper dietPlanItemMapper;

    public Map<String, Object> getAdminStats() {
        String today = LocalDate.now().toString();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", sysUserMapper.selectCount(null));
        data.put("foodCount", foodInfoMapper.selectCount(null));
        data.put("recipeCount", foodRecipeMapper.selectCount(null));
        data.put("todayRecommendCount", recommendRecordMapper.selectCount(
                new LambdaQueryWrapper<RecommendRecord>().apply("DATE(create_time) = {0}", today)));
        data.put("todayNewUsers", sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().apply("DATE(create_time) = {0}", today)));
        data.put("profileCount", userHealthProfileMapper.selectCount(null));
        return data;
    }

    public Map<String, Object> getAdminCharts() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recommendTrend", buildRecommendTrend());
        data.put("categoryDistribution", buildCategoryDistribution());
        return data;
    }

    //用户概览面板
    public Map<String, Object> getUserOverview(Long userId) {
        Map<String, Object> data = new LinkedHashMap<>();

        //今日饮食计划
        List<DietPlan> todayPlans = dietPlanMapper.selectList(new LambdaQueryWrapper<DietPlan>()
                .eq(DietPlan::getUserId, userId)
                .eq(DietPlan::getPlanDate, LocalDate.now()));

        //遍历每个计划，累加其明细的热量
        BigDecimal todayCalories = BigDecimal.ZERO;
        int todayFoodCount = 0;
        for (DietPlan plan : todayPlans) {
            List<DietPlanItem> items = dietPlanItemMapper.selectList(new LambdaQueryWrapper<DietPlanItem>()
                    .eq(DietPlanItem::getPlanId, plan.getId()));
            for (DietPlanItem item : items) {
                if (item.getCalories() != null) {
                    todayCalories = todayCalories.add(item.getCalories());
                }
            }
            todayFoodCount += items.size();
        }

        //健康档案
        UserHealthProfile profile = userHealthProfileMapper.selectOne(new LambdaQueryWrapper<UserHealthProfile>()
                .eq(UserHealthProfile::getUserId, userId));

        data.put("todayCalories", todayCalories.setScale(0, RoundingMode.HALF_UP));
        data.put("todayPlanCount", todayPlans.size());
        data.put("todayFoodCount", todayFoodCount);
        data.put("hasProfile", profile != null);
        data.put("targetCalories", profile == null || profile.getTargetCalories() == null
                ? BigDecimal.ZERO : profile.getTargetCalories());
        data.put("recommendCount", recommendRecordMapper.selectCount(new LambdaQueryWrapper<RecommendRecord>()
                .eq(RecommendRecord::getUserId, userId)));
        data.put("planTotal", dietPlanMapper.selectCount(new LambdaQueryWrapper<DietPlan>()
                .eq(DietPlan::getUserId, userId)));

        return data;
    }

    private List<Map<String, Object>> buildRecommendTrend() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Long count = recommendRecordMapper.selectCount(new LambdaQueryWrapper<RecommendRecord>()
                    .apply("DATE(create_time) = {0}", date.toString()));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(formatter));
            item.put("count", count);
            trend.add(item);
        }
        return trend;
    }

    private List<Map<String, Object>> buildCategoryDistribution() {
        List<FoodCategory> categories = foodCategoryMapper.selectList(null);
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (FoodCategory category : categories) {
            Long count = foodInfoMapper.selectCount(new LambdaQueryWrapper<FoodInfo>()
                    .eq(FoodInfo::getCategoryId, category.getId()));
            if (count > 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", category.getCategoryName());
                item.put("value", count);
                distribution.add(item);
            }
        }
        return distribution;
    }
}