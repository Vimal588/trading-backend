package com.zosh.services;

import com.zosh.model.PaymentDetails;
import com.zosh.model.User;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;

public interface PaymentDetailService {
    public PaymentDetails addPaymentDetails(String accountNumber ,
                                            String accountHolderName,
                                            String ifsc,
                                            String bankName,
                                            User user);

    public PaymentDetails getUserPaymentDetails(User user);
}
