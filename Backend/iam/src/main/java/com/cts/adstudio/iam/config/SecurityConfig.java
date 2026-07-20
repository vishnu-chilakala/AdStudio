package com.cts.adstudio.iam.config;

import com.cts.adstudio.iam.security.CustomUserDetailsService;
import com.cts.adstudio.iam.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Single, application-wide security configuration for the AdStudio monolith.
 *
 * <p>Consolidates the seven per-service security configs that existed when this
 * was a set of microservices. Design:</p>
 * <ul>
 *   <li>The IAM stateless-JWT machinery is kept intact: the {@link JwtAuthenticationFilter}
 *       runs on every request and, when a valid {@code Bearer} token is present, populates
 *       the {@link org.springframework.security.core.context.SecurityContextHolder}.</li>
 *   <li>URL-level access is locked down: every endpoint requires a valid JWT except a small
 *       public allowlist - the authentication endpoints ({@code /api/auth/**}), the OpenAPI
 *       / Swagger UI resources, the {@code health}/{@code info} actuator probes, and the two
 *       internal {@code GET} delivered-figure endpoints the finance module calls
 *       server-to-server (without a token) during invoice generation.</li>
 *   <li>A missing or invalid token yields {@code 401 Unauthorized}; an authenticated caller
 *       who lacks the required role yields {@code 403 Forbidden}.</li>
 *   <li>{@code @EnableMethodSecurity} layers per-endpoint RBAC on top via {@code @PreAuthorize}:
 *       the IAM audit-log endpoints (ADMIN), the finance billing endpoints (FINANCE_EXECUTIVE /
 *       DELIVERY_PUBLISHER / BRAND_ADVERTISER / ADMIN as appropriate), the delivery-record
 *       endpoints (DELIVERY_PUBLISHER / ADMIN) and the pacing-alert endpoints (MEDIA_PLANNER /
 *       ADMIN). Authenticate via {@code POST /api/auth/login}, then send the returned token as
 *       {@code Authorization: Bearer <token>}.</li>
 * </ul>
 *
 * <p>{@link PasswordEncoder}, {@link DaoAuthenticationProvider} and
 * {@link AuthenticationManager} are required by the IAM {@code AuthServiceImpl} and
 * {@code DataInitializer}, so they are declared here.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- Public endpoints (no token required) -----------------------------
                        // Authentication itself: you cannot present a token even before to obtain one.
                        .requestMatchers("/api/auth/**").permitAll()

                        // API documentation. these should be public to all
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Operational probes (only health + info are exposed; see application.properties).
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // Internal, server-to-server delivered-figure lookups. The finance module
                        // calls these over HTTP WITHOUT a token during invoice generation, so they
                        // must remain open. They return only aggregate numbers, never record detail.
                        .requestMatchers(HttpMethod.GET,
                                "/api/delivery/campaigns/*/delivered-spend",
                                "/api/delivery/insertion-orders/*/delivered-value").permitAll()
                        // --- Everything else now requires a valid JWT ------------------------
                        .anyRequest().authenticated())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Missing/invalid token -> 401 (instead of the servlet container default).
                // An authenticated caller who lacks the required role still gets 403 from the
                // default access-denied handler.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
