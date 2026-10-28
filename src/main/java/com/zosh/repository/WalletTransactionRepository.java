package com.zosh.repository;

import com.zosh.model.Wallet;
import com.zosh.model.WalletTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransactions, Long> {

    List<WalletTransactions> findByWallet(Wallet wallet);
}