package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入菜品
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据ID删除口味表的口味数据
     * @param id
     */
    @Delete("delete from dish_flavor where dish_flavor.id = #{id}")
    void deleteFlavorById(Long id);

    /**
     * 根据ID查询当前菜品的口味数据
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where dish_flavor.id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
