package com.practice.mealplanner.repository;

import com.practice.mealplanner.model.FamilyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, Long> {
    Optional<FamilyGroup> findByMasterUserId(Long masterUserId);
    Optional<FamilyGroup> findByInviteCode(String inviteCode);
}
