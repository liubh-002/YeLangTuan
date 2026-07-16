package com.practice.mealplanner.service;

import com.practice.mealplanner.dto.UserRegisterRequest;
import com.practice.mealplanner.model.FamilyGroup;
import com.practice.mealplanner.model.FamilyMember;
import com.practice.mealplanner.model.User;
import com.practice.mealplanner.repository.FamilyGroupRepository;
import com.practice.mealplanner.repository.FamilyMemberRepository;
import com.practice.mealplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FamilyGroupRepository familyGroupRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final Map<String, String> verifyCodeStore = new HashMap<String, String>();

    public String generateVerifyCode(String phone) {
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));
        verifyCodeStore.put(phone, code);
        return code;
    }

    public boolean verifyCode(String phone, String code) {
        String storedCode = verifyCodeStore.get(phone);
        return storedCode != null && storedCode.equals(code);
    }

    public User register(UserRegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已注册");
        }
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setGender(request.getGender());
        user.setAge(request.getAge());
        user.setMonthSalary(request.getMonthSalary());
        user.setTastePrefer(request.getTastePrefer());
        user.setDietTaboo(request.getDietTaboo());
        if (request.getName() != null) {
            user.setNickname(request.getName());
        }
        return userRepository.save(user);
    }

    public User login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    /**
     * Sub-account login: validates invite code + phone + password
     */
    public User loginWithInviteCode(String inviteCode, String phone, String password) {
        // Validate invite code
        FamilyGroup group = familyGroupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("邀请码无效"));

        // Find user
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // Validate password
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // Check that this user is linked to this family group as a sub-account
        FamilyMember member = familyMemberRepository.findBySubAccountUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("该账号未关联到此家庭组"));

        if (!member.getFamilyId().equals(group.getId())) {
            throw new RuntimeException("该账号未关联到此家庭组");
        }

        if (!"ACTIVE".equals(member.getStatus())) {
            throw new RuntimeException("账号尚未激活，请联系主账号审批");
        }

        return user;
    }

    public User getUserByToken(String token) {
        String phone = token;
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User updatePassword(String phone, String newPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(newPassword);
        return userRepository.save(user);
    }
}
