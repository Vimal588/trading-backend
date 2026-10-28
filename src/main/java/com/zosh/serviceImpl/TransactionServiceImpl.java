package com.zosh.serviceImpl;

import com.zosh.model.Wallet;
import com.zosh.model.WalletTransactions;
import com.zosh.repository.WalletTransactionRepository;
import com.zosh.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Override
    public List<WalletTransactions> getTransactionsByWallet(Wallet wallet) {

        return walletTransactionRepository.findByWallet(wallet);
    }
}
