
package com.zosh.controller;

import com.zosh.model.PaymentDetails;
import com.zosh.model.User;
import com.zosh.services.PaymentDetailService;
import com.zosh.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentDetailController {

    @Autowired
    private PaymentDetailService paymentDetailService;

    @Autowired
    private UserService userService;

    // Add or update payment details
    @PostMapping("payment-details")
    public ResponseEntity<PaymentDetails> addPaymentDetails(
            @RequestBody PaymentDetails paymentDetails,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        PaymentDetails savedPaymentDetails = paymentDetailService.addPaymentDetails(
                paymentDetails.getAccountNumber(),
                paymentDetails.getAccountHolderName(),
                paymentDetails.getIfsc(),
                paymentDetails.getBankName(),
                user
        );

        return new ResponseEntity<>(savedPaymentDetails, HttpStatus.CREATED); // fixed
    }

    @GetMapping("/payment-details")
    public ResponseEntity<PaymentDetails> getPaymentDetails(
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);
        PaymentDetails paymentDetails = paymentDetailService.getUserPaymentDetails(user);

        return new ResponseEntity<>(paymentDetails, HttpStatus.OK); // fixed: GET should be 200, not 201
    }
}
