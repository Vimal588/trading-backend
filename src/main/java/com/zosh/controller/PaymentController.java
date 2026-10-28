package com.zosh.controller;

import com.zosh.domain.PaymentMethod;
import com.zosh.model.PaymentDetails;
import com.zosh.model.PaymentOrder;
import com.zosh.model.User;
import com.zosh.repository.UserRepository;
import com.zosh.response.PaymentResponse;
import com.zosh.services.PaymentService;
import com.zosh.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

public class PaymentController {
    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/api/payment/{paymentMethod}/amount/{amount}")
    public ResponseEntity<PaymentResponse> paymentHandler(
            @PathVariable PaymentMethod paymentMethod,
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt
            ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        PaymentResponse paymentResponse;

        PaymentOrder order = paymentService.createOrder(user , amount ,paymentMethod);
        if(paymentMethod.equals(PaymentMethod.RAZORPAY)){
            paymentResponse = paymentService.createRazorpayPaymentLink(user,amount, order.getId());
        }else{
            paymentResponse = paymentService.createStripePaymentLink(user ,amount , order.getId());

        }
        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }
}
