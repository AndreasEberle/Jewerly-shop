package andreas.kafkis.eberle.jewelry.shop.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.RegisterRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Role;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.RoleRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role customerRole;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        customerRole = new Role();
        customerRole.setId(1);
        customerRole.setName("CUSTOMER");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPasswordHash("encodedPassword");
        testUser.setActive(true);
        testUser.setRoles(new java.util.HashSet<>(Set.of(customerRole)));

        registerRequest = RegisterRequest.builder()
                .email("newuser@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("password123")
                .build();
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CUSTOMER");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: nonexistent@example.com");
    }

    @Test
    void createUser_WhenValidRequest_ShouldCreateUser() {
        // Given
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");
        newUser.setActive(true);
        
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User createdUser = userService.createUser(newUser, "password123");

        // Then
        assertThat(createdUser).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail(any(User.class));
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowException() {
        // Given
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUser, "password123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User with email newuser@example.com already exists");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void createUser_WhenRoleNotFound_ShouldThrowException() {
        // Given
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUser, "password123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Default role CUSTOMER not found");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        User user = userService.findById(userId);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findById_WhenUserNotFound_ShouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: " + userId);
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User user = userService.findByEmail("test@example.com");

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findByEmail("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: nonexistent@example.com");
    }

    @Test
    void updateUser_WhenValidUser_ShouldUpdateUser() {
        // Given
        User userUpdates = new User();
        userUpdates.setFirstName("UpdatedName");
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User updatedUser = userService.updateUser(testUser.getId(), userUpdates);

        // Then
        assertThat(updatedUser).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    void setUserActive_WhenValidId_ShouldUpdateUser() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.setUserActive(userId, false);

        // Then
        verify(userRepository).save(testUser);
    }

    @Test
    void addRoleToUser_WhenValidRole_ShouldAddRole() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.addRoleToUser(userId, "ADMIN");

        // Then
        verify(userRepository).save(testUser);
    }

    @Test
    void hasRole_WhenUserHasRole_ShouldReturnTrue() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        boolean hasRole = userService.hasRole(userId, "CUSTOMER");

        // Then
        assertThat(hasRole).isTrue();
    }

    @Test
    void hasRole_WhenUserDoesNotHaveRole_ShouldReturnFalse() {
        // Given
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        boolean hasRole = userService.hasRole(userId, "ADMIN");

        // Then
        assertThat(hasRole).isFalse();
    }
}
