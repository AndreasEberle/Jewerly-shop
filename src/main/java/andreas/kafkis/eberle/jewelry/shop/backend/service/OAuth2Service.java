package andreas.kafkis.eberle.jewelry.shop.backend.service;


import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

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
        User user = findOrCreateOAuth2User(email, firstName, lastName, googleId);
        
        // Log admin user detection
        Set<String> roleNames = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        boolean isAdmin = roleNames.contains("ADMIN");
        
        log.info("🔍 User roles loaded for {}: {}", email, roleNames);
        log.info("🔍 Total roles count: {}", user.getRoles().size());
        
        if (isAdmin) {
            log.info("🔐 Found existing OAuth2 ADMIN user for email: {} - Roles: {}", email, roleNames);
        } else {
            log.info("👤 Found existing OAuth2 CUSTOMER user for email: {} - Roles: {}", email, roleNames);
        }

        // Simple 2FA logic: Only require 2FA if BOTH global flag is true AND user has 2FA enabled in DB
        boolean requires2FA = false;
        if (global2FAEnabled) {
            boolean user2FAEnabled = user.isTotpEnabled() && user.getTotpSecret() != null && !user.getTotpSecret().isEmpty();
            requires2FA = isAdmin && user2FAEnabled;
            
            // Debug logging
            log.info("🔍 2FA Debug - global2FAEnabled: {}, isAdmin: {}, user2FAEnabled: {}, requires2FA: {}", 
                    global2FAEnabled, isAdmin, user2FAEnabled, requires2FA);
            log.info("🔍 User 2FA details - totpEnabled: {}, totpSecret: {}", user.isTotpEnabled(), 
                    user.getTotpSecret() != null ? "present" : "null");
        } else {
            log.info("🔍 2FA Debug - Global 2FA disabled, skipping 2FA check");
        }
        
        if (requires2FA) {
            // For 2FA-enabled users, don't generate tokens yet - they need to verify 2FA first
            log.info("🔐 2FA required for admin user: {}", email);
            return AuthenticationResponse.builder()
                    .accessToken(null) // No token until 2FA is verified
                    .refreshToken(null)
                    .tokenType("Bearer")
                    .expiresIn(0L)
                    .requires2FA(true) // Signal that 2FA is required
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
        }

        // Generate JWT tokens for users without 2FA or after 2FA verification
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Build response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .requires2FA(false)
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
    }

    /**
     * Find existing user or create new one from OAuth2 data
     */
    private User findOrCreateOAuth2User(String email, String firstName, String lastName, String googleId) {
        // Normalize email to lowercase for consistent searching
        String normalizedEmail = email.toLowerCase().trim();
        logger.debug("Looking for OAuth2 user with normalized email: '" + normalizedEmail + "'");
        
        // Search for existing user with normalized email
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (existingUser.isPresent()) {
            logger.debug("Found existing OAuth2 user for email: " + normalizedEmail);
            return existingUser.get();
        }
        
        // User doesn't exist, try to create new one
        logger.debug("Creating new OAuth2 user for normalized email: " + normalizedEmail);
        
        try {
            return createNewUserInFreshTransaction(normalizedEmail, firstName, lastName);
        } catch (Exception e) {
            // If there's any database error (like concurrent creation or constraint violation),
            // try to find the user again - it might have been created by another thread
            logger.warn("Failed to save new user, trying to find existing user: " + normalizedEmail, e);
            
            // Use a fresh query to find the user with normalized email
            logger.debug("Searching for user with normalized email: '" + normalizedEmail + "'");
            Optional<User> user = userRepository.findByEmailIgnoreCase(normalizedEmail);
            if (user.isPresent()) {
                logger.debug("Found user after save failure: " + normalizedEmail);
                return user.get();
            }
            
            // If we still can't find the user, log the error and rethrow
            logger.error("Failed to create or find OAuth2 user for email: " + normalizedEmail, e);
            throw new RuntimeException("Failed to create or find OAuth2 user", e);
        }
    }

    /**
     * Create a new user in a fresh transaction to avoid optimistic locking issues
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User createNewUserInFreshTransaction(String normalizedEmail, String firstName, String lastName) {
        logger.debug("Creating user in fresh transaction for email: " + normalizedEmail);
        
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Customer role not found"));

        User newUser = User.builder()
                .email(normalizedEmail)
                .firstName(firstName != null ? firstName : "Google")
                .lastName(lastName != null ? lastName : "User")
                .passwordHash("") // OAuth users don't have passwords
                .active(true)
                .roles(Set.of(customerRole))
                .build();

        logger.debug("Saving new user with email: " + normalizedEmail);
        User savedUser = userRepository.save(newUser);
        logger.debug("User saved successfully with ID: " + savedUser.getId());
        return savedUser;
    }

    /**
     * Check if user can login with OAuth2
     */
    public boolean canLoginWithOAuth2(String email) {
        return userRepository.findByEmail(email)
                .map(User::isActive)
                .orElse(true); // New users can always login
    }
}
