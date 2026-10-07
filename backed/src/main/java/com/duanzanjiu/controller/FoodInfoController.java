package com.duanzanjiu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.FoodInfo;
import com.duanzanjiu.entity.SysUser;
import com.duanzanjiu.service.FoodInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food/info")
public class FoodInfoController {
    @Autowired
    private FoodInfoService foodInfoService;



    @GetMapping("/list")
    public Result<Page<FoodInfo>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false ) String keyword,
        @RequestParam(required = false ) String categoryId
    ){
        Page<FoodInfo> page = new Page<>(pageNum,pageSize);
        Page<FoodInfo> result = foodInfoService.list(page,keyword,categoryId);
        return Result.ok(result);

    }

    @PostMapping
    public Result<?> add(@RequestBody FoodInfo foodInfo){
        foodInfoService.addFood(foodInfo);
        return Result.ok("添加成功");
    }


    @PutMapping
    public Result<?> update(@RequestBody FoodInfo foodInfo){
        foodInfoService.updateFood(foodInfo);
        return Result.ok("更新成功");
    }

    @GetMapping("/{id}")
    public Result<FoodInfo> getDetail(@PathVariable Long id){
        FoodInfo foodInfo = foodInfoService.getDetail(id);
        return Result.ok(foodInfo);
}

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        foodInfoService.deleteFood(id);
        return Result.ok("删除成功");
    }
}
