package com.albaraka_bank.modules.operation.service;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.service.AccountService;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.operation.dto.OperationRequest;
import com.albaraka_bank.modules.operation.dto.OperationResponse;
import com.albaraka_bank.modules.operation.factory.OperationFactory;
import com.albaraka_bank.modules.operation.model.Operation;
import com.albaraka_bank.modules.operation.model.OperationStatus;
import com.albaraka_bank.modules.operation.model.OperationType;
import com.albaraka_bank.modules.operation.repository.OperationRepository;
import com.albaraka_bank.modules.operation.repository.DocumentRepository;
import com.albaraka_bank.modules.operation.strategy.OperationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationService {

    private final OperationRepository operationRepository;
    private final DocumentRepository documentRepository;
    private final AccountService accountService;
    private final TransactionValidator transactionValidator;
    private final OperationFactory operationFactory;

    @Transactional
    public OperationResponse createOperation(OperationRequest request, User user) {
        Account sourceAccount = accountService.getAccountByOwner(user);
        Account destinationAccount = null;

        if (request.getType() == OperationType.TRANSFER) {
            if (request.getDestinationAccountNumber() == null) {
                throw new RuntimeException("Destination account required for transfers");
            }
            destinationAccount = accountService.getAccountByNumber(request.getDestinationAccountNumber());
        }

        OperationStatus status = transactionValidator.validate(request.getAmount());

        Operation operation = Operation.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .status(status)
                .accountSource(sourceAccount)
                .accountDestination(destinationAccount)
                .build();

        operationRepository.save(operation);

        if (status == OperationStatus.EXECUTED) {
            executeOperation(operation);
        }

        return mapToResponse(operation);
    }

    public List<OperationResponse> getOperationsByUser(User user) {
        Account account = accountService.getAccountByOwner(user);
        List<Operation> operations = operationRepository.findByAccountSourceOrAccountDestination(account, account);
        return operations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<OperationResponse> getPendingOperations() {
        List<Operation> operations = operationRepository.findByStatus(OperationStatus.PENDING);
        return operations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public OperationResponse approveOperation(Long operationId) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new RuntimeException("Only pending operations can be approved");
        }

        operation.setStatus(OperationStatus.EXECUTED);
        operation.setValidatedAt(LocalDateTime.now());

        executeOperation(operation);

        return mapToResponse(operation);
    }

    @Transactional
    public OperationResponse rejectOperation(Long operationId) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new RuntimeException("Only pending operations can be rejected");
        }

        operation.setStatus(OperationStatus.CANCELLED);
        operation.setValidatedAt(LocalDateTime.now());
        operationRepository.save(operation);

        return mapToResponse(operation);
    }

    private void executeOperation(Operation operation) {
        operation.setExecutedAt(LocalDateTime.now());

        OperationStrategy strategy = operationFactory.getStrategy(operation.getType());
        strategy.process(operation.getAmount(), operation.getAccountSource(), operation.getAccountDestination());

        operationRepository.save(operation);
    }

    private OperationResponse mapToResponse(Operation operation) {
        boolean hasDocument = documentRepository.findByOperationId(operation.getId()).isPresent();

        return OperationResponse.builder()
                .id(operation.getId())
                .type(operation.getType())
                .amount(operation.getAmount())
                .status(operation.getStatus())
                .createdAt(operation.getCreatedAt())
                .validatedAt(operation.getValidatedAt())
                .executedAt(operation.getExecutedAt())
                .sourceAccountNumber(operation.getAccountSource().getAccountNumber())
                .destinationAccountNumber(
                        operation.getAccountDestination() != null ? operation.getAccountDestination().getAccountNumber()
                                : null)
                .hasDocument(hasDocument)
                .build();
    }
}
