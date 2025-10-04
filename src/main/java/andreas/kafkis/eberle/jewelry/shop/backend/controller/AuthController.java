package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.AuthenticationRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.AuthenticationResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.RegisterRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UserInfo;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.JwtService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.MetricsService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.OAuth2Service;
import andreas.kafkis.eberle.jewelry.shop.backend.service.TwoFactorAuthService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OAuth2Service oauth2Service;
    private final MetricsService metricsService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final SystemConfigService systemConfigService;
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * Register a new user
     */
    @Operation(
            summary = "Register a new user",
            description = "Create a new user account and return JWT tokens for immediate authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or email already exists",
                    content = @Content
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Registration attempt for email: {}", request.getEmail());
        log.info("Registration data: firstName={}, lastName={}, countryCode={}, phoneNumber={}", 
                request.getFirstName(), request.getLastName(), request.getCountryCode(), request.getPhoneNumber());
        try {
            // Check if user already exists
            User existingUser = userService.findByEmailOrNull(request.getEmail());
            if (existingUser != null) {
                log.warn("Registration attempt with existing email: {}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(AuthenticationResponse.builder()
                                .error("An account with this email already exists. Please use a different email or try logging in.")
                                .build());
            }
            
            // Create user entity
            User user = User.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneCountryCode(request.getCountryCode())
                    .phoneNumber(request.getPhoneNumber())
                    .dateOfBirth(parseDateOfBirth(request.getDateOfBirth()))
                    .gender(request.getGender())
                    .active(true)
                    .build();

            // Save user with encoded password
            User savedUser = userService.createUser(user, request.getPassword());

            // Load user details for JWT generation
            UserDetails userDetails = userService.loadUserByUsername(savedUser.getEmail());

            // Generate tokens
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Record metrics
            metricsService.recordUserRegistration();

            // Build response
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24 hours in seconds
                    .user(UserInfo.builder()
                            .id(savedUser.getId().toString())
                            .email(savedUser.getEmail())
                            .firstName(savedUser.getFirstName())
                            .lastName(savedUser.getLastName())
                            .roles(savedUser.getRoles().stream()
                                    .map(role -> role.getName())
                                    .collect(Collectors.toSet()))
                            .active(savedUser.isActive())
                            .build())
                    .build();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Registration failed for email: {}, error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(AuthenticationResponse.builder()
                            .error("Registration failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Authenticate user and return JWT tokens
     */
    @Operation(
            summary = "Authenticate user",
            description = "Login with email and password to receive JWT tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Parameter(description = "User login credentials", required = true)
            @Valid @RequestBody AuthenticationRequest request
    ) {
        log.info("Login attempt for email: {}", request.getEmail());
        log.info("Raw request body: {}", request);
        log.info("Request email: '{}'", request.getEmail());
        log.info("Request password: '{}'", request.getPassword());
        try {
            // Check if user exists and is OAuth-only before attempting authentication
            User user = userService.findByEmail(request.getEmail());
            if (user == null) {
                log.warn("User not found for email: {}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(AuthenticationResponse.builder()
                                .error("Invalid email or password. Please check your credentials and try again.")
                                .build());
            }
            
            if (user.isOauthOnly()) {
                log.warn("OAuth-only user attempted email/password login: {}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(AuthenticationResponse.builder()
                                .error("This account was created with Google. Please use Google to sign in.")
                                .build());
            }

            // Debug logging for password verification
            log.debug("Attempting login for user: {}", request.getEmail());
            log.debug("User found in DB: {}", user.getEmail());
            log.debug("User password hash in DB: {}", user.getPasswordHash());
            log.debug("Password from request: {}", request.getPassword());
            log.debug("User active status: {}", user.isActive());

            // Test password matching directly
            log.debug("Testing password: '{}'", request.getPassword());
            log.debug("Testing against hash: '{}'", user.getPasswordHash());
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
            log.debug("Direct password match test: {}", passwordMatches);
            
            // Test with known working hash
            String testHash = passwordEncoder.encode("password");
            log.debug("Generated test hash for 'password': {}", testHash);
            boolean testMatch = passwordEncoder.matches("password", testHash);
            log.debug("Test match with generated hash: {}", testMatch);
            
            // Load UserDetails to see what authentication manager will use
            UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
            log.debug("UserDetails password hash: {}", userDetails.getPassword());
            log.debug("UserDetails authorities: {}", userDetails.getAuthorities());
            
            // Authenticate user
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
                log.debug("Authentication successful for user: {}", request.getEmail());
            } catch (org.springframework.security.authentication.BadCredentialsException e) {
                log.warn("Authentication failed for email: {} - Invalid credentials", request.getEmail());
                log.warn("Exception details: {}", e.getMessage());
                return ResponseEntity.badRequest()
                        .body(AuthenticationResponse.builder()
                                .error("Invalid email or password. Please check your credentials and try again.")
                                .build());
            }

            // Load user details
            User authenticatedUser = userService.findByEmail(request.getEmail());

            // Generate tokens
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Record metrics
            metricsService.recordUserLogin();

            // Build response
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24 hours in seconds
                    .user(UserInfo.builder()
                            .id(user.getId().toString())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(user.getRoles().stream()
                                    .map(role -> role.getName())
                                    .collect(Collectors.toSet()))
                            .active(user.isActive())
                            .build())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login failed for email: {}, error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().build();
            }

            String refreshToken = authHeader.substring(7);
            String userEmail = jwtService.extractUsername(refreshToken);

            if (userEmail != null) {
                UserDetails userDetails = userService.loadUserByUsername(userEmail);
                User user = userService.findByEmail(userEmail);

                if (jwtService.isTokenValid(refreshToken, userDetails)) {
                    String newAccessToken = jwtService.generateToken(userDetails);

                    AuthenticationResponse response = AuthenticationResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(refreshToken)
                            .tokenType("Bearer")
                            .expiresIn(86400L)
                            .user(UserInfo.builder()
                                    .id(user.getId().toString())
                                    .email(user.getEmail())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .roles(user.getRoles().stream()
                                            .map(role -> role.getName())
                                            .collect(Collectors.toSet()))
                                    .active(user.isActive())
                                    .build())
                            .build();

                    return ResponseEntity.ok(response);
                }
            }

            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request
    ) {
        try {
            String token = null;
            
            // Try to get token from Authorization header first (for email/password login)
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                // Try to get token from HTTP-only cookie (for OAuth login)
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwt_token".equals(cookie.getName())) {
                            token = cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
            if (token == null) {
                return ResponseEntity.badRequest().build();
            }

            String userEmail = jwtService.extractUsername(token);

            if (userEmail != null) {
                User user = userService.findByEmail(userEmail);

                UserInfo userInfo = UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet()))
                        .active(user.isActive())
                        .build();

                return ResponseEntity.ok(userInfo);
            }

            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Logout user and clear cookies
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        try {
            // Clear HTTP-only cookies
            Cookie accessTokenCookie = new Cookie("jwt_token", null);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);
            response.addCookie(accessTokenCookie);
            
            Cookie refreshTokenCookie = new Cookie("jwt_refresh_token", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);
            
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Logout failed");
        }
    }

    /**
     * Test endpoint to verify password hash (DEBUG ONLY)
     */
    @PostMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
        
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("password", password);
        result.put("dbHash", user.getPasswordHash());
        result.put("matches", matches);
        result.put("userActive", user.isActive());
        result.put("oauthOnly", user.isOauthOnly());
        
        return ResponseEntity.ok(result);
    }

    /**
     * OAuth2 error endpoint
     */
    @GetMapping("/oauth2/error")
    public ResponseEntity<String> oauth2Error() {
        return ResponseEntity.badRequest().body("OAuth2 authentication failed");
    }

    /**
     * OAuth2 success endpoint - redirects to static page
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<String> oauth2Success(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String refreshToken,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String isAdmin) {
        
        // Build redirect URL with parameters
        StringBuilder redirectUrl = new StringBuilder("/static/oauth2-success.html");
        boolean hasParams = false;
        
        if (user != null) {
            redirectUrl.append(hasParams ? "&" : "?").append("user=").append(URLEncoder.encode(user, StandardCharsets.UTF_8));
            hasParams = true;
        }
        if (token != null) {
            redirectUrl.append(hasParams ? "&" : "?").append("token=").append(token);
            hasParams = true;
        }
        if (isAdmin != null) {
            redirectUrl.append(hasParams ? "&" : "?").append("isAdmin=").append(isAdmin);
            hasParams = true;
        }
        
        return ResponseEntity.status(302)
                .header("Location", redirectUrl.toString())
                .build();
    }

    /**
     * Get OAuth2 login URLs
     */
    @GetMapping("/oauth2/urls")
    public ResponseEntity<Object> getOAuth2LoginUrls() {
        return ResponseEntity.ok(java.util.Map.of(
                "google", "/oauth2/authorization/google"
        ));
    }

    /**
     * Swagger UI with token authentication - redirects to static page
     */
    @GetMapping("/swagger-ui-token")
    public void swaggerUiWithToken(@RequestParam(required = false) String token, 
                                   HttpServletResponse response) throws IOException {
        // Redirect to static HTML page
        String redirectUrl = "/static/swagger-ui-token.html";
        if (token != null) {
            redirectUrl += "?token=" + token;
        }
        
        response.sendRedirect(redirectUrl);
    }

    /**
     * Check if current user is admin (for session-based authentication)
     */
    @Operation(
            summary = "Check admin status",
            description = "Check if the current authenticated user has admin role"
    )
    @GetMapping("/check-admin")
    public ResponseEntity<Map<String, Object>> checkAdminStatus() {
        try {
            // Get current user from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is not authenticated or is anonymous
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false, 
                    "admin", false,
                    "message", "Not authenticated"
                ));
            }
            
            // Check if user has admin role
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "admin", isAdmin,
                "user", auth.getName(),
                "message", isAdmin ? "Admin user authenticated" : "Customer user authenticated"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "admin", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * JWT-based 2FA status check (for OAuth2 users)
     */
    @Operation(
            summary = "Check 2FA status (JWT-based)",
            description = "Check 2FA status using JWT token"
    )
    @GetMapping("/jwt/2fa/status")
    public ResponseEntity<Map<String, Object>> getJWT2FAStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract JWT token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "error", "No valid JWT token provided"
                ));
            }
            
            String token = authHeader.substring(7);
            
            // Validate JWT token by checking if it's expired
            String email;
            try {
                // Try to extract username to validate token structure
                email = jwtService.extractUsername(token);
                if (email == null || email.isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                        "authenticated", false,
                        "error", "Invalid JWT token - no username found"
                    ));
                }
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "error", "Invalid or expired JWT token: " + e.getMessage()
                ));
            }
            User user = userService.findByEmail(email);
            
            // Check if user has admin role
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ADMIN"));
            
            if (!isAdmin) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "admin", false,
                    "error", "Admin role required"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "admin", true,
                "user", email,
                "totpEnabled", user.isTotpEnabled(),
                "hasSecret", user.getTotpSecret() != null && !user.getTotpSecret().isEmpty()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Session-based 2FA status check (for OAuth2 users)
     */
    @Operation(
            summary = "Check 2FA status (session-based)",
            description = "Check 2FA status for the current authenticated user"
    )
    @GetMapping("/session/2fa/status")
    public ResponseEntity<Map<String, Object>> getSession2FAStatus() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "error", "Not authenticated"
                ));
            }
            
            // Check if user has admin role
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "admin", false,
                    "error", "Admin role required"
                ));
            }
            
            String email = auth.getName();
            User user = userService.findByEmail(email);
            
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "admin", true,
                "user", email,
                "totpEnabled", user.isTotpEnabled(),
                "hasSecret", user.getTotpSecret() != null && !user.getTotpSecret().isEmpty()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Simple session test endpoint
     */
    @Operation(
            summary = "Session test",
            description = "Test if OAuth2 session is working"
    )
    @GetMapping("/session/test")
    public ResponseEntity<Map<String, Object>> testSession() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            Map<String, Object> response = Map.of(
                "hasAuth", auth != null,
                "isAuthenticated", auth != null && auth.isAuthenticated(),
                "isAnonymous", auth instanceof AnonymousAuthenticationToken,
                "authType", auth != null ? auth.getClass().getSimpleName() : "null",
                "principal", auth != null ? auth.getPrincipal().getClass().getSimpleName() : "null",
                "authorities", auth != null ? auth.getAuthorities().size() : 0
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "hasAuth", false
            ));
        }
    }

    /**
     * Session-based health check (for OAuth2 users)
     */
    @Operation(
            summary = "Health check (session-based)",
            description = "Health check for the current authenticated user"
    )
    @GetMapping("/session/health")
    public ResponseEntity<Map<String, Object>> getSessionHealth() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "status", "Not authenticated"
                ));
            }
            
            // Check if user has admin role
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "admin", isAdmin,
                "user", auth.getName(),
                "status", "OK",
                "message", "Session-based health check successful",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "status", "ERROR",
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Verify 2FA code for login
     */
    @Operation(
            summary = "Verify 2FA code",
            description = "Verify TOTP code for users with 2FA enabled"
    )
    @PostMapping("/verify-2fa")
    public ResponseEntity<AuthenticationResponse> verifyTwoFactor(
            @RequestParam String email,
            @RequestParam String code,
            HttpServletResponse response) {
        
        User user = userService.findByEmail(email);
        
        if (!twoFactorAuthService.isTwoFactorEnabled(user.getTotpSecret(), user.isTotpEnabled())) {
            return ResponseEntity.badRequest().build();
        }

        if (!twoFactorAuthService.verifyCode(user.getTotpSecret(), code)) {
            return ResponseEntity.badRequest().build();
        }

        // Generate tokens after successful 2FA verification
        UserDetails userDetails = userService.loadUserByUsername(email);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Record metrics
        metricsService.recordUserLogin();

        // Set HTTP-only cookies for JWT tokens
        jakarta.servlet.http.Cookie accessTokenCookie = new jakarta.servlet.http.Cookie("jwt_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(86400); // 24 hours
        response.addCookie(accessTokenCookie);

        jakarta.servlet.http.Cookie refreshTokenCookie = new jakarta.servlet.http.Cookie("jwt_refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(604800); // 7 days
        response.addCookie(refreshTokenCookie);

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet()))
                        .active(user.isActive())
                        .build())
                .build();

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Public 2FA setup (for OAuth2 users before authentication)
     */
    @Operation(
            summary = "Setup 2FA (Public)",
            description = "Setup 2FA for OAuth2 users before authentication"
    )
    @PostMapping("/2fa/setup")
    public ResponseEntity<Map<String, Object>> setupPublic2FA(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email);
            
            if (user.isTotpEnabled()) {
                return ResponseEntity.ok(Map.of(
                    "error", "2FA is already enabled for this user"
                ));
            }

            String secret = twoFactorAuthService.generateSecret();
            String qrCodeDataUrl = twoFactorAuthService.generateQrCodeDataUrl(email, secret);
            String[] backupCodes = twoFactorAuthService.generateBackupCodes();

            return ResponseEntity.ok(Map.of(
                "secret", secret,
                "qrCode", qrCodeDataUrl,
                "backupCodes", backupCodes,
                "message", "Scan the QR code with your authenticator app"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", "Failed to setup 2FA: " + e.getMessage()
            ));
        }
    }

    /**
     * Handle GET requests to 2FA setup (redirect to proper page)
     */
    @GetMapping("/2fa/setup")
    public ResponseEntity<Map<String, Object>> setupPublic2FAGet(@RequestParam(required = false) String email) {
        return ResponseEntity.ok(Map.of(
            "error", "Use POST method for 2FA setup",
            "message", "Please use the 2FA setup form on the login page"
        ));
    }

    /**
     * Verify 2FA setup and enable it for the user
     */
    @PostMapping("/2fa/verify-setup")
    public ResponseEntity<Map<String, Object>> verify2FASetup(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String secret) {
        try {
            User user = userService.findByEmail(email);
            
            if (user.isTotpEnabled()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "2FA is already enabled for this user"
                ));
            }

            // Verify the code with the provided secret
            if (!twoFactorAuthService.verifyCode(secret, code)) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "Invalid verification code"
                ));
            }

            // Enable 2FA for the user
            user.setTotpSecret(secret);
            user.setTotpEnabled(true);
            userService.updateUser(user.getId(), user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "2FA has been successfully enabled for your account"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", "Failed to enable 2FA: " + e.getMessage()
            ));
        }
    }

    /**
     * JWT-based 2FA setup (for OAuth2 users)
     */
    @Operation(
            summary = "Setup 2FA (JWT-based)",
            description = "Setup 2FA using JWT token"
    )
    @PostMapping("/jwt/2fa/setup")
    public ResponseEntity<Map<String, Object>> setupJWT2FA(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract JWT token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "error", "No valid JWT token provided"
                ));
            }
            
            String token = authHeader.substring(7);
            
            // Validate JWT token
            String email;
            try {
                email = jwtService.extractUsername(token);
                if (email == null || email.isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                        "authenticated", false,
                        "error", "Invalid JWT token - no username found"
                    ));
                }
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "error", "Invalid or expired JWT token: " + e.getMessage()
                ));
            }
            
            User user = userService.findByEmail(email);
            
            // Check if user has admin role
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ADMIN"));
            
            if (!isAdmin) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "admin", false,
                    "error", "Admin role required"
                ));
            }
            
            // Generate 2FA secret
            String secret = twoFactorAuthService.generateSecret();
            user.setTotpSecret(secret);
            user.setTotpEnabled(false); // Not enabled until verified
            userRepository.save(user);
            
            // Generate QR code URL
            String qrCodeUrl = twoFactorAuthService.generateQrCodeDataUrl(user.getEmail(), secret);
            
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "admin", true,
                "user", email,
                "secret", secret,
                "qrCodeUrl", qrCodeUrl,
                "message", "2FA secret generated. Scan QR code with authenticator app."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * JWT-based 2FA verification (for OAuth2 users)
     */
    @Operation(
            summary = "Verify 2FA setup (JWT-based)",
            description = "Verify 2FA setup using JWT token"
    )
    @PostMapping("/jwt/2fa/verify")
    public ResponseEntity<Map<String, Object>> verifyJWT2FA(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String code) {
        try {
            // Extract JWT token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "error", "No valid JWT token provided"
                ));
            }
            
            String token = authHeader.substring(7);
            
            // Validate JWT token
            String email;
            try {
                email = jwtService.extractUsername(token);
                if (email == null || email.isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                        "authenticated", false,
                        "error", "Invalid JWT token - no username found"
                    ));
                }
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "error", "Invalid or expired JWT token: " + e.getMessage()
                ));
            }
            
            User user = userService.findByEmail(email);
            
            // Check if user has admin role
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ADMIN"));
            
            if (!isAdmin) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "admin", false,
                    "error", "Admin role required"
                ));
            }
            
            // Verify 2FA code
            if (twoFactorAuthService.verifyCode(user.getTotpSecret(), code)) {
                user.setTotpEnabled(true);
                userRepository.save(user);
                
                // Generate new JWT tokens after successful 2FA verification
                UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
                String accessToken = jwtService.generateToken(userDetails);
                String refreshToken = jwtService.generateRefreshToken(userDetails);
                
                // Set HTTP-only cookies
                jakarta.servlet.http.Cookie accessTokenCookie = new jakarta.servlet.http.Cookie("jwt_token", accessToken);
                accessTokenCookie.setHttpOnly(true);
                accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge(86400); // 24 hours
                
                jakarta.servlet.http.Cookie refreshTokenCookie = new jakarta.servlet.http.Cookie("jwt_refresh_token", refreshToken);
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge(604800); // 7 days
                
                // Note: We can't set cookies in ResponseEntity, so we'll return the tokens
                // The frontend should handle setting cookies or redirect to a page that sets them
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "admin", true,
                    "user", email,
                    "totpEnabled", true,
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "message", "2FA successfully verified! Tokens generated."
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "admin", true,
                    "user", email,
                    "totpEnabled", false,
                    "error", "Invalid 2FA code. Please try again."
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Debug endpoint to test cookie authentication
     */
    @Operation(
            summary = "Debug cookie authentication",
            description = "Test if cookies are working properly"
    )
    @GetMapping("/debug/cookies")
    public ResponseEntity<Map<String, Object>> debugCookies(HttpServletRequest request) {
        try {
            Map<String, Object> response = new java.util.HashMap<>();
            
            // Get all cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                response.put("cookieCount", cookies.length);
                Map<String, String> cookieMap = new java.util.HashMap<>();
                for (Cookie cookie : cookies) {
                    cookieMap.put(cookie.getName(), cookie.getValue());
                }
                response.put("cookies", cookieMap);
            } else {
                response.put("cookieCount", 0);
                response.put("cookies", new java.util.HashMap<>());
            }
            
            // Check authentication status
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            response.put("hasAuth", auth != null);
            response.put("isAuthenticated", auth != null && auth.isAuthenticated());
            response.put("isAnonymous", auth instanceof AnonymousAuthenticationToken);
            response.put("authType", auth != null ? auth.getClass().getSimpleName() : "null");
            response.put("principal", auth != null ? auth.getPrincipal().getClass().getSimpleName() : "null");
            response.put("authorities", auth != null ? auth.getAuthorities().size() : 0);
            
            // Check if user is admin
            boolean isAdmin = false;
            String userEmail = null;
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                // Get user email from principal
                if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                    userEmail = ((org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal()).getUsername();
                }
                
                // Check if user has ADMIN role
                isAdmin = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            }
            
            response.put("isAdmin", isAdmin);
            response.put("userEmail", userEmail);
            
            // Add detailed authorities
            if (auth != null && auth.getAuthorities() != null) {
                List<String> authorityNames = auth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(java.util.stream.Collectors.toList());
                response.put("authorityNames", authorityNames);
            } else {
                response.put("authorityNames", new java.util.ArrayList<>());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "hasAuth", false
            ));
        }
    }

    /**
     * Parse date of birth string to OffsetDateTime
     */
    private OffsetDateTime parseDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Parse the date string (expected format: yyyy-MM-dd from HTML date input)
            LocalDate localDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE);
            // Convert to OffsetDateTime at start of day in UTC
            return localDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date of birth: {}", dateOfBirth, e);
            return null;
        }
    }
}
