package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.VerificationToken;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.VerificationTokenRepository;

@Service
public class VerificationTokenService {
    
    @Autowired
    private VerificationTokenRepository tokenRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * Generate and save a verification token
     */
    @Transactional
    public VerificationToken generateToken(User user, VerificationToken.TokenType tokenType) {
        // Invalidate existing tokens of the same type for this user
        tokenRepository.invalidateUserTokens(user.getId(), tokenType);
        
        // Generate new token
        String token = UUID.randomUUID().toString();
        
        // Get expiration time from config
        LocalDateTime expiresAt;
        if (tokenType == VerificationToken.TokenType.PASSWORD_RESET) {
            int expirationMinutes = Integer.parseInt(systemConfigService.getConfigValue("email.password_reset.token_expiration_minutes", "60"));
            expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        } else {
            int expirationHours = Integer.parseInt(systemConfigService.getConfigValue("email.account_confirmation.token_expiration_hours", "24"));
            expiresAt = LocalDateTime.now().plusHours(expirationHours);
        }
        
        VerificationToken verificationToken = VerificationToken.builder()
            .token(token)
            .user(user)
            .tokenType(tokenType)
            .expiresAt(expiresAt)
            .used(false)
            .build();
        
        return tokenRepository.save(verificationToken);
    }
    
    /**
     * Validate and retrieve a token
     */
    @Transactional(readOnly = true)
    public Optional<VerificationToken> validateToken(String token, VerificationToken.TokenType expectedType) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByTokenAndTokenType(token, expectedType);
        
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }
        
        VerificationToken verificationToken = tokenOpt.get();
        
        // Check if token is expired
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        
        // Check if token is already used
        if (verificationToken.isUsed()) {
            return Optional.empty();
        }
        
        return Optional.of(verificationToken);
    }
    
    /**
     * Mark token as used
     */
    @Transactional
    public void markTokenAsUsed(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenEntity -> {
            tokenEntity.setUsed(true);
            tokenRepository.save(tokenEntity);
        });
    }
}

