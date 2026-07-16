package com.practice.mealplanner.service;

import com.practice.mealplanner.dto.FamilyMemberRequest;
import com.practice.mealplanner.dto.FamilyMemberResponse;
import com.practice.mealplanner.dto.SubAccountRequest;
import com.practice.mealplanner.model.FamilyGroup;
import com.practice.mealplanner.model.FamilyMember;
import com.practice.mealplanner.model.User;
import com.practice.mealplanner.repository.FamilyGroupRepository;
import com.practice.mealplanner.repository.FamilyMemberRepository;
import com.practice.mealplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyMemberService {

    private final FamilyMemberRepository memberRepository;
    private final FamilyGroupRepository groupRepository;
    private final UserRepository userRepository;

    public List<FamilyMemberResponse> getMembers(Long userId) {
        FamilyGroup group = findGroupByUserId(userId);
        List<FamilyMember> members = memberRepository.findByFamilyIdAndStatus(group.getId(), "ACTIVE");
        return members.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    public List<FamilyMemberResponse> getPendingApprovals(Long userId) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId)
                .orElseThrow(() -> new RuntimeException("家庭组不存在"));
        List<FamilyMember> pending = memberRepository.findByFamilyIdAndStatus(group.getId(), "PENDING_APPROVAL");
        return pending.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    public List<FamilyMemberResponse> getMembersByFamily(Long familyId) {
        List<FamilyMember> members = memberRepository.findByFamilyId(familyId);
        return members.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    public FamilyMemberResponse addMember(Long userId, FamilyMemberRequest request) {
        FamilyGroup group = findGroupByUserId(userId);
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(group.getId());
        member.setName(request.getName());
        member.setAge(request.getAge());
        member.setCrowd(request.getPersonTag());
        member.setDietTaboo(request.getDietTaboo() != null ? request.getDietTaboo() : "无");
        member.setFoodVolume(request.getAppetite() != null ? String.valueOf(request.getAppetite()) : "3");
        member.setIsSubAccount(false);
        member.setStatus("ACTIVE");
        member = memberRepository.save(member);
        return buildResponse(member);
    }

    @Transactional
    public FamilyMemberResponse addSubAccount(Long masterUserId, SubAccountRequest request) {
        // Verify the master user owns a family group
        FamilyGroup group = groupRepository.findByMasterUserId(masterUserId)
                .orElseThrow(() -> new RuntimeException("请先创建家庭组"));

        // Check phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("该手机号已注册");
        }

        String subName = request.getName() != null && !request.getName().trim().isEmpty()
                ? request.getName() : "家庭成员-" + request.getPhone().substring(7);

        // Create a new User account for the sub-account
        User subUser = new User();
        subUser.setPhone(request.getPhone());
        subUser.setPassword(request.getPassword());
        subUser.setName(subName);
        subUser.setNickname(subName);
        subUser.setGender("未设置");
        subUser.setDietTaboo(request.getDietTaboo() != null ? request.getDietTaboo() : "无");
        subUser = userRepository.save(subUser);

        // Create FamilyMember record linking to the sub-account
        FamilyMember member = new FamilyMember();
        member.setUserId(masterUserId);
        member.setSubAccountUserId(subUser.getId());
        member.setPhone(request.getPhone());
        member.setFamilyId(group.getId());
        member.setName(subName);
        member.setAge(request.getAge());
        member.setCrowd(request.getPersonTag() != null ? request.getPersonTag() : "普通");
        member.setDietTaboo(request.getDietTaboo() != null ? request.getDietTaboo() : "无");
        member.setFoodVolume(request.getAppetite() != null ? String.valueOf(request.getAppetite()) : "3");
        member.setIsSubAccount(true);
        member.setStatus("ACTIVE");
        member = memberRepository.save(member);

        // Add sub-account userId to family group member list
        String memberIds = group.getMemberUserIds();
        group.setMemberUserIds((memberIds != null ? memberIds : "") + "," + subUser.getId());
        groupRepository.save(group);

        return buildResponse(member);
    }

    @Transactional
    public FamilyMemberResponse inviteByCode(Long userId, String inviteCode) {
        FamilyGroup group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("邀请码无效"));

        // Check if already a member
        String memberIds = group.getMemberUserIds();
        if (memberIds != null && memberIds.contains(String.valueOf(userId))) {
            throw new RuntimeException("已是该家庭成员");
        }

        // Get the current user info
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // Create a pending FamilyMember record
        FamilyMember member = new FamilyMember();
        member.setUserId(group.getMasterUserId());
        member.setSubAccountUserId(userId);
        member.setPhone(currentUser.getPhone());
        member.setFamilyId(group.getId());
        member.setName(currentUser.getName() != null ? currentUser.getName() : "待完善");
        member.setIsSubAccount(true);
        member.setStatus("PENDING_APPROVAL");
        member = memberRepository.save(member);

        return buildResponse(member);
    }

    @Transactional
    public FamilyMemberResponse approveMember(Long masterUserId, Long memberId) {
        // Verify it's the main account
        FamilyGroup group = groupRepository.findByMasterUserId(masterUserId)
                .orElseThrow(() -> new RuntimeException("只有主账号可以审批"));

        FamilyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));

        if (!member.getFamilyId().equals(group.getId())) {
            throw new RuntimeException("该成员不属于您的家庭");
        }

        member.setStatus("ACTIVE");
        member = memberRepository.save(member);

        // Add to family group member list
        if (member.getSubAccountUserId() != null) {
            String ids = group.getMemberUserIds();
            group.setMemberUserIds((ids != null ? ids : "") + "," + member.getSubAccountUserId());
            groupRepository.save(group);
        }

        return buildResponse(member);
    }

    @Transactional
    public void rejectMember(Long masterUserId, Long memberId) {
        FamilyGroup group = groupRepository.findByMasterUserId(masterUserId)
                .orElseThrow(() -> new RuntimeException("只有主账号可以审批"));

        FamilyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));

        if (!member.getFamilyId().equals(group.getId())) {
            throw new RuntimeException("该成员不属于您的家庭");
        }

        memberRepository.delete(member);
    }

    public FamilyMemberResponse updateMember(Long userId, Long memberId, FamilyMemberRequest request) {
        FamilyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        member.setName(request.getName());
        member.setAge(request.getAge());
        member.setCrowd(request.getPersonTag());
        member.setDietTaboo(request.getDietTaboo() != null ? request.getDietTaboo() : "无");
        member.setFoodVolume(request.getAppetite() != null ? String.valueOf(request.getAppetite()) : "3");
        member = memberRepository.save(member);
        return buildResponse(member);
    }

    @Transactional
    public void deleteMember(Long userId, Long memberId) {
        FamilyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));

        // If this is a sub-account, also remove from family group
        if (member.getIsSubAccount() != null && member.getIsSubAccount() && member.getSubAccountUserId() != null) {
            FamilyGroup group = groupRepository.findByMasterUserId(userId).orElse(null);
            if (group != null && group.getMemberUserIds() != null) {
                String ids = group.getMemberUserIds();
                ids = ids.replace("," + member.getSubAccountUserId(), "")
                        .replace(member.getSubAccountUserId() + ",", "")
                        .replace(String.valueOf(member.getSubAccountUserId()), "");
                group.setMemberUserIds(ids);
                groupRepository.save(group);
            }
        }
        memberRepository.deleteById(memberId);
    }

    public boolean isMainAccount(Long userId) {
        return groupRepository.findByMasterUserId(userId).isPresent();
    }

    /**
     * Find the group that a user belongs to (either as master or sub-account)
     */
    private FamilyGroup findGroupByUserId(Long userId) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId).orElse(null);
        if (group != null) return group;

        // Try to find as sub-account member
        FamilyMember member = memberRepository.findBySubAccountUserId(userId).orElse(null);
        if (member != null && "ACTIVE".equals(member.getStatus())) {
            return groupRepository.findById(member.getFamilyId())
                    .orElseThrow(() -> new RuntimeException("家庭组不存在"));
        }

        throw new RuntimeException("家庭组不存在");
    }

    private FamilyMemberResponse buildResponse(FamilyMember member) {
        FamilyMemberResponse response = new FamilyMemberResponse();
        response.setId(member.getId());
        response.setName(member.getName());
        response.setAge(member.getAge());
        response.setPersonTag(member.getCrowd());
        response.setAppetite(member.getFoodVolume() != null ? Integer.parseInt(member.getFoodVolume()) : 3);
        response.setDietTaboo(member.getDietTaboo());
        response.setDietaryType("普通饮食");
        response.setSubAccountUserId(member.getSubAccountUserId());
        response.setPhone(member.getPhone());
        response.setStatus(member.getStatus());
        response.setIsSubAccount(member.getIsSubAccount());
        return response;
    }
}
