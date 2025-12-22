package com.albaraka_bank.modules.account.repository;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.iam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByOwner(User owner);
}
