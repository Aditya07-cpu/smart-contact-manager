package com.smart.smartcontactmanager.Controller;

import com.smart.smartcontactmanager.Helper.Message;
import com.smart.smartcontactmanager.Model.User;
import com.smart.smartcontactmanager.Repository.UserRepository;
import com.smart.smartcontactmanager.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/forgot")
    public String openEmailFrom() {
        return "forgot_email_form";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("email") String email, HttpSession session) {

        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);

        String subject = "OTP FROM SCM";
        String body = "OTP: " + otp + " ";

        boolean flag = false;
        flag = emailService.sendEmail(email, subject, body);

        if(flag) {
            session.setAttribute("otp", otp);
            session.setAttribute("email", email);
            return "verify_otp";
        }
        else {
            session.setAttribute("message", new Message("Check Your Email Id !!", "alert-danger"));
            return "forgot_email_form";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
        int myOtp = (int) session.getAttribute("otp");
        String email = (String) session.getAttribute("email");

        User user = userRepository.getUserByUsername(email);

        if(user == null) {
            session.setAttribute("message", new Message("User Does Not Exists With This Email Id.", "alert-danger"));
            return "forgot_email_form";
        }
        else {

        }

        if (myOtp == otp) {
            return "password_change_form";
        }
        else {
            session.setAttribute("message", new Message("You Have Entered Wrong OTP.", "alert-danger"));
            return "verify_otp";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User user = userRepository.getUserByUsername(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "redirect:/signin?change=password changed successfully";
    }
}
