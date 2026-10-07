package com.duanzanjiu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.entity.FoodRecipe;
import com.duanzanjiu.entity.FoodRecipeItem;
import com.duanzanjiu.service.FoodRecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food/recipe")
public class FoodRecipeController  {

    @Autowired
    private FoodRecipeService foodRecipeService;

    @GetMapping("/list")
    public Result<Page<FoodRecipe>> list
            (@RequestParam(defaultValue = "1") Integer pageNum,
             @RequestParam(defaultValue = "10") Integer pageSize,
             @RequestParam(required = false ) String keyword,
             @RequestParam(required = false ) String mealType)
    {
       Page<FoodRecipe> page = new Page<>(pageNum, pageSize);
      Page<FoodRecipe> result = foodRecipeService.listRecipes(page, keyword, mealType);
        return Result.ok(result);
    }

    @PostMapping
    public Result<?> add(@RequestBody FoodRecipe foodRecipe) {
        foodRecipeService.addRecipe(foodRecipe);
        return Result.ok("新增成功");
    }

    @PutMapping
    public Result<?> update(@RequestBody FoodRecipe foodRecipe) {
        foodRecipeService.updateRecipe(foodRecipe);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        foodRecipeService.deleteRecipe(id);
        return Result.ok("删除成功");
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        FoodRecipe foodRecipe = foodRecipeService.getById(id);
        return Result.ok(foodRecipe);   }
}
