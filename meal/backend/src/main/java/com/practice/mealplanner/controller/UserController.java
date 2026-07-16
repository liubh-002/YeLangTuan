package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.User;
import com.practice.mealplanner.service.AuthService;
import com.practice.mealplanner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(HttpServletRequest request, @RequestBody Map<String, Object> updates) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(userService.updateUser(user.getId(), updates));
    }

    @GetMapping("/info/{userId}")
    public ResponseEntity<User> getUserInfo(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未提供认证令牌");
        }
        return authHeader.substring(7);
    }
}