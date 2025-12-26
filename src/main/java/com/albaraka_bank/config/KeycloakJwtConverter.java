package com.albaraka_bank.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Map<String, String> ROLE_MAPPING = Map.of(
            "client", "ROLE_CLIENT",
            "agent", "ROLE_AGENT_BANCAIRE",
            "admin", "ROLE_ADMIN");

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            authorities.addAll(
                    roles.stream()
                            .map(role -> ROLE_MAPPING.getOrDefault(role.toLowerCase(), "ROLE_" + role.toUpperCase()))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()));
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().stream()
                    .filter(v -> v instanceof Map)
                    .map(v -> (Map<?, ?>) v)
                    .filter(m -> m.containsKey("roles"))
                    .flatMap(m -> ((List<?>) m.get("roles")).stream())
                    .filter(r -> r instanceof String)
                    .map(r -> (String) r)
                    .map(role -> ROLE_MAPPING.getOrDefault(role.toLowerCase(), "ROLE_" + role.toUpperCase()))
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
