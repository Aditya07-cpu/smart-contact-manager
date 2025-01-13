package com.smart.smartcontactmanager.Controller;

import com.smart.smartcontactmanager.Helper.Message;
import com.smart.smartcontactmanager.Model.User;
import com.smart.smartcontactmanager.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/signup")
    public String signup(Model model) {

        model.addAttribute("title", "Signup - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {

        try {
            if(!agreement) {
                System.out.println("You have not agreed the terms and conditions");
                throw new Exception("You have not agreed the terms and conditions");
            }

            if(result.hasErrors()) {
                System.out.println("ERROR" + result.toString());
                model.addAttribute("user", user);
                return "signup";
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            User savedUser = userRepository.save(user);

            model.addAttribute("user", new User());

            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "signup";
        }
        catch (Exception exception) {
            exception.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something Went Wrong !!" + exception.getMessage(), "alert-danger"));
            return "signup";
        }
    }

    @GetMapping("/signin")
    public String customLogin(Model model) {

        model.addAttribute("title", "Login Page - Smart Contact Manager");

        return "login";
    }
}
