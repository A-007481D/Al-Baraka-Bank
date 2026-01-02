package com.albaraka_bank.web;

import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import com.albaraka_bank.modules.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWebController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        long activeUsersCount = users.stream().filter(User::isActive).count();
        long agentUsersCount = users.stream()
                .filter(u -> u.getRole() == UserRole.AGENT_BANCAIRE).count();

        model.addAttribute("activeUsersCount", activeUsersCount);
        model.addAttribute("agentUsersCount", agentUsersCount);

        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Email already exists");
                return "redirect:/admin/users";
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setActive(true);
            User savedUser = userRepository.save(user);

            if (user.getRole() == UserRole.CLIENT) {
                accountService.createAccount(savedUser);
            }

            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
        redirectAttributes.addFlashAttribute("success", "User activated");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
        redirectAttributes.addFlashAttribute("success", "User deactivated");
        return "redirect:/admin/users";
    }
}
