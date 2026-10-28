package com.zosh.serviceImpl;

import com.zosh.domain.OrderType;
import com.zosh.model.Order;
import com.zosh.model.User;
import com.zosh.model.Wallet;
import com.zosh.repository.WalletRepository;
import com.zosh.services.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public Wallet getUserWallet(User user) {
        Wallet wallet = walletRepository.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUser(user);
            walletRepository.save(wallet);
        }
        return wallet;
    }

    @Override
    public Wallet addBalance(Wallet wallet, Long money) {
        BigDecimal balance = wallet.getBalance();
        BigDecimal newBalance = balance.add(BigDecimal.valueOf(money));
        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findWalletById(Long id) throws Exception {
        Optional<Wallet> wallet = walletRepository.findById(id);
        if (wallet.isPresent()) {
            return wallet.get();
        }
        throw new Exception("Wallet not Found");
    }

    @Transactional
    @Override
    public Wallet walletToWalletTransfer(
            User sender,
            Wallet receiverWallet,
            Long amount) throws Exception {

        Wallet senderWallet = getUserWallet(sender);

        BigDecimal transferAmount =
                BigDecimal.valueOf(amount);

        if (amount <= 0) {
            throw new Exception("Transfer amount must be greater than zero");
        }

        if(senderWallet.getBalance()
                .compareTo(BigDecimal.valueOf(amount)) < 0) {

            throw new Exception("Insufficient balance");
        }

        BigDecimal senderBalance =
                senderWallet.getBalance()
                        .subtract(transferAmount);

        senderWallet.setBalance(senderBalance);

        BigDecimal receiverBalance =
                receiverWallet.getBalance()
                        .add(transferAmount);

        receiverWallet.setBalance(receiverBalance);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        return senderWallet;
    }

    @Override
    public Wallet payOrderPayment(Order order, User user) throws Exception {

        Wallet wallet = getUserWallet(user);

        BigDecimal balance = wallet.getBalance();

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }

        if (order.getOrderType().equals(OrderType.BUY)) {

            // Check first
            if (balance.compareTo(order.getPrice()) < 0) {
                throw new Exception("Insufficient funds for this transaction");
            }

            // Then subtract
            BigDecimal newBalance =
                    balance.subtract(order.getPrice());

            wallet.setBalance(newBalance);

        } else {

            // SELL
            BigDecimal newBalance =
                    balance.add(order.getPrice());

            wallet.setBalance(newBalance);
        }

        return walletRepository.save(wallet);
    }
}
