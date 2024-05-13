package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private UserService userService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private RoleRepository roleRepository;
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setbCryptPasswordEncoder(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @GetMapping
    public String getAdminPage() {
        return "admin";
    }

    @GetMapping("/users-list")
    public String getAllUsers(Model model) {
        List<User> users = userService.allUsers();
        model.addAttribute("users", users);
        return "users-list";
    }

    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "add-user";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute("user") User user, @RequestParam("role") String role, Model model) {
        if (!userService.saveUser(user, role)) {
            model.addAttribute("message", "User already exists");
            return "add-user";
        }
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "edit-user";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") User user, @RequestParam("roleId") Long roleId, Model model) {
        User userFromDB = userRepository.findById(user.getId()).orElse(null);
        Role role = roleRepository.findById(roleId).orElse(null);

        if (userFromDB == null || role == null) {
            model.addAttribute("message", "User or role not found");
            return "edit-user";
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        userFromDB.setRoles(roles);
        userFromDB.setUsername(user.getUsername());
        userFromDB.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userRepository.save(userFromDB);
        return "redirect:/admin";
    }

    @GetMapping("show-info/{id}")
    public String showInfo(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id) {
        User user = userService.findUserById(id);
        userRepository.delete(user);
        return "redirect:/admin/users-list";
    }
}