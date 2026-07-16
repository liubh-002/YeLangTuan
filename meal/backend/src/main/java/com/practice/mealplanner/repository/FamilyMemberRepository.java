package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByFamilyId(Long familyId);
    List<FamilyMember> findByUserId(Long userId);
    List<FamilyMember> findByFamilyIdAndStatus(Long familyId, String status);
    Optional<FamilyMember> findBySubAccountUserId(Long subAccountUserId);
    void deleteByFamilyId(Long familyId);
}
