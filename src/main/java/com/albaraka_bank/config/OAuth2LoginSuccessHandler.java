package com.albaraka_bank.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = determineTargetUrl(authorities);

        response.sendRedirect(redirectUrl);
    }

    private String determineTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_admin")) {
                return "/admin/users";
            } else if (role.equals("ROLE_AGENT_BANCAIRE") || role.equals("ROLE_agent")) {
                return "/agent/operations";
            } else if (role.equals("ROLE_CLIENT") || role.equals("ROLE_client")) {
                return "/client/operations";
            }
        }

            return "/dashboard";
    }
}
