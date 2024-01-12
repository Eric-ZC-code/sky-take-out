package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //清理缓存数据
        String pattern = "dish_" + dishDTO.getCategoryId();
        cacheClean(pattern);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation(value = "批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品的批量删除：{}", ids);
        dishService.deleteBatch(ids);

        //清理缓存数据
        String pattern = "dish_*";
        cacheClean(pattern);

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("查询菜品：{}", id);
        DishVO dishVo = dishService.getById(id);
        return Result.success(dishVo);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    @PutMapping
    @ApiOperation(value = "修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.update(dishDTO);

        //清理缓存数据
        String pattern = "dish_*";
        cacheClean(pattern);

        return Result.success();
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status, id);

        //清理缓存数据
        String pattern = "dish_*";
        cacheClean(pattern);

        return Result.success();
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cacheClean(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
