package com.practice.mealplanner.controller;

import com.practice.mealplanner.dto.FamilyGroupResponse;
import com.practice.mealplanner.dto.FamilyMemberRequest;
import com.practice.mealplanner.dto.FamilyMemberResponse;
import com.practice.mealplanner.dto.JoinFamilyRequest;
import com.practice.mealplanner.dto.SubAccountRequest;
import com.practice.mealplanner.model.User;
import com.practice.mealplanner.service.AuthService;
import com.practice.mealplanner.service.FamilyGroupService;
import com.practice.mealplanner.service.FamilyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family")
public class FamilyController {

    private final FamilyGroupService groupService;
    private final FamilyMemberService memberService;
    private final AuthService authService;

    @GetMapping("/mode")
    public ResponseEntity<Map<String, String>> getMode(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        String mode = groupService.getMode(user.getId());
        Map<String, String> result = new HashMap<>();
        result.put("mode", mode);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/group")
    public ResponseEntity<FamilyGroupResponse> createFamily(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(groupService.createFamily(user.getId()));
    }

    @GetMapping("/group")
    public ResponseEntity<FamilyGroupResponse> getFamily(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(groupService.getFamily(user.getId()));
    }

    @PostMapping("/join")
    public ResponseEntity<FamilyGroupResponse> joinFamily(HttpServletRequest request,
                                                           @Valid @RequestBody JoinFamilyRequest joinRequest) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(groupService.joinFamily(user.getId(), joinRequest));
    }

    @DeleteMapping("/group")
    public ResponseEntity<Void> leaveFamily(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        groupService.leaveFamily(user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<FamilyMemberResponse>> getMembers(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(memberService.getMembers(user.getId()));
    }

    @PostMapping("/members")
    public ResponseEntity<?> addMember(HttpServletRequest request,
                                        @Valid @RequestBody FamilyMemberRequest memberRequest) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        // Sub-accounts cannot add family members
        if (!memberService.isMainAccount(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "子账户无权添加家庭成员"));
        }

        return ResponseEntity.ok(memberService.addMember(user.getId(), memberRequest));
    }

    @PutMapping("/members/{memberId}")
    public ResponseEntity<FamilyMemberResponse> updateMember(HttpServletRequest request,
                                                             @PathVariable Long memberId,
                                                             @Valid @RequestBody FamilyMemberRequest memberRequest) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(memberService.updateMember(user.getId(), memberId, memberRequest));
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Void> deleteMember(HttpServletRequest request, @PathVariable Long memberId) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        memberService.deleteMember(user.getId(), memberId);
        return ResponseEntity.ok().build();
    }

    // === Sub-account endpoints ===

    @PostMapping("/sub-account")
    public ResponseEntity<?> addSubAccount(HttpServletRequest request,
                                            @Valid @RequestBody SubAccountRequest subRequest) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        if (!memberService.isMainAccount(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "只有主账号可以添加子账户"));
        }

        return ResponseEntity.ok(memberService.addSubAccount(user.getId(), subRequest));
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<List<FamilyMemberResponse>> getPendingApprovals(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        if (!memberService.isMainAccount(user.getId())) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(memberService.getPendingApprovals(user.getId()));
    }

    @PostMapping("/approve/{memberId}")
    public ResponseEntity<?> approveMember(HttpServletRequest request, @PathVariable Long memberId) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        if (!memberService.isMainAccount(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "只有主账号可以审批"));
        }

        return ResponseEntity.ok(memberService.approveMember(user.getId(), memberId));
    }

    @PostMapping("/reject/{memberId}")
    public ResponseEntity<?> rejectMember(HttpServletRequest request, @PathVariable Long memberId) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);

        if (!memberService.isMainAccount(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "只有主账号可以审批"));
        }

        memberService.rejectMember(user.getId(), memberId);
        return ResponseEntity.ok(Map.of("message", "已拒绝"));
    }

    @GetMapping("/check-main-account")
    public ResponseEntity<Map<String, Object>> checkMainAccount(HttpServletRequest request) {
        String token = extractToken(request);
        User user = authService.getUserByToken(token);
        boolean isMain = memberService.isMainAccount(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("isMainAccount", isMain);
        result.put("userId", user.getId());
        return ResponseEntity.ok(result);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未提供认证令牌");
        }
        return authHeader.substring(7);
    }
}
