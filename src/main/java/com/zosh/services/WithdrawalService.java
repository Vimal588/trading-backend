package com.zosh.services;

import com.zosh.model.User;
import com.zosh.model.Withdrawal;

import java.util.List;

public interface WithdrawalService {

    Withdrawal requestWithdrawal(Long amount , User user);


    Withdrawal procedWithwithdrawal(Long withdrawalId ,boolean accept) throws Exception;

    List<Withdrawal> getUsersWithdrawalHistory(User user);

    List<Withdrawal>  getAllWithdrawalRequest();

}
