package com.example.markdownnotes.controller;

import com.example.markdownnotes.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profilePage(Principal principal, Model model) {
        model.addAttribute("username", principal.getName());
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("pwError", "Passwords do not match");
            return "redirect:/profile";
        }
        try {
            userService.changePassword(principal.getName(), oldPassword, newPassword);
            ra.addFlashAttribute("pwSuccess", "Password updated");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("pwError", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete-account")
    public String deleteAccount(@RequestParam String confirmUsername,
                                Principal principal,
                                RedirectAttributes ra) {
        if (!confirmUsername.equals(principal.getName())) {
            ra.addFlashAttribute("deleteError", "Username does not match");
            return "redirect:/profile";
        }
        userService.deleteAccount(principal.getName());
        SecurityContextHolder.clearContext();
        return "redirect:/login?deleted";
    }
}