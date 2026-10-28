package com.zosh.services;

import com.zosh.domain.OrderType;
import com.zosh.model.Coin;
import com.zosh.model.Order;
import com.zosh.model.OrderItem;
import com.zosh.model.User;

import java.util.List;

public interface OrderService {
    Order createOrder(User user , OrderItem orderItem , OrderType orderType);
    Order getOrderById(Long orderId) throws Exception;
    List<Order> getAllOrdersOfUser(Long userId , OrderType orderType , String assetSymbol);

    Order processOrder(Coin coin , double quantity ,OrderType orderType ,User user) throws Exception;

}
