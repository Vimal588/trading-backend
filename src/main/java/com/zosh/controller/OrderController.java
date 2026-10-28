package com.zosh.controller;

import com.zosh.domain.OrderType;
import com.zosh.model.Coin;
import com.zosh.model.Order;
import com.zosh.model.User;
import com.zosh.model.WalletTransactions;
import com.zosh.request.CreateOrderRequest;
import com.zosh.services.CoinService;
import com.zosh.services.OrderService;
import com.zosh.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

    private String jwt;

    public OrderController() throws Exception {
    }

//    @Autowired
//    private WalletTransactionsService walletTransactionsService

    @PostMapping("/pay")
    public ResponseEntity<Order> payOrderPayment(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateOrderRequest req
    )throws Exception {

        User user = userService.findUserProfileByJwt(jwt);
        Coin coin = coinService.findById(req.getCoinId());

        Order order = orderService.processOrder(coin, req.getQuantity(), req.getOrderType(),user);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@RequestHeader ("Authorization") String jwtToken , @PathVariable Long orderId) throws Exception {

        User user = userService.findUserProfileByJwt(jwtToken);

        Order order = orderService.getOrderById(orderId);

        if(order.getUser().getId().equals(user.getId())){
            return ResponseEntity.ok(order);
        }else{
            throw new Exception("You don't have access");
        }
    }


    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrderForUser(@RequestHeader ("Authorization") String jwt , @RequestParam(required = false) OrderType order_type , @RequestParam(required = false) String asset_symbol ) throws Exception {


        Long userId = userService.findUserProfileByJwt(jwt).getId();

        List<Order> userOrders = orderService.getAllOrdersOfUser(userId ,order_type ,asset_symbol);


            return ResponseEntity.ok(userOrders);

    }



}
