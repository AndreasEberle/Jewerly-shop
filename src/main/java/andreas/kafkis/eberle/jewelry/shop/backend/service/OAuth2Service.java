package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.AuthenticationResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UserInfo;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Role;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.RoleRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class OAuth2Service {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Value("${app.security.2fa.enabled:false}")
    private boolean global2FAEnabled;

    /**
     * Process OAuth2 login and return JWT tokens
     */
    public AuthenticationResponse processOAuth2Login(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String googleId = oauth2User.getAttribute("sub");

        // Find or create user
        Optional<User> userOpt = findOrCreateOAuth2User(email, firstName, lastName, googleId);
        if (userOpt.isEmpty()) {
            return AuthenticationResponse.builder()
                    .error("Failed to create or find user account. Please try again.")
                    .build();
        }
        User user = userOpt.get();
        
        // Log admin user detection
        Set<String> roleNames = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        log.info("OAuth2 login for user: {} with roles: {}", email, roleNames);
        
        // Check if user is active
        if (!user.isActive()) {
            log.warn("OAuth2 login attempt for inactive user: {}", email);
            return AuthenticationResponse.builder()
                    .error("Account is deactivated. Please contact support.")
                    .build();
        }

        // Check if 2FA is required
        boolean requires2FA = global2FAEnabled && user.isTotpEnabled() && user.getTotpSecret() != null;
        if (requires2FA) {
            log.info("2FA required for OAuth2 user: {}", email);
            return AuthenticationResponse.builder()
                    .requires2FA(true)
                    .user(UserInfo.builder()
                            .id(user.getId().toString())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                            .active(user.isActive())
                            .build())
                    .build();
        }

        // Generate JWT tokens
        UserDetails userDetails = userService.loadUserByUsername(email);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("OAuth2 login successful for user: {}", email);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .requires2FA(false)
                .user(UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .active(user.isActive())
                        .build())
                .build();
    }

    /**
     * Find existing user or create new OAuth2 user
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<User> findOrCreateOAuth2User(String email, String firstName, String lastName, String googleId) {
        try {
        	User existingUser = userRepository.findByEmail(email);
            
            if (existingUser!=null) {
                User user = existingUser;
                
                // Mark as OAuth-only if not already
                if (!user.isOauthOnly()) {
                    user.setOauthOnly(true);
                    user = userRepository.save(user);
                    log.info("Marked user {} as OAuth-only", email);
                }
                
                return Optional.of(user);
            }

            // Create new OAuth2 user
            log.info("Creating new OAuth2 user: {}", email);
            
            // Get default role
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default USER role not found"));

            User newUser = User.builder()
                    .email(email)
                    .firstName(firstName != null ? firstName : "Unknown")
                    .lastName(lastName != null ? lastName : "User")
                    .passwordHash("") // OAuth users don't have passwords
                    .active(true)
                    .oauthOnly(true)
                    .build();

            newUser.getRoles().add(userRole);
            User savedUser = userRepository.save(newUser);
            
            log.info("Created new OAuth2 user with ID: {}", savedUser.getId());
            return Optional.of(savedUser);
        } catch (Exception e) {
            log.error("Error finding or creating OAuth2 user for email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
