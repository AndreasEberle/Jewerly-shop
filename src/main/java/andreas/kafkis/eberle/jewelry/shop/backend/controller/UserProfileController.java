package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Address;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.AddressRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "User Profile", description = "User profile and address management endpoints")
public class UserProfileController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Helper method to get current user from authentication
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return user;
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneCountryCode() != null) {
            user.setPhoneCountryCode(request.getPhoneCountryCode());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(java.time.OffsetDateTime.parse(request.getDateOfBirth()));
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getNewsletterSubscribed() != null) {
            user.setNewsletterSubscribed(request.getNewsletterSubscribed());
        }
        if (request.getMarketingEmails() != null) {
            user.setMarketingEmails(request.getMarketingEmails());
        }
        if (request.getSmsNotifications() != null) {
            user.setSmsNotifications(request.getSmsNotifications());
        }
        
        User savedUser = userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId().toString());
        response.put("email", savedUser.getEmail());
        response.put("firstName", savedUser.getFirstName());
        response.put("lastName", savedUser.getLastName());
        response.put("phoneCountryCode", savedUser.getPhoneCountryCode());
        response.put("phoneNumber", savedUser.getPhoneNumber());
        response.put("dateOfBirth", savedUser.getDateOfBirth());
        response.put("gender", savedUser.getGender());
        response.put("preferredLanguage", savedUser.getPreferredLanguage());
        response.put("newsletterSubscribed", savedUser.isNewsletterSubscribed());
        response.put("marketingEmails", savedUser.isMarketingEmails());
        response.put("smsNotifications", savedUser.isSmsNotifications());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Change user password
     */
    @PutMapping("/change-password")
    @Operation(summary = "Change user password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Current password is incorrect");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "New password must be at least 8 characters long");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user addresses
     */
    @GetMapping("/addresses")
    @Operation(summary = "Get user addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(Authentication authentication) {
        User user = getCurrentUser(authentication);
        // Only return active addresses
        List<Address> addresses = addressRepository.findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtAsc(user.getId());
        
        List<AddressDTO> addressDTOs = addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(addressDTOs);
    }
    
    /**
     * Create address
     */
    @PostMapping("/addresses")
    @Operation(summary = "Create new address")
    public ResponseEntity<AddressDTO> createAddress(
            @RequestBody CreateAddressRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        // If this is set as default, unset other defaults (only for active addresses)
        if (request.getIsDefault() != null && request.getIsDefault()) {
            List<Address> existingAddresses = addressRepository.findByUserIdAndIsActiveTrue(user.getId());
            existingAddresses.forEach(addr -> addr.setDefault(false));
            addressRepository.saveAll(existingAddresses);
        }
        
        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .apartment(request.getApartment())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isActive(true)
                .build();
        
        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(convertToDTO(saved));
    }
    
    /**
     * Update address
     */
    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update address")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable UUID addressId,
            @RequestBody UpdateAddressRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Verify ownership
        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        // If this is set as default, unset other defaults (only for active addresses)
        if (request.getIsDefault() != null && request.getIsDefault()) {
            List<Address> existingAddresses = addressRepository.findByUserIdAndIsActiveTrue(user.getId());
            existingAddresses.forEach(addr -> {
                if (!addr.getId().equals(addressId)) {
                    addr.setDefault(false);
                }
            });
            addressRepository.saveAll(existingAddresses);
        }
        
        if (request.getStreet() != null) {
            address.setStreet(request.getStreet());
        }
        if (request.getApartment() != null) {
            address.setApartment(request.getApartment());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        if (request.getIsDefault() != null) {
            address.setDefault(request.getIsDefault());
        }
        
        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(convertToDTO(saved));
    }
    
    /**
     * Delete address (soft delete - sets isActive to false)
     */
    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete address (soft delete)")
    public ResponseEntity<Map<String, String>> deleteAddress(
            @PathVariable UUID addressId,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Verify ownership
        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        // Soft delete: set isActive to false instead of actually deleting
        // This preserves the address for orders that reference it
        address.setActive(false);
        addressRepository.save(address);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Address deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    private AddressDTO convertToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId().toString());
        dto.setStreet(address.getStreet());
        dto.setApartment(address.getApartment());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setIsDefault(address.isDefault());
        return dto;
    }
    
    // DTOs
    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }
    
    @Data
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phoneCountryCode;
        private String phoneNumber;
        private String dateOfBirth;
        private String gender;
        private String preferredLanguage;
        private Boolean newsletterSubscribed;
        private Boolean marketingEmails;
        private Boolean smsNotifications;
    }
    
    @Data
    public static class AddressDTO {
        private String id;
        private String street;
        private String apartment;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private Boolean isDefault;
    }
    
    @Data
    public static class CreateAddressRequest {
        private String street;
        private String apartment;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private Boolean isDefault;
    }
    
    @Data
    public static class UpdateAddressRequest {
        private String street;
        private String apartment;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private Boolean isDefault;
    }
}

