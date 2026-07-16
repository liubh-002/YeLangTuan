package com.practice.mealplanner.service;

import com.practice.mealplanner.dto.FamilyMemberRequest;
import com.practice.mealplanner.dto.FamilyMemberResponse;
import com.practice.mealplanner.model.FamilyGroup;
import com.practice.mealplanner.model.FamilyMember;
import com.practice.mealplanner.repository.FamilyGroupRepository;
import com.practice.mealplanner.repository.FamilyMemberRepository;
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

    public List<FamilyMemberResponse> getMembers(Long userId) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId)
                .orElseThrow(() -> new RuntimeException("家庭组不存在"));
        List<FamilyMember> members = memberRepository.findByFamilyId(group.getId());
        return members.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    public List<FamilyMemberResponse> getMembersByFamily(Long familyId) {
        List<FamilyMember> members = memberRepository.findByFamilyId(familyId);
        return members.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    public FamilyMemberResponse addMember(Long userId, FamilyMemberRequest request) {
        FamilyGroup group = groupRepository.findByMasterUserId(userId)
                .orElseThrow(() -> new RuntimeException("家庭组不存在"));
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(group.getId());
        member.setName(request.getName());
        member.setAge(request.getAge());
        member.setCrowd(request.getPersonTag());
        member.setDietTaboo(request.getDietTaboo() != null ? request.getDietTaboo() : "无");
        member.setFoodVolume(request.getAppetite() != null ? String.valueOf(request.getAppetite()) : "中");
        member = memberRepository.save(member);
        return buildResponse(member);
    }

    public FamilyMemberResponse updateMember(Long userId, Long memberId, FamilyMemberRequest request) {
        FamilyMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        member.setName(request.getName());
        member.setAge(request.getAge());
        member.setCrowd(request.getPersonTag());
        member.setDietTaboo(request.getDietTaboo() != null ? request.getDietTaboo() : "无");
        member.setFoodVolume(request.getAppetite() != null ? String.valueOf(request.getAppetite()) : "中");
        member = memberRepository.save(member);
        return buildResponse(member);
    }

    @Transactional
    public void deleteMember(Long userId, Long memberId) {
        memberRepository.deleteById(memberId);
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
        return response;
    }
}
