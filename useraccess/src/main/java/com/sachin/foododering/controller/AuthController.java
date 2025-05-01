package com.sachin.foododering.controller;

import com.sachin.foododering.model.User;
import com.sachin.foododering.security.JwtUtil;
import com.sachin.foododering.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    // ==============================
    // 🚀 Customer Registration
    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        User user = userService.registerUser(username, password, "CUSTOMER");
        return ResponseEntity.ok(user);
    }

    // 🚀 Admin Registration
    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        User user = userService.registerUser(username, password, "ADMIN");
        return ResponseEntity.ok(user);
    }

    // ==============================
    // 🔒 Customer Login
    @PostMapping("/login/customer")
    public ResponseEntity<?> loginCustomer(@RequestBody Map<String, String> request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.get("username"),
                        request.get("password")
                )
        );

        User user = userService.findByUsername(request.get("username")).orElse(null);
        if (user == null || !user.getRole().equals("CUSTOMER")) {
            return ResponseEntity.status(403).body("Not authorized as customer");
        }

        String token = jwtUtil.generateToken(authentication);
        return ResponseEntity.ok(Map.of("token", token));
    }

    // 🔒 Admin Login
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.get("username"),
                        request.get("password")
                )
        );

        User user = userService.findByUsername(request.get("username")).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("Not authorized as admin");
        }

        String token = jwtUtil.generateToken(authentication);
        return ResponseEntity.ok(Map.of("token", token));
    }

    // Add to AuthController
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        return ResponseEntity.ok(user);
    }

}
