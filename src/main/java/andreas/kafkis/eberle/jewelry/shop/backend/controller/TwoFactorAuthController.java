package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.TwoFactorAuthService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/2fa")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication", description = "2FA setup and management for admin users")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final UserService userService;

    @Operation(
            summary = "Setup 2FA for admin user",
            description = "Generate QR code and secret for 2FA setup"
    )
    @PostMapping("/setup")
    public ResponseEntity<Map<String, Object>> setupTwoFactor(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        		if (user == null) {
        		    throw new UsernameNotFoundException("User not found with email: " + email);
        		}
        
        if (user.isTotpEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "2FA is already enabled for this user"));
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
    }

    @Operation(
            summary = "Enable 2FA for admin user",
            description = "Verify the TOTP code and enable 2FA"
    )
    @PostMapping("/enable")
    public ResponseEntity<Map<String, Object>> enableTwoFactor(
            @RequestParam String secret,
            @RequestParam String code,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        		if (user == null) {
        		    throw new UsernameNotFoundException("User not found with email: " + email);
        		}

        if (user.isTotpEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "2FA is already enabled"));
        }

        if (!twoFactorAuthService.verifyCode(secret, code)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid verification code"));
        }

        // Enable 2FA for the user
        user.setTotpSecret(secret);
        user.setTotpEnabled(true);
        userService.updateUser(user.getId(), user);

        return ResponseEntity.ok(Map.of(
                "message", "2FA has been enabled successfully",
                "enabled", true
        ));
    }

    @Operation(
            summary = "Disable 2FA for admin user",
            description = "Disable 2FA for the current admin user"
    )
    @PostMapping("/disable")
    public ResponseEntity<Map<String, Object>> disableTwoFactor(
            @RequestParam String code,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        		if (user == null) {
        		    throw new UsernameNotFoundException("User not found with email: " + email);
        		}

        if (!user.isTotpEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "2FA is not enabled"));
        }

        if (!twoFactorAuthService.verifyCode(user.getTotpSecret(), code)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid verification code"));
        }

        // Disable 2FA
        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        userService.updateUser(user.getId(), user);

        return ResponseEntity.ok(Map.of(
                "message", "2FA has been disabled successfully",
                "enabled", false
        ));
    }

    @Operation(
            summary = "Get 2FA status",
            description = "Check if 2FA is enabled for the current user"
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTwoFactorStatus(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        		if (user == null) {
        		    throw new UsernameNotFoundException("User not found with email: " + email);
        		}
        
        boolean enabled = twoFactorAuthService.isTwoFactorEnabled(user.getTotpSecret(), user.isTotpEnabled());
        
        return ResponseEntity.ok(Map.of(
                "enabled", enabled,
                "email", email
        ));
    }
}
