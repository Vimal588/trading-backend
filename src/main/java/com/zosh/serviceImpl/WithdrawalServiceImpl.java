
package com.zosh.serviceImpl;

import com.zosh.domain.WithdrawalStatus;
import com.zosh.model.User;
import com.zosh.model.Withdrawal;
import com.zosh.repository.WithdrawalRepository;
import com.zosh.services.WithdrawalService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;

    public WithdrawalServiceImpl(
            WithdrawalRepository withdrawalRepository) {
        this.withdrawalRepository = withdrawalRepository;
    }

    @Override
    @Transactional
    public Withdrawal requestWithdrawal(Long amount, User user) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(amount);
        withdrawal.setUser(user);
        withdrawal.setStatus(WithdrawalStatus.PENDING);

        return withdrawalRepository.save(withdrawal);
    }

    @Override
    @Transactional
    public Withdrawal procedWithwithdrawal(
            Long withdrawalId, boolean accept) throws Exception {

        Optional<Withdrawal> optional =
                withdrawalRepository.findById(withdrawalId);

        if (optional.isEmpty()) {
            throw new Exception("Withdrawal not found");
        }

        Withdrawal withdrawal = optional.get();

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException(
                    "This withdrawal has already been processed");
        }

        withdrawal.setDate(LocalDateTime.now());

        if (accept) {
            withdrawal.setStatus(WithdrawalStatus.SUCCESS);
        } else {
            // Add REJECTED to the enum before using this status.
            withdrawal.setStatus(WithdrawalStatus.REJECTED);
        }

        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public List<Withdrawal> getUsersWithdrawalHistory(User user) {
        return withdrawalRepository.findByUserId(user.getId());
    }

    @Override
    public List<Withdrawal> getAllWithdrawalRequest() {
        return withdrawalRepository.findAll();
    }
}
