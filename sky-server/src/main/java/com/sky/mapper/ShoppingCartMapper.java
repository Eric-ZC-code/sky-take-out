package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据ID修改商品数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 清空购物车数据
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 获得购物车一个菜品数据
     * @param shoppingCart
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId} and dish_id = #{dishId};")
    ShoppingCart getByUserIdAndDishId(ShoppingCart shoppingCart);

    /**
     * 获得购物车一个套餐数据
     * @param shoppingCart
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId} and setmeal_id = #{setmealId};")
    ShoppingCart getByUserIdAndSetmealId(ShoppingCart shoppingCart);

    /**
     * 删除购物车的一个商品数据
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteByShoppingId(Long id);

}
