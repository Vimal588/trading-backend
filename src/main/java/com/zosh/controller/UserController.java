package com.zosh.controller;

import com.zosh.domain.VerificationType;
import com.zosh.model.ForgotPasswordToken;
import com.zosh.model.User;
import com.zosh.model.VerificationCode;
import com.zosh.request.ForgotPasswordTokenRequest;
import com.zosh.request.ResetPasswordRequest;
import com.zosh.response.ApiResponse;
import com.zosh.response.AuthResponse;
import com.zosh.services.EmailService;
import com.zosh.services.ForgotPasswordService;
import com.zosh.services.UserService;
import com.zosh.services.VerificationCodeService;
import com.zosh.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    private String jwt;
    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt) throws Exception {

    User user = userService.findUserProfileByJwt(jwt);

    return new ResponseEntity<User>(user,HttpStatus.OK);
}
    @PostMapping ("/api/users/verification/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationType(@RequestHeader("Authorization") String jwt , @PathVariable VerificationType verificationType) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);

        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        if(verificationCode==null){
            verificationCode=verificationCodeService.sendVerificationCode(user,verificationType);
        }
        if(verificationType.equals(VerificationType.EMAIL)){
            emailService.senderVerficationOtpEmail(user.getEmail(),verificationCode.getOtp());
        }

        return new ResponseEntity<>("Verification OTP Send Successfully", HttpStatus.OK);
    }
    @PatchMapping("/api/users/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<User> enableTwoFactorAuthentication(
            @PathVariable String otp,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        String sendTo = verificationCode.getVerificationType().equals(VerificationType.EMAIL) ? verificationCode.getEmail() : verificationCode.getMobile();
        boolean isVerified = verificationCode.getOtp().equals(otp);
        if(isVerified){
            User updateUser =userService.enableTwoFactorAuthentication(verificationCode.getVerificationType(),sendTo,user);
            verificationCodeService.deleteVerificationCodeById(verificationCode);
            return new ResponseEntity<>(updateUser ,HttpStatus.OK);
        }
      throw new Exception("wrong otp");
    }

    @PostMapping("/auth/users/reset-password/send-otp")
    public ResponseEntity<AuthResponse> sendForgotPasswordOtp(@RequestBody ForgotPasswordTokenRequest req) throws Exception {
        User user = userService.findUserByEmail(req.getSendTo());
        if (user == null) {
            throw new Exception("No account registered with email: " + req.getSendTo());
        }
        String otp = OtpUtils.generateOTP();
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        ForgotPasswordToken token = forgotPasswordService.findByUser(user.getId());
        if (token != null) {
            forgotPasswordService.deleteToken(token);
        }

        VerificationType verificationType = req.getVerificationType() != null ? req.getVerificationType() : VerificationType.EMAIL;
        token = forgotPasswordService.createToken(user, id, otp, verificationType, req.getSendTo());

        if (VerificationType.EMAIL.equals(verificationType)) {
            emailService.senderVerficationOtpEmail(user.getEmail(), token.getOtp());
        }

        AuthResponse response = new AuthResponse();
        response.setSession(token.getId());
        response.setMessage("Password reset OTP sent successfully. (Code: " + token.getOtp() + ")");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/auth/users/reset-password/verify-otp")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String id,
            @RequestBody ResetPasswordRequest req
    ) throws Exception {

        ForgotPasswordToken forgotPasswordToken = forgotPasswordService.findById(id);
        if (forgotPasswordToken == null) {
            throw new Exception("Invalid or expired reset session. Please request a new OTP.");
        }
        boolean isVerified = forgotPasswordToken.getOtp().equals(req.getOtp());
        if (isVerified) {
            userService.updatePassword(forgotPasswordToken.getUser(), req.getPassword());
            forgotPasswordService.deleteToken(forgotPasswordToken);
            ApiResponse response = new ApiResponse();
            response.setMessage("Password updated successfully");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        }
        throw new Exception("Wrong OTP");
    }
}
