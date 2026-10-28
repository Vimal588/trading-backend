package com.zosh.services;

import com.zosh.model.Wallet;
import com.zosh.model.WalletTransactions;

import java.util.List;

public interface TransactionService {



    List<WalletTransactions> getTransactionsByWallet(Wallet wallet);
//
//    WalletTransactions createTransaction(
//            Wallet wallet,
//            WalletTransaction type,
//            Long referenceId,
//            String description,
//            Long amount
//  );
}