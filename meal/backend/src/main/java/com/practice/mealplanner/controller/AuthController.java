package com.practice.mealplanner.controller;

import com.practice.mealplanner.dto.ForgotPasswordRequest;
import com.practice.mealplanner.dto.UserLoginRequest;
import com.practice.mealplanner.dto.UserRegisterRequest;
import com.practice.mealplanner.dto.VerifyCodeRequest;
import com.practice.mealplanner.model.User;
import com.practice.mealplanner.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest request) {
        User user = authService.register(request);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("token", user.getPhone());
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("name", user.getName());
        result.put("nickname", user.getNickname());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UserLoginRequest request) {
        User user = authService.login(request.getPhone(), request.getPassword());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("token", user.getPhone());
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("nickname", user.getNickname());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> sendVerifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        String code = authService.generateVerifyCode(request.getPhone());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("code", code);
        result.put("message", "验证码已发送");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        if (!authService.verifyCode(request.getPhone(), request.getVerifyCode())) {
            throw new RuntimeException("验证码错误");
        }
        authService.updatePassword(request.getPhone(), request.getNewPassword());
        Map<String, String> result = new HashMap<String, String>();
        result.put("message", "密码修改成功");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        if (!authService.verifyCode(request.getPhone(), request.getVerifyCode())) {
            throw new RuntimeException("验证码错误");
        }
        authService.updatePassword(request.getPhone(), request.getNewPassword());
        Map<String, String> result = new HashMap<String, String>();
        result.put("message", "密码修改成功");
        return ResponseEntity.ok(result);
    }
}