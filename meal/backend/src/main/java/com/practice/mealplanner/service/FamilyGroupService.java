package com.practice.mealplanner.service;

import com.practice.mealplanner.dto.FamilyGroupResponse;
import com.practice.mealplanner.dto.FamilyMemberResponse;
import com.practice.mealplanner.dto.JoinFamilyRequest;
import com.practice.mealplanner.model.FamilyGroup;
import com.practice.mealplanner.model.FamilyMember;
import com.practice.mealplanner.repository.FamilyGroupRepository;
import com.practice.mealplanner.repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        FamilyGroup group = findGroupByUserId(userId);
        return buildResponse(group);
    }

    public String getMode(Long userId) {
        // Check if user is a sub-account in a family
        FamilyMember subMember = memberRepository.findBySubAccountUserId(userId).orElse(null);
        if (subMember != null && "ACTIVE".equals(subMember.getStatus())) {
            return "FAMILY";
        }

        FamilyGroup group = groupRepository.findByMasterUserId(userId).orElse(null);
        if (group == null) return "PERSONAL";

        // Count active family members PLUS the main account themselves
        long memberCount = memberRepository.findByFamilyIdAndStatus(group.getId(), "ACTIVE").size();
        // If there are active members (sub-accounts) or the main account has added any member info, it is FAMILY mode
        if (memberCount > 0) return "FAMILY";

        // Also check non-sub-account family members (simple member info)
        long allMembers = memberRepository.findByFamilyId(group.getId()).size();
        return allMembers > 0 ? "FAMILY" : "PERSONAL";
    }

    @Transactional
    public FamilyGroupResponse joinFamily(Long userId, JoinFamilyRequest request) {
        FamilyGroup group = groupRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("邀请码无效"));

        String memberIds = group.getMemberUserIds();
        if (memberIds != null && memberIds.contains(String.valueOf(userId))) {
            throw new RuntimeException("已加入该家庭组");
        }

        // If the inviter is a sub-account, the join needs approval from the main account
        // Check: does this user have a pending invitation already?
        List<FamilyMember> pendingMembers = memberRepository.findByFamilyIdAndStatus(group.getId(), "PENDING_APPROVAL");
        boolean hasPending = pendingMembers.stream()
                .anyMatch(m -> userId.equals(m.getSubAccountUserId()));

        if (hasPending) {
            throw new RuntimeException("已提交申请，等待主账号审批");
        }

        // Use the invite-by-code flow which creates a pending record
        memberService.inviteByCode(userId, request.getInviteCode());

        return buildResponse(group);
    }

    @Transactional
    public void leaveFamily(Long userId) {
        // Check if user is the main account
        FamilyGroup group = groupRepository.findByMasterUserId(userId).orElse(null);
        if (group != null) {
            memberRepository.deleteByFamilyId(group.getId());
            groupRepository.delete(group);
            return;
        }

        // Check if user is a sub-account
        FamilyMember member = memberRepository.findBySubAccountUserId(userId).orElse(null);
        if (member != null) {
            // Remove from family group member list
            FamilyGroup fg = groupRepository.findById(member.getFamilyId()).orElse(null);
            if (fg != null && fg.getMemberUserIds() != null) {
                String ids = fg.getMemberUserIds();
                ids = ids.replace("," + userId, "")
                        .replace(userId + ",", "")
                        .replace(String.valueOf(userId), "");
                fg.setMemberUserIds(ids);
                groupRepository.save(fg);
            }
            memberRepository.delete(member);
            return;
        }

        throw new RuntimeException("未加入任何家庭组");
    }

    private String generateInviteCode() {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        while (groupRepository.findByInviteCode(code).isPresent()) {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        return code;
    }

    private FamilyGroup findGroupByUserId(Long userId) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId).orElse(null);
        if (group != null) return group;

        FamilyMember member = memberRepository.findBySubAccountUserId(userId).orElse(null);
        if (member != null && "ACTIVE".equals(member.getStatus())) {
            return groupRepository.findById(member.getFamilyId())
                    .orElseThrow(() -> new RuntimeException("家庭组不存在"));
        }

        throw new RuntimeException("家庭组不存在");
    }

    private FamilyGroupResponse buildResponse(FamilyGroup group) {
        FamilyGroupResponse response = new FamilyGroupResponse();
        response.setId(group.getId());
        response.setInviteCode(group.getInviteCode());
        response.setMembers(memberService.getMembersByFamily(group.getId()));
        // Only main account sees pending approvals
        List<FamilyMember> pending = memberRepository.findByFamilyIdAndStatus(group.getId(), "PENDING_APPROVAL");
        response.setPendingApprovals(pending.stream()
                .map(m -> {
                    FamilyMemberResponse mr = new FamilyMemberResponse();
                    mr.setId(m.getId());
                    mr.setName(m.getName());
                    mr.setPhone(m.getPhone());
                    mr.setStatus(m.getStatus());
                    mr.setIsSubAccount(m.getIsSubAccount());
                    mr.setSubAccountUserId(m.getSubAccountUserId());
                    return mr;
                })
                .collect(Collectors.toList()));
        return response;
    }
}
