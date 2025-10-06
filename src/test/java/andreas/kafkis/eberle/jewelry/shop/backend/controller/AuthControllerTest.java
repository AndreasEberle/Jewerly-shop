package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.AuthenticationRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.RegisterRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Role;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.JwtService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.OAuth2Service;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private OAuth2Service oauth2Service;

    @MockBean
    private UserService userService;

    @MockBean
    private SystemConfigService systemConfigService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = new Role();
        
        customerRole.setName("CUSTOMER");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setActive(true);
        testUser.setRoles(Set.of(customerRole));
    }

    @Test
    void register_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("newuser@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("Password123")
                .build();

        when(userService.createUser(any(User.class))).thenReturn(Optional.of(testUser));
        when(userService.loadUserByUsername(anyString())).thenReturn(
                org.springframework.security.core.userdetails.User.builder()
                        .username(testUser.getEmail())
                        .password("encoded")
                        .authorities("ROLE_CUSTOMER")
                        .build()
        );
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("Password123")
                .build();

        // Mock the authentication manager
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                "test@example.com", 
                "Password123", 
                java.util.List.of(() -> "ROLE_CUSTOMER")
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        // Mock the user lookup
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);
        
        // Mock the UserDetails for JWT generation
        org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.builder()
                        .username(testUser.getEmail())
                        .password("encoded")
                        .authorities("ROLE_CUSTOMER")
                        .build();
        
        when(userService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        
        // Mock JWT generation
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"CUSTOMER"})
    void getCurrentUser_WhenValidToken_ShouldReturnUserInfo() throws Exception {
        // Given
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(userService.findByEmail(anyString())).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
}
