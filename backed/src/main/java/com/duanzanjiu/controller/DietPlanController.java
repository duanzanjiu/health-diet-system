package com.duanzanjiu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.DietPlan;
import com.duanzanjiu.entity.DietPlanItem;
import com.duanzanjiu.service.DietPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/diet-plan")

public class DietPlanController {

    @Autowired
    private DietPlanService dietPlanService;

    @GetMapping("/list")
    public Result<Page<DietPlan>> listAll(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String source,
        @RequestParam(required = false) String date
        ) {
        Page<DietPlan> page = new Page<>(pageNum, pageSize);
        Page<DietPlan> result = dietPlanService.listAll(page, keyword, source, date);
        return Result.ok(result);
    }

    @PostMapping("/from-recommend/{recordId}")
    public Result<DietPlan> fromRecommend(@PathVariable Long recordId) {
        Long userId = getCurrentUserId();
        DietPlan plan = dietPlanService.createFromRecommend( recordId,userId);
        return Result.ok(plan);

    }

    @GetMapping("/{id}")
    public Result<DietPlan> GetDetail(@PathVariable Long id){
        DietPlan plan = dietPlanService.getDetail(id);
        return Result.ok(plan);
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        Long userId = getCurrentUserId();
        dietPlanService.delete(id,userId);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<Page<DietPlan>> myPlans(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        //@RequestParam(required = false) String keyword,
       // @RequestParam(required = false) String source,
        @RequestParam(required = false) String date
        ) {
        Long userId = getCurrentUserId();
        Page<DietPlan> page = new Page<>(pageNum, pageSize);
        Page<DietPlan> result = dietPlanService.listMyPlans(page,userId, date);
        return Result.ok(result);
    }

    @PostMapping("/ai-generate")
    public Result<DietPlan> aiGenerate(@RequestBody Map<String,String> body){
        Long userId = getCurrentUserId();
        String foodDescription = body.get("foodDescription");
        DietPlan plan = dietPlanService.aiGeneratePlan(userId,foodDescription);
        return Result.ok(plan);
    }

    @PostMapping
    public Result<?> create(@RequestBody DietPlan plan){
        Long userId = getCurrentUserId();
        dietPlanService.createPlan(plan,userId);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id){
        Long userId = getCurrentUserId();
        dietPlanService.completePlan(id,userId);
        return Result.ok("标记完成");
    }

    


    //获取用户id
    private Long getCurrentUserId(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return(Long) principal;
    }
}
