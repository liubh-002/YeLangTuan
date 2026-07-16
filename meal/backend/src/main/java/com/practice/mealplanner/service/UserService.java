package com.practice.mealplanner.service;

import com.practice.mealplanner.model.User;
import com.practice.mealplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User updateUser(Long userId, Map<String, Object> updates) {
        User user = getUserById(userId);
        if (updates.containsKey("nickname")) {
            user.setNickname((String) updates.get("nickname"));
        }
        if (updates.containsKey("gender")) {
            user.setGender((String) updates.get("gender"));
        }
        if (updates.containsKey("age")) {
            user.setAge((Integer) updates.get("age"));
        }
        if (updates.containsKey("monthSalary")) {
            user.setMonthSalary(new BigDecimal(updates.get("monthSalary").toString()));
        }
        if (updates.containsKey("dietTaboo")) {
            user.setDietTaboo((String) updates.get("dietTaboo"));
        }
        if (updates.containsKey("tastePrefer")) {
            user.setTastePrefer((String) updates.get("tastePrefer"));
        }
        return userRepository.save(user);
    }
}
