package com.albaraka_bank.modules.iam.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static java.util.Collections.singletonList;

public enum UserRole implements GrantedAuthority {
    CLIENT,
    AGENT_BANCAIRE,
    ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }

    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        return singletonList(new SimpleGrantedAuthority(getAuthority()));
    }
}

