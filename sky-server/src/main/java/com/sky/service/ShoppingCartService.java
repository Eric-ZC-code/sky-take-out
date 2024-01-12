package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ShoppingCartService {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);


    List<ShoppingCart> showShoppingCart();

    void cleanShoppingCart();

    void deleteGood(ShoppingCartDTO shoppingCartDTO);
}
