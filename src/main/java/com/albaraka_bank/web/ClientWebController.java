package com.albaraka_bank.web;

import com.albaraka_bank.common.util.SecurityUtils;
import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.service.AccountService;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.operation.dto.OperationRequest;
import com.albaraka_bank.modules.operation.dto.OperationResponse;
import com.albaraka_bank.modules.operation.model.Document;
import com.albaraka_bank.modules.operation.model.OperationType;
import com.albaraka_bank.modules.operation.repository.DocumentRepository;
import com.albaraka_bank.modules.operation.service.DocumentService;
import com.albaraka_bank.modules.operation.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientWebController {

    private final OperationService operationService;
    private final DocumentService documentService;
    private final AccountService accountService;
    private final DocumentRepository documentRepository;

    @GetMapping("/operations")
    public String listOperations(Model model) {
        User user = SecurityUtils.getCurrentUser();
        List<OperationResponse> operations = operationService.getOperationsByUser(user);
        Account account = accountService.getAccountByOwner(user);

        model.addAttribute("operations", operations);
        model.addAttribute("account", account);
        model.addAttribute("operationTypes", OperationType.values());
        return "client/operations";
    }

    @GetMapping("/operations/new")
    public String newOperationForm(Model model) {
        User user = SecurityUtils.getCurrentUser();
        Account account = accountService.getAccountByOwner(user);

        model.addAttribute("account", account);
        model.addAttribute("operationTypes", OperationType.values());
        return "client/operation-form";
    }

    @PostMapping("/operations")
    public String createOperation(
            @RequestParam("type") OperationType type,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "destinationAccountNumber", required = false) String destinationAccountNumber,
            RedirectAttributes redirectAttributes) {
        try {
            User user = SecurityUtils.getCurrentUser();

            OperationRequest request = new OperationRequest();
            request.setType(type);
            request.setAmount(amount);
            request.setDestinationAccountNumber(destinationAccountNumber);

            OperationResponse response = operationService.createOperation(request, user);

            if (response.getStatus().name().equals("PENDING")) {
                redirectAttributes.addFlashAttribute("warning",
                        "Operation created with PENDING status. Please upload a justification document.");
                return "redirect:/client/operations/" + response.getId() + "/upload";
            }

            redirectAttributes.addFlashAttribute("success",
                    "Operation completed successfully! New balance will be reflected shortly.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/client/operations";
    }

    @GetMapping("/operations/{id}/upload")
    public String uploadForm(@PathVariable Long id, Model model) {
        model.addAttribute("operationId", id);
        return "client/upload-document";
    }

    @PostMapping("/operations/{id}/upload")
    public String uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            Document document = documentService.uploadDocument(id, file);
            redirectAttributes.addFlashAttribute("success",
                    "Document uploaded successfully: " + document.getFileName() +
                            ". AI is analyzing your document...");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/client/operations";
    }
}
