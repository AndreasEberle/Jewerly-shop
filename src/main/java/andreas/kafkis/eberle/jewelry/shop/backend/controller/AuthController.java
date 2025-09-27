package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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
import andreas.kafkis.eberle.jewelry.shop.backend.service.JwtService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.OAuth2Service;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OAuth2Service oauth2Service;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            // Create user entity
            User user = User.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .active(true)
                    .build();

            // Save user with encoded password
            User savedUser = userService.createUser(user, request.getPassword());

            // Load user details for JWT generation
            UserDetails userDetails = userService.loadUserByUsername(savedUser.getEmail());

            // Generate tokens
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

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
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Load user details
            User user = userService.findByEmail(request.getEmail());
            UserDetails userDetails = userService.loadUserByUsername(request.getEmail());

            // Generate tokens
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

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
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().build();
            }

            String token = authHeader.substring(7);
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
     * OAuth2 error endpoint
     */
    @GetMapping("/oauth2/error")
    public ResponseEntity<String> oauth2Error() {
        return ResponseEntity.badRequest().body("OAuth2 authentication failed");
    }

    /**
     * OAuth2 success endpoint
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<String> oauth2Success(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String refreshToken,
            @RequestParam(required = false) String user) {
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>OAuth2 Success</title>
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                    .success { color: #28a745; font-size: 24px; margin-bottom: 20px; }
                    .info { color: #6c757d; font-size: 16px; margin-bottom: 10px; }
                    .token-info { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .token { font-family: monospace; font-size: 12px; word-break: break-all; }
                </style>
            </head>
            <body>
                <div class="success">✅ OAuth2 Authentication Successful!</div>
                <div class="info">You have successfully logged in with Google.</div>
                <div class="info">You can now close this window and return to your application.</div>
            """;
        
        if (token != null) {
            html += "<div class=\"token-info\">" +
                    "<div class=\"info\"><strong>Access Token:</strong></div>" +
                    "<div class=\"token\">" + token + "</div>" +
                    "</div>";
        }
        
        html += """
            </body>
            </html>
            """;
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(html);
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
}
