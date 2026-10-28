package com.zosh.controller;

import com.zosh.config.JwtProvider;
import com.zosh.model.TwoFactorOTP;
import com.zosh.model.User;
import com.zosh.repository.UserRepository;
import com.zosh.response.AuthResponse;
import com.zosh.services.CustomUserDetails;
import com.zosh.services.EmailService;
import com.zosh.services.TwoFactorOtpService;
import com.zosh.services.WatchListService;
import com.zosh.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomUserDetails customUserDetails;

    @Autowired
    private TwoFactorOtpService twoFactorOtpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WatchListService watchListService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) throws Exception {
        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if(isEmailExist!=null){
            throw new Exception("Email is already exists");
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setFullName(user.getFullName());
        User savedUser = userRepository.save(newUser);

        watchListService.createWatchList(savedUser);

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        user.getPassword()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(auth);
        String jwt = JwtProvider.generateToken(auth);
        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("Register Sucesss");


        return new ResponseEntity<>(res , HttpStatus.CREATED);
    }
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) throws Exception {
        String userName = user.getEmail();
        String password = user.getPassword();
        Authentication auth = authenticate(userName ,password);

        SecurityContextHolder.getContext()
                .setAuthentication(auth);
        String jwt = JwtProvider.generateToken(auth);
        User authUser = userRepository.findByEmail(userName);
        if (user.getTwoFactorAuth() != null &&
                user.getTwoFactorAuth().isEnabled()) {
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factor auth is enabled");
            res.setTwoFactorAuthEnabled(true);
            String otp = OtpUtils.generateOTP();
            TwoFactorOTP oldTwoFactorOTP = twoFactorOtpService.findByUser(authUser.getId());
            if(oldTwoFactorOTP!=null){
                twoFactorOtpService.deleteTwoFactorOtp(oldTwoFactorOTP);
            }
            TwoFactorOTP newTwoFactorOTP = twoFactorOtpService.createTwoFactorOtp(authUser , otp ,jwt);

            emailService.senderVerficationOtpEmail(userName ,otp);
            res.setSession(newTwoFactorOTP.getId());
            return new ResponseEntity<>(res ,HttpStatus.CREATED);
        }




        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setStatus(true);
        res.setMessage("Login Sucesss");


        return new ResponseEntity<>(res , HttpStatus.CREATED);
    }

    private Authentication authenticate(String userName, String password) {
        UserDetails userDetails = customUserDetails.loadUserByUsername(userName);
        if(userDetails==null){
            throw new BadCredentialsException("Invaild username");
        }
        if(!password.equals(userDetails.getPassword())){
            throw  new BadCredentialsException("invaild password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails ,password,userDetails.getAuthorities());
    }
    @PostMapping("/two-factor/otp/{otp}")
    public ResponseEntity<AuthResponse> verifySigingOtp(@PathVariable String otp ,@RequestParam String id){
        TwoFactorOTP twoFactorOTP = twoFactorOtpService.findById(id);

        if(twoFactorOtpService.verifyTwoFactorOtp(twoFactorOTP , otp)){
            AuthResponse res = new AuthResponse();
            res.setMessage("Two factoer authentication verified");
            res.setTwoFactorAuthEnabled(true);
            res.setJwt(twoFactorOTP.getJwt());
            return  new ResponseEntity<>(res,HttpStatus.OK);
        }
        throw new RuntimeException("Invalid Otp");
    }

}
