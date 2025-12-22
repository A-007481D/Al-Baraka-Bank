package com.albaraka_bank.modules.operation.repository;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.operation.model.Operation;
import com.albaraka_bank.modules.operation.model.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findByStatus(OperationStatus status);

    List<Operation> findByAccountSourceOrAccountDestination(Account source, Account destination);
}
