package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleService roleService;

    public AdminController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder,
                           RoleService roleService) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleService = roleService;
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
    public String addUser(@ModelAttribute("user") User user,
                          @RequestParam("role") String role, Model model) {
        if (!userService.saveUser(user, role)) {
            model.addAttribute("message", "User already exists");
            return "add-user";
        }
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.findAll());
        return "edit-user";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") User user, @RequestParam("roleId") Long roleId, Model model) {
        User userFromDB = userService.findById(user.getId());
        Role role = roleService.findById(roleId);

        if (userFromDB == null || role == null) {
            model.addAttribute("message", "User or role not found");
            return "edit-user";
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        userFromDB.setRoles(roles);
        userFromDB.setUsername(user.getUsername());
        userFromDB.setEmail(user.getEmail()); //new
        userFromDB.setAge(user.getAge()); //new
        userFromDB.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userService.saveUser(userFromDB);
        return "redirect:/admin/users-list";
    }

    @GetMapping("show-info/{id}")
    public String showInfo(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users-list";
    }

    public String getRoleName(String roleName) {
        return roleName.contains("ADMIN") ? "Admin" : "User";
    }
}