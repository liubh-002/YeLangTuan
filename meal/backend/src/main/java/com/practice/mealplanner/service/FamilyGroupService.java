package com.practice.mealplanner.service;

import com.practice.mealplanner.dto.FamilyGroupResponse;
import com.practice.mealplanner.dto.JoinFamilyRequest;
import com.practice.mealplanner.model.FamilyGroup;
import com.practice.mealplanner.repository.FamilyGroupRepository;
import com.practice.mealplanner.repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyGroupService {

    private final FamilyGroupRepository groupRepository;
    private final FamilyMemberService memberService;
    private final FamilyMemberRepository memberRepository;

    public FamilyGroupResponse createFamily(Long userId) {
        if (groupRepository.findByMasterUserId(userId).isPresent()) {
            throw new RuntimeException("用户已创建家庭组");
        }
        FamilyGroup group = new FamilyGroup();
        group.setMasterUserId(userId);
        group.setInviteCode(generateInviteCode());
        group.setMemberUserIds(String.valueOf(userId));
        group = groupRepository.save(group);
        return buildResponse(group);
    }

    public FamilyGroupResponse getFamily(Long userId) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId)
                .orElseThrow(() -> new RuntimeException("家庭组不存在"));
        return buildResponse(group);
    }

    public String getMode(Long userId) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId).orElse(null);
        if (group == null) return "PERSONAL";
        long memberCount = memberRepository.findByFamilyId(group.getId()).size();
        return memberCount > 0 ? "FAMILY" : "PERSONAL";
    }

    @Transactional
    public FamilyGroupResponse joinFamily(Long userId, JoinFamilyRequest request) {
        FamilyGroup group = groupRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("邀请码无效"));
        String memberIds = group.getMemberUserIds();
        if (memberIds != null && memberIds.contains(String.valueOf(userId))) {
            throw new RuntimeException("已加入该家庭组");
        }
        group.setMemberUserIds((memberIds != null ? memberIds : "") + "," + userId);
        group = groupRepository.save(group);
        return buildResponse(group);
    }

    @Transactional
    public void leaveFamily(Long userId) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId)
                .orElseThrow(() -> new RuntimeException("家庭组不存在"));
        memberRepository.deleteByFamilyId(group.getId());
        groupRepository.delete(group);
    }

    private String generateInviteCode() {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        while (groupRepository.findByInviteCode(code).isPresent()) {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        return code;
    }

    private FamilyGroupResponse buildResponse(FamilyGroup group) {
        FamilyGroupResponse response = new FamilyGroupResponse();
        response.setId(group.getId());
        response.setInviteCode(group.getInviteCode());
        response.setMembers(memberService.getMembersByFamily(group.getId()));
        return response;
    }
}
