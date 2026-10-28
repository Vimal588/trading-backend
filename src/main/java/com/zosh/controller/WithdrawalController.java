package com.zosh.controller;

import com.zosh.model.User;
import com.zosh.model.Wallet;
import com.zosh.model.WalletTransactions;
import com.zosh.model.Withdrawal;

import com.zosh.services.TransactionService;
import com.zosh.services.UserService;
import com.zosh.services.WalletService;
import com.zosh.services.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class WithdrawalController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;


    // =========================
    // CREATE WITHDRAWAL REQUEST
    // =========================

    @PostMapping("/api/withdrawal/{amount}")
    public ResponseEntity<?> withdrawalRequest(
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt) throws Exception {

        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest()
                    .body("Withdrawal amount must be greater than zero");
        }

        User user = userService.findUserProfileByJwt(jwt);
        Wallet userWallet = walletService.getUserWallet(user);

        if (userWallet.getBalance() == null ||
                userWallet.getBalance().compareTo(
                        java.math.BigDecimal.valueOf(amount)) < 0) {
            return ResponseEntity.badRequest()
                    .body("Insufficient wallet balance");
        }

        Withdrawal withdrawal =
                withdrawalService.requestWithdrawal(amount, user);

        walletService.addBalance(userWallet, -amount);

        return ResponseEntity.ok(withdrawal);
    }


    // =========================
    // ADMIN PROCEED WITHDRAWAL
    // =========================

    @PatchMapping("/api/admin/withdrawal/{id}/proceed/{accept}")
    public ResponseEntity<?> proceedWithdrawal(
            @PathVariable Long id,
            @PathVariable boolean accept,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user = userService.findUserProfileByJwt(jwt); // kept only for JWT auth check

        Withdrawal withdrawal = withdrawalService.procedWithwithdrawal(id, accept);
        User withdrawalUser = withdrawal.getUser();
        Wallet withdrawalWallet = walletService.getUserWallet(withdrawalUser);

        if (!accept) {
            walletService.addBalance(
                    withdrawalWallet,   // fixed: refund the requester, not the admin
                    withdrawal.getAmount()
            );
        }

        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }

    // =========================
    // USER WITHDRAWAL HISTORY
    // =========================

    @GetMapping("/api/withdrawal")
    public ResponseEntity<List<Withdrawal>> getWithdrawalHistory(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserProfileByJwt(jwt);

        List<Withdrawal> withdrawals =
                withdrawalService.getUsersWithdrawalHistory(user);

        return new ResponseEntity<>(
                withdrawals,
                HttpStatus.OK
        );
    }


    // =========================
    // ADMIN GET ALL WITHDRAWALS
    // =========================

    @GetMapping("/api/admin/withdrawal")
    public ResponseEntity<List<Withdrawal>> getAllWithdrawalRequest(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserProfileByJwt(jwt);

        List<Withdrawal> withdrawals =
                withdrawalService.getAllWithdrawalRequest();

        return new ResponseEntity<>(
                withdrawals,
                HttpStatus.OK
        );
    }
}