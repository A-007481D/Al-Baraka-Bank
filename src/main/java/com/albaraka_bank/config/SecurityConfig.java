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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtFilter jwtFilter;
        private final UserDetailsService userDetailsService;
        private final KeycloakJwtConverter keycloakJwtConverter;
        private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
        private final ClientRegistrationRepository clientRegistrationRepository;

        @Value("${keycloak.enabled:false}")
        private boolean keycloakEnabled;

        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
        private String keycloakIssuerUri;

        @Bean
        @Order(1)
        public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
                http.securityMatcher("/", "/login", "/register", "/logout", "/dashboard", "/agent/**",
                                "/admin/**", "/client/**", "/oauth2/**", "/login/oauth2/**",
                                "/webjars/**", "/css/**", "/js/**")
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/logout", "/register", "/agent/**",
                                                "/admin/**", "/client/**"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                                                .permitAll()
                                                .requestMatchers("/login", "/register").permitAll()
                                                .requestMatchers("/agent/**").hasRole("AGENT_BANCAIRE")
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/client/**").hasRole("CLIENT")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .successHandler(oauth2LoginSuccessHandler)
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userAuthoritiesMapper(userAuthoritiesMapper()))
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessHandler(oidcLogoutSuccessHandler())
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
                        return http.securityMatcher("/oauth2/**").csrf(AbstractHttpConfigurer::disable)
                                        .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
                                        .build();
                }

                http.securityMatcher("/api/keycloak/**")
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                http.securityMatcher("/api/**", "/auth/**")
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**")
                                                .permitAll()
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

        @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
        private String keycloakJwkSetUri;

        @Bean
        public JwtDecoder jwtDecoder() {
                if (keycloakEnabled && !keycloakIssuerUri.isEmpty() && !keycloakJwkSetUri.isEmpty()) {
                        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(keycloakJwkSetUri).build();
                        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(keycloakIssuerUri);
                        jwtDecoder.setJwtValidator(withIssuer);
                        return jwtDecoder;
                }
                return token -> {
                        throw new UnsupportedOperationException("Keycloak is not enabled");
                };
        }

        @Bean
        public LogoutSuccessHandler oidcLogoutSuccessHandler() {
                OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(
                                clientRegistrationRepository);
                successHandler.setPostLogoutRedirectUri("{baseUrl}/login?logout");
                return successHandler;
        }

        @Bean
        public GrantedAuthoritiesMapper userAuthoritiesMapper() {
                return (authorities) -> {
                        Set<GrantedAuthority> mappedAuthorities = new java.util.HashSet<>();

                        authorities.forEach(authority -> {
                                if (authority instanceof OidcUserAuthority) {
                                        OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                                        Map<String, Object> userInfo = oidcUserAuthority.getUserInfo().getClaims();

                                        if (userInfo.containsKey("realm_access")) {
                                                Map<String, Object> realmAccess = (Map<String, Object>) userInfo
                                                                .get("realm_access");
                                                List<String> roles = (List<String>) realmAccess.get("roles");
                                                roles.forEach(role -> {
                                                        String mappedRole = switch (role.toLowerCase()) {
                                                                case "client" -> "ROLE_CLIENT";
                                                                case "agent" -> "ROLE_AGENT_BANCAIRE";
                                                                case "admin" -> "ROLE_ADMIN";
                                                                default -> "ROLE_" + role.toUpperCase();
                                                        };
                                                        mappedAuthorities.add(new SimpleGrantedAuthority(mappedRole));
                                                });
                                        }
                                }
                                mappedAuthorities.add(authority);
                        });

                        return mappedAuthorities;
                };
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Allow all localhost ports for development
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:*",
                                "http://127.0.0.1:*"));
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
