package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.SetmealService;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入购物车的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

       //如果已经存在，商品数量加一
        if (shoppingCartList != null && shoppingCartList.size()>0) {
            ShoppingCart cart = shoppingCartList.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }else {
            //如果不存在，插入一条购物车数据
            //判断本次添加的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null){
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            } else {
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void deleteGood(ShoppingCartDTO shoppingCartDTO) {
        //获取当前微信用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);

        if(shoppingCart.getDishId() != null){
            //当前商品为菜品
            ShoppingCart cart = shoppingCartMapper.getByUserIdAndDishId(shoppingCart);
            //如果购物车中商品数量大于1 则对应商品数量减少1
            cart.setNumber(cart.getNumber() - 1);

            if(cart.getNumber() == 0){
                //如果减少后购物车中商品数量等于0 则删除购物车中的商品数据
                shoppingCartMapper.deleteByShoppingId(cart.getId());
            }else {
                //如果减少后购物车中商品数量>=1 则跟新购物车中的商品数据
                shoppingCartMapper.updateNumberById(cart);
            }
        }else {
            //当前商品为套餐
            ShoppingCart cart = shoppingCartMapper.getByUserIdAndSetmealId(shoppingCart);
            //如果购物车中商品数量大于1 则对应商品数量减少1
            cart.setNumber(cart.getNumber() - 1);

            if(cart.getNumber() == 0){
                //如果减少后购物车中商品数量等于0 则删除购物车中的商品数据
                shoppingCartMapper.deleteByShoppingId(cart.getId());
            }else {
                //如果减少后购物车中商品数量>=1 则跟新购物车中的商品数据
                shoppingCartMapper.updateNumberById(cart);
            }
        }
    }
}
