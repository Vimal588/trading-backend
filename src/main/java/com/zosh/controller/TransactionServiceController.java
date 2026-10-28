package com.zosh.controller;

import com.zosh.model.User;
import com.zosh.model.Wallet;
import com.zosh.model.WalletTransactions;
import com.zosh.services.TransactionService;
import com.zosh.services.UserService;
import com.zosh.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionServiceController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/api/transactions")
    public ResponseEntity<List<WalletTransactions>> getUserWallet(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        Wallet wallet = walletService.getUserWallet(user);

        List<WalletTransactions> transactionsList =
                transactionService.getTransactionsByWallet(wallet);

        return new ResponseEntity<>(
                transactionsList,
                HttpStatus.ACCEPTED
        );
    }
}