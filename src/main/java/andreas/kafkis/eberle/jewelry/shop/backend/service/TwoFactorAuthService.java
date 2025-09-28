package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.Base64;

import org.springframework.stereotype.Service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TwoFactorAuthService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    /**
     * Generate a new TOTP secret for a user
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * Generate QR code data URL for setting up 2FA
     */
    public String generateQrCodeDataUrl(String email, String secret) {
        try {
            QrData qrData = new QrData.Builder()
                    .label(email)
                    .secret(secret)
                    .issuer("Jewelry Shop")
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            byte[] qrCodeImage = qrGenerator.generate(qrData);
            String base64Image = Base64.getEncoder().encodeToString(qrCodeImage);
            return "data:image/png;base64," + base64Image;
        } catch (QrGenerationException e) {
            log.error("Error generating QR code for 2FA setup", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Verify a TOTP code
     */
    public boolean verifyCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            log.error("Error verifying TOTP code", e);
            return false;
        }
    }

    /**
     * Check if user has 2FA enabled
     */
    public boolean isTwoFactorEnabled(String totpSecret, boolean totpEnabled) {
        return totpEnabled && totpSecret != null && !totpSecret.isEmpty();
    }

    /**
     * Generate backup codes (for recovery)
     */
    public String[] generateBackupCodes() {
        String[] codes = new String[10];
        for (int i = 0; i < 10; i++) {
            codes[i] = generateRandomCode();
        }
        return codes;
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append((char) ('A' + Math.random() * 26));
        }
        return code.toString();
    }
}
