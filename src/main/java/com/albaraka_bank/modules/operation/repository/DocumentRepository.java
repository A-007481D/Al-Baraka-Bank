package com.albaraka_bank.modules.operation.repository;

import com.albaraka_bank.modules.operation.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByOperationId(Long operationId);
}
