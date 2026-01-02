package com.albaraka_bank.web;

import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final AuthService authService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/register";
            }

            authService.register(fullName, email, password, UserRole.CLIENT);
            redirectAttributes.addFlashAttribute("success",
                    "Account created successfully! You can now login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
