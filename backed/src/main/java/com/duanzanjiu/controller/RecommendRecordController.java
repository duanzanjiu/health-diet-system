package com.duanzanjiu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.RecommendRecord;
import com.duanzanjiu.service.RecommendRecordService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend/record")
public class RecommendRecordController {

    @Autowired
    private RecommendRecordService recommendRecordService;

    @GetMapping("/today")
    public Result<RecommendRecord> tody(){
        Long userId = getCurrentUserId();
        RecommendRecord recommendRecord = recommendRecordService.getTodayRecommend(userId);
        return Result.ok(recommendRecord);

    }

    @PostMapping("/generate")
    public Result<RecommendRecord> aiRecommend(){
        Long userId = getCurrentUserId();
        RecommendRecord recommendRecord = recommendRecordService.generateRecommend(userId);
        return Result.ok(recommendRecord);

    }

    @PostMapping("/ai")
    public Result<RecommendRecord> aiFeedback(@RequestBody AiRecommendRequest request){
        String userRequest = request.getUserRequest();
        Long userId = getCurrentUserId();
        RecommendRecord recommendRecord = recommendRecordService.aiGenerateRecommend(userId,userRequest);
        return Result.ok(recommendRecord);
    }

    @PostMapping("/feedback")
    public Result<?> feedback(@RequestBody FeedbackRequest request){
        Long userId = getCurrentUserId();
        String feedback = request.getFeedback();
        recommendRecordService.feedback(userId,feedback, request.getRecordId());
        return Result.ok("反馈成功");
    }

    @GetMapping("/list")
    public Result<Page<RecommendRecord>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String source
    ){
        Page<RecommendRecord> page=new Page<>(pageNum,pageSize);
        Page<RecommendRecord> result = recommendRecordService.listRecords(page, date, source);
        return Result.ok(result);
    }

    @GetMapping("/my")
    public Result<Page<RecommendRecord>> myRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String date
    ){
        Long userId = getCurrentUserId();
        Page<RecommendRecord> page=new Page<>(pageNum,pageSize);
        Page<RecommendRecord> result = recommendRecordService.listMyRecords(page, userId,date);
        return Result.ok(result);
    }

    //获取用户id的方法

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (Long) principal;
    }

    @Data
    static class AiRecommendRequest{
        private String userRequest;
    }

    @Data
    static class FeedbackRequest{
        private String feedback;
        private Long recordId;
    }
}
