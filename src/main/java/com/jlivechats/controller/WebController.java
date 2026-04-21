package com.jlivechats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.jlivechats.service.AuthenticationService;
import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    private final AuthenticationService authenticationService;

    @Autowired
    public WebController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            return "redirect:/chat";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            return "redirect:/chat";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, 
                       Model model, HttpSession session) {
        if (authenticationService.login(username, password)) {
            session.setAttribute("username", username);
            return "redirect:/chat";
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email, 
                          @RequestParam String password, @RequestParam String confirmPassword,
                          Model model, HttpSession session) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        if (authenticationService.register(username, email, password)) {
            session.setAttribute("username", username);
            return "redirect:/chat";
        }
        model.addAttribute("error", "Registration failed - username may already exist");
        return "register";
    }

    @GetMapping("/chat")
    public String chatPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        return "chat";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
