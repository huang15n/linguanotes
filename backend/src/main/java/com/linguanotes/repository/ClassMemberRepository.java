package com.linguanotes.repository;

import com.linguanotes.model.ClassMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClassMemberRepository extends JpaRepository<ClassMember, Long> {
    List<ClassMember> findAllBySchoolClassIdOrderByJoinedAtAsc(Long classId);
    List<ClassMember> findAllByUserIdOrderByJoinedAtDesc(Long userId);
    Optional<ClassMember> findBySchoolClassIdAndUserId(Long classId, Long userId);
    long countBySchoolClassId(Long classId);
}
