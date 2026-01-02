package com.albaraka_bank.modules.account.controller;

import com.albaraka_bank.common.util.SecurityUtils;
import com.albaraka_bank.modules.account.dto.AccountResponse;
import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.service.AccountService;
import com.albaraka_bank.modules.iam.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/account")
    public ResponseEntity<AccountResponse> getMyAccount() {
        User user = SecurityUtils.getCurrentUser();
        Account account = accountService.getAccountByOwner(user);

        AccountResponse response = AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .ownerName(user.getFullName())
                .build();

        return ResponseEntity.ok(response);
    }
}
