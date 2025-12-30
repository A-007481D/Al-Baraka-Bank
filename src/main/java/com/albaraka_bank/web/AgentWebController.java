package com.albaraka_bank.web;

import com.albaraka_bank.modules.operation.dto.OperationResponse;
import com.albaraka_bank.modules.operation.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentWebController {

    private final OperationService operationService;

    @GetMapping("/operations")
    public String pendingOperations(Model model) {
        List<OperationResponse> operations = operationService.getPendingOperations();
        model.addAttribute("operations", operations);
        return "agent/operations";
    }

    @PostMapping("/operations/{id}/approve")
    public String approveOperation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        operationService.approveOperation(id);
        redirectAttributes.addFlashAttribute("success", "Operation approved successfully");
        return "redirect:/agent/operations";
    }

    @PostMapping("/operations/{id}/reject")
    public String rejectOperation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        operationService.rejectOperation(id);
        redirectAttributes.addFlashAttribute("success", "Operation rejected");
        return "redirect:/agent/operations";
    }
}
