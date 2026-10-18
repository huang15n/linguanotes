package com.linguanotes.service;

import com.linguanotes.dto.ClassMemberResponse;
import com.linguanotes.dto.ClassRequest;
import com.linguanotes.dto.ClassResponse;
import com.linguanotes.model.ClassMember;
import com.linguanotes.model.ClassMemberRole;
import com.linguanotes.model.SchoolClass;
import com.linguanotes.repository.ClassMemberRepository;
import com.linguanotes.repository.SchoolClassRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ClassService {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SchoolClassRepository classes;
    private final ClassMemberRepository members;
    private final UserAccountRepository accounts;
    private final SecureRandom random = new SecureRandom();

    public ClassService(SchoolClassRepository classes, ClassMemberRepository members,
                        UserAccountRepository accounts) {
        this.classes = classes;
        this.members = members;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> list(Long userId) {
        Map<Long, SchoolClass> accessible = new LinkedHashMap<>();
        for (SchoolClass owned : classes.findAllByTeacherIdOrderByCreatedAtDesc(userId)) {
            accessible.put(owned.getId(), owned);
        }
        for (ClassMember membership : members.findAllByUserIdOrderByJoinedAtDesc(userId)) {
            accessible.putIfAbsent(membership.getSchoolClass().getId(), membership.getSchoolClass());
        }
        return accessible.values().stream().map(item -> map(item, userId)).toList();
    }

    public ClassResponse create(Long userId, ClassRequest request) {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setTeacher(accounts.getReferenceById(userId));
        schoolClass.setName(request.name().trim());
        schoolClass.setDescription(request.description());
        schoolClass.setJoinCode(generateCode());
        return map(classes.saveAndFlush(schoolClass), userId);
    }

    public ClassResponse join(Long userId, String joinCode) {
        SchoolClass schoolClass = classes.findByJoinCodeIgnoreCase(joinCode.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune classe ne correspond à ce code"));
        if (schoolClass.getTeacher().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous êtes déjà le propriétaire de cette classe");
        }
        members.findBySchoolClassIdAndUserId(schoolClass.getId(), userId).orElseGet(() -> {
            ClassMember member = new ClassMember();
            member.setSchoolClass(schoolClass);
            member.setUser(accounts.getReferenceById(userId));
            member.setRole(ClassMemberRole.STUDENT);
            return members.save(member);
        });
        return map(schoolClass, userId);
    }

    @Transactional(readOnly = true)
    public List<ClassMemberResponse> members(Long classId, Long userId) {
        requireAccess(classId, userId);
        return members.findAllBySchoolClassIdOrderByJoinedAtAsc(classId).stream()
                .map(member -> new ClassMemberResponse(member.getUser().getId(),
                        member.getUser().getName(), member.getUser().getEmail(),
                        member.getRole().name(), member.getJoinedAt()))
                .toList();
    }

    public void removeMember(Long classId, Long memberId, Long ownerId) {
        SchoolClass schoolClass = requireOwner(classId, ownerId);
        ClassMember member = members.findBySchoolClassIdAndUserId(schoolClass.getId(), memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Élève introuvable"));
        members.delete(member);
    }

    public void delete(Long classId, Long ownerId) {
        classes.delete(requireOwner(classId, ownerId));
    }

    private SchoolClass requireOwner(Long classId, Long userId) {
        SchoolClass schoolClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable"));
        if (!schoolClass.getTeacher().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        return schoolClass;
    }

    private SchoolClass requireAccess(Long classId, Long userId) {
        SchoolClass schoolClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable"));
        boolean allowed = schoolClass.getTeacher().getId().equals(userId)
                || members.findBySchoolClassIdAndUserId(classId, userId).isPresent();
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        return schoolClass;
    }

    private ClassResponse map(SchoolClass schoolClass, Long viewerId) {
        boolean owner = schoolClass.getTeacher().getId().equals(viewerId);
        return new ClassResponse(schoolClass.getId(), schoolClass.getName(),
                schoolClass.getDescription(), owner ? schoolClass.getJoinCode() : null,
                schoolClass.getTeacher().getId(), schoolClass.getTeacher().getName(),
                members.countBySchoolClassId(schoolClass.getId()), owner,
                schoolClass.getCreatedAt());
    }

    private String generateCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder code = new StringBuilder(6);
            for (int index = 0; index < 6; index++) {
                code.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            if (classes.findByJoinCodeIgnoreCase(code.toString()).isEmpty()) {
                return code.toString();
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Impossible de générer un code de classe");
    }
}
