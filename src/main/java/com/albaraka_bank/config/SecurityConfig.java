package com.albaraka_bank.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;
    private final KeycloakJwtConverter keycloakJwtConverter;

    @Value("${keycloak.enabled:false}")
    private boolean keycloakEnabled;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String keycloakIssuerUri;

    @Bean
    @Order(1)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/", "/login", "/logout", "/dashboard", "/agent/**", "/admin/**", "/client/**",
                        "/webjars/**", "/css/**", "/js/**")
                .csrf(csrf -> csrf.ignoringRequestMatchers("/logout"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest
                                .toStaticResources().atCommonLocations())
                        .permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/agent/**").hasRole("AGENT_BANCAIRE")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/client/**").hasRole("CLIENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .rememberMe(remember -> remember
                        .key("albaraka-remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60))
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain keycloakSecurityFilterChain(HttpSecurity http) throws Exception {
        if (!keycloakEnabled) {
            return http
                    .securityMatcher("/oauth2/**")
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
                    .build();
        }

        http
                .securityMatcher("/api/keycloak/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/keycloak/client/**").hasRole("CLIENT")
                        .requestMatchers("/api/keycloak/agent/**").hasRole("AGENT_BANCAIRE")
                        .requestMatchers("/api/keycloak/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter)));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/auth/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/client/**").hasRole("CLIENT")
                        .requestMatchers("/api/agent/**").hasRole("AGENT_BANCAIRE")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (keycloakEnabled && !keycloakIssuerUri.isEmpty()) {
            return JwtDecoders.fromIssuerLocation(keycloakIssuerUri);
        }
        return token -> {
            throw new UnsupportedOperationException("Keycloak is not enabled");
        };
    }
}
