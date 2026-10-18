package com.linguanotes.repository;

import com.linguanotes.model.AccountRole;
import com.linguanotes.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(AccountRole role);
}