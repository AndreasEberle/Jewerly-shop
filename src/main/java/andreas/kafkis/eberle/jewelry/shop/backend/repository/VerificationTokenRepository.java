package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.VerificationToken;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByTokenAndTokenType(String token, VerificationToken.TokenType tokenType);
    
    Optional<VerificationToken> findByUserIdAndTokenTypeAndUsedFalse(UUID userId, VerificationToken.TokenType tokenType);
    
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.user.id = :userId AND vt.tokenType = :tokenType")
    void invalidateUserTokens(@Param("userId") UUID userId, @Param("tokenType") VerificationToken.TokenType tokenType);
}

