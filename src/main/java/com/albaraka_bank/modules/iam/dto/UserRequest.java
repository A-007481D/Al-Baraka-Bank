package com.albaraka_bank.modules.iam.dto;

import com.albaraka_bank.modules.iam.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    private boolean active = true;
}
