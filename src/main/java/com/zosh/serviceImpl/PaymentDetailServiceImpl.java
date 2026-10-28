package com.zosh.serviceImpl;

import com.zosh.model.PaymentDetails;
import com.zosh.model.User;
import com.zosh.repository.PaymentDetailRepository;
import com.zosh.services.PaymentDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentDetailServiceImpl implements PaymentDetailService {

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Override
    public PaymentDetails addPaymentDetails(
            String accountNumber,
            String accountHolderName,
            String ifsc,
            String bankName,
            User user) {

        PaymentDetails paymentDetails = new PaymentDetails();

        paymentDetails.setAccountNumber(accountNumber);
        paymentDetails.setAccountHolderName(accountHolderName);
        paymentDetails.setIfsc(ifsc);
        paymentDetails.setBankName(bankName);
        paymentDetails.setUser(user);
        return paymentDetailRepository.save(paymentDetails);
    }

    @Override
    public PaymentDetails getUserPaymentDetails(User user) {
        return paymentDetailRepository.findByUserId(user.getId());
    }
}