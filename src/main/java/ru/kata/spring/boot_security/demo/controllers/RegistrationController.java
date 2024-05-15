package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserService;

@Controller
@RequestMapping("/registration")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;

    }

    @GetMapping("/new")
    public String showRegistrationForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "register-new-user";
    }

    @PostMapping("/new")
    public String regNewUser(@ModelAttribute("user") User user,
                             Model model) {
        if (!userService.saveUser(user, "ROLE_USER")) {
            model.addAttribute("message", "User already exists");
            return "register-new-user";
        }
        return "redirect:/login";
    }

}
