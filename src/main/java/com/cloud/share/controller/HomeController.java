package com.cloud.share.controller;

import com.cloud.share.dto.PasswordResetRequest;
import com.cloud.share.exception.ResourceNotFoundException;
import com.cloud.share.service.HomeService;
import com.cloud.share.service.UserService;
import com.cloud.share.util.CommonUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;


@Tag(name="Some User operation",description = "basic user level Apis")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    // slf4j
    Logger log =  LoggerFactory.getLogger(HomeController.class);


    @Autowired
    private HomeService homeService;

    @Autowired
    private UserService userService;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUserAccount(@RequestParam Integer uid, @RequestParam String token) throws ResourceNotFoundException {
        log.info("HomeController  :: verifyUserAccount :: Execution of verifyUserAccount" );
        Boolean result = homeService.verifyUserAccount(uid, token);
        if (result) {
            return CommonUtil.createBuildResponseMessage("Account Verification successfully", HttpStatus.OK);
        }
        return CommonUtil.createErrorResponseMessage("Invalid verification link", HttpStatus.BAD_REQUEST);
    }


    // password reset

    @GetMapping("/send-email")
    public ResponseEntity<?> sendEmailForPasswordReset(@RequestParam String email, HttpServletRequest request) throws ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        String url = CommonUtil.getUrl(request);
        userService.sendEmailPasswordReset(email, url);
        return CommonUtil.createBuildResponseMessage("Email Send Successfully !! check mail and reset password", HttpStatus.OK);

    }

    @GetMapping("/verify-password-link")
    public ResponseEntity<?> verifyPasswordResetToken(@RequestParam Integer uid, @RequestParam String token) throws ResourceNotFoundException {
        userService.verifyPasswordResetLink(uid, token);
        return CommonUtil.createBuildResponseMessage("Verified Successfully", HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) throws ResourceNotFoundException {
        userService.resetPassword(passwordResetRequest);
        return CommonUtil.createBuildResponseMessage("Password Reset Successfully", HttpStatus.OK);
    }
}
