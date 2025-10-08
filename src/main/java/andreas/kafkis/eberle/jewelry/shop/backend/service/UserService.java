package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Role;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.RoleRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    	User user = userRepository.findByEmail(email);
    	if (user == null) {
    	    throw new UsernameNotFoundException("User not found with email: " + email);
    	}


        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .authorities(getAuthorities(user.getRoles()))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<User> createUser(User user) {
        try {
            // Hash password
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            
            // Set default role if no roles assigned
            if (user.getRoles().isEmpty()) {
                Role userRole = roleRepository.findByName("USER")
                        .orElseThrow(() -> new RuntimeException("Default USER role not found"));
                user.getRoles().add(userRole);
            }
            
            User savedUser = userRepository.save(user);
            log.info("Created user: {}", savedUser.getEmail());
            return Optional.of(savedUser);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public User findByEmailOrNull(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> updateUser(UUID id, User userUpdates) {
        try {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            // Update fields if provided
            if (userUpdates.getFirstName() != null) {
                existingUser.setFirstName(userUpdates.getFirstName());
            }
            if (userUpdates.getLastName() != null) {
                existingUser.setLastName(userUpdates.getLastName());
            }
            if (userUpdates.getPhoneCountryCode() != null) {
                existingUser.setPhoneCountryCode(userUpdates.getPhoneCountryCode());
            }
            if (userUpdates.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(userUpdates.getPhoneNumber());
            }
            if (userUpdates.getDateOfBirth() != null) {
                existingUser.setDateOfBirth(userUpdates.getDateOfBirth());
            }
            if (userUpdates.getGender() != null) {
                existingUser.setGender(userUpdates.getGender());
            }

            // Update password if provided
            if (userUpdates.getPasswordHash() != null && !userUpdates.getPasswordHash().isEmpty()) {
                existingUser.setPasswordHash(passwordEncoder.encode(userUpdates.getPasswordHash()));
            }

            User savedUser = userRepository.save(existingUser);
            return Optional.of(savedUser);
        } catch (Exception e) {
            log.error("Error updating user with id {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void setUserActive(UUID userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setActive(active);
        userRepository.save(user);
        log.info("Set user {} active status to {}", userId, active);
    }

    @Transactional
    public void addRoleToUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        if (user.getRoles() == null) {
            user.setRoles(new java.util.HashSet<>());
        }
        
        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Added role {} to user {}", roleName, userId);
    }

    @Transactional
    public void removeRoleFromUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (user.getRoles() != null) {
            user.getRoles().removeIf(role -> role.getName().equals(roleName));
            userRepository.save(user);
            log.info("Removed role {} from user {}", roleName, userId);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (user.getRoles() == null) {
            return false;
        }
        
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}
