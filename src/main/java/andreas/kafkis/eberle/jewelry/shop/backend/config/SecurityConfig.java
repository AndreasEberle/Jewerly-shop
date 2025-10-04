package andreas.kafkis.eberle.jewelry.shop.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final JwtCookieAuthenticationFilter jwtCookieAuthFilter;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - only what's truly needed
                       .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/oauth2/**", "/api/auth/verify-2fa", "/api/auth/2fa/setup", "/api/auth/2fa/verify-setup", "/api/auth/2fa/global-status", "/api/auth/jwt/**", "/api/auth/debug/**", "/api/auth/oauth2/error", "/api/auth/oauth2/success", "/api/auth/oauth2/urls").permitAll()
                .requestMatchers("/api/products", "/api/products/*", "/api/products/category/**").permitAll() // Only view products publicly
                .requestMatchers("/api/products/*/images", "/api/products/*/images/*").permitAll() // Product images
                .requestMatchers("/error").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll() // OAuth2 endpoints
                .requestMatchers("/oauth2/**").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/api/backup/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/health/**").hasAnyRole("ADMIN") // Health checks - authenticated users
                .requestMatchers("/swagger-ui/**").hasRole("ADMIN") // Swagger UI - admin only
                .requestMatchers("/swagger-ui.html").hasRole("ADMIN") // Swagger UI - admin only
                .requestMatchers("/swagger-ui-token").permitAll() // Token-based Swagger UI - public
                .requestMatchers("/static/**").permitAll() // Static HTML files - public
                .requestMatchers("/api-docs/**").hasRole("ADMIN") // OpenAPI docs - admin only
                .requestMatchers("/v3/api-docs/**").hasRole("ADMIN") // OpenAPI v3 docs - admin only
                .requestMatchers("/swagger-resources/**").hasRole("ADMIN") // Swagger resources - admin only
                .requestMatchers("/webjars/**").hasRole("ADMIN") // WebJars - admin only
                
                // Customer endpoints (authenticated users)
                .requestMatchers("/api/cart/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/profile/**").authenticated()
                .requestMatchers("/api/auth/check-admin").authenticated() // Check admin status - requires authentication
                
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2AuthenticationSuccessHandler)
                .failureUrl("/api/auth/oauth2/error")
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/static/access-denied.html")
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtCookieAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
