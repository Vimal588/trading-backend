package com.zosh.serviceImpl;

import com.zosh.domain.VerificationType;
import com.zosh.model.ForgotPasswordToken;
import com.zosh.model.User;
import com.zosh.repository.ForgotPasswordRepository;
import com.zosh.services.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForgotPasswordImpl implements ForgotPasswordService {
    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Override
    public ForgotPasswordToken createToken(User user, String id, String otp, VerificationType verificationType, String sendTo) {
        ForgotPasswordToken token = new ForgotPasswordToken();
        token.setId(id);
        token.setOtp(otp);
        token.setUser(user);
        token.setVerificationType(verificationType);
        token.setSendTo(sendTo);
        return forgotPasswordRepository.save(token);
    }

    @Override
    public ForgotPasswordToken findById(String id) {
        Optional<ForgotPasswordToken> token = forgotPasswordRepository.findById(id);

        return token.orElse(null);
    }

    @Override
    public ForgotPasswordToken findByUser(Long userId) {

        return forgotPasswordRepository.findByUserId(userId);
    }

    @Override
    public void deleteToken(ForgotPasswordToken token) {
  forgotPasswordRepository.delete(token);
    }
}
