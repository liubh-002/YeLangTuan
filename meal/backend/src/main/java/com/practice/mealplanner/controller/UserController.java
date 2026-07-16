package com.practice.mealplanner.controller;

import com.practice.mealplanner.model.User;
import com.practice.mealplanner.repository.FamilyMemberRepository;
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
    private final FamilyMemberRepository familyMemberRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("phone", user.getPhone());
        result.put("nickname", user.getNickname());
        result.put("gender", user.getGender());
        result.put("age", user.getAge());
        result.put("monthSalary", user.getMonthSalary());
        result.put("tastePrefer", user.getTastePrefer());
        result.put("dietTaboo", user.getDietTaboo());
        // Check if this user is a sub-account
        boolean isSub = familyMemberRepository.findBySubAccountUserId(user.getId()).isPresent();
        result.put("isSubAccount", isSub);
        return ResponseEntity.ok(result);
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