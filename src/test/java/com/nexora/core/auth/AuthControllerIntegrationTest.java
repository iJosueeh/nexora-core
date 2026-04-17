package com.nexora.core.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.core.auth.dto.*;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.AcademicInterests;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.profile.repository.AcademicInterestsRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private ProfilesRepository profilesRepository;

    @MockBean
    private AcademicInterestsRepository academicInterestsRepository;

    @MockBean
    private ProfilesInterestsRepository profilesInterestsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebClient supabaseWebClient;

    private Roles studentRole;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        studentRole = new Roles();
        studentRole.setName(Role.ROLE_STUDENT.name());
        when(roleRepository.findByName(any())).thenReturn(Optional.of(studentRole));
    }

    @SuppressWarnings("unchecked")
    private void setupWebClientMock(Object... responses) {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(supabaseWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        
        if (responses.length > 0) {
            Mono[] monos = new Mono[responses.length];
            for (int i = 0; i < responses.length; i++) {
                monos[i] = Mono.just(responses[i]);
            }
            
            if (monos.length == 1) {
                when(responseSpec.bodyToMono(ArgumentMatchers.<Class<Map>>any())).thenReturn(monos[0]);
            } else {
                Mono first = monos[0];
                Mono[] rest = new Mono[monos.length - 1];
                System.arraycopy(monos, 1, rest, 0, monos.length - 1);
                when(responseSpec.bodyToMono(ArgumentMatchers.<Class<Map>>any())).thenReturn(first, rest);
            }
        }
    }

    @Test
    void registerStartShouldSucceedAndReturnTokens() throws Exception {
        String email = "test.new@utp.edu.pe";
        String supabaseId = UUID.randomUUID().toString();
        
        setupWebClientMock(
            Map.of("id", supabaseId), // 1. Admin user creation
            Map.of( // 2. Login call
                "access_token", "test_access_token",
                "refresh_token", "test_refresh_token",
                "expires_in", 3600
            )
        );

        User user = new User();
        user.setId(UUID.fromString(supabaseId));
        user.setEmail(email);
        user.setRole(studentRole);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty(), Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(profilesRepository.findByUser_Id(any())).thenReturn(null);

        RegisterStartRequest request = new RegisterStartRequest();
        request.setEmail(email);
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").value("test_access_token"))
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void registerIdentityShouldUpdateProfile() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("identity@utp.edu.pe");
        user.setRole(studentRole);

        Profiles profile = new Profiles();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        
        when(profilesRepository.findByUser_Id(any())).thenReturn(profile);
        when(profilesRepository.save(any())).thenReturn(profile);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        RegisterIdentityRequest identityRequest = new RegisterIdentityRequest();
        identityRequest.setUsername("new_username");
        identityRequest.setFullName("Full Name");
        identityRequest.setBio("My Bio");

        mockMvc.perform(put("/api/v1/auth/register/identity")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(identityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("new_username"));
    }

    @Test
    void registerPreferencesShouldUpdateProfile() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("prefs@utp.edu.pe");
        user.setRole(studentRole);

        Profiles profile = new Profiles();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        
        when(profilesRepository.findByUser_Id(any())).thenReturn(profile);
        when(academicInterestsRepository.findByName(any())).thenReturn(Optional.empty());
        when(academicInterestsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        RegisterPreferencesRequest prefsRequest = new RegisterPreferencesRequest();
        prefsRequest.setAcademicInterests(new String[]{"Java", "Testing"});

        mockMvc.perform(put("/api/v1/auth/register/preferences")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prefsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(user.getEmail()));
    }

    @Test
    void loginShouldSucceedWhenSupabaseOk() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("login@utp.edu.pe");
        user.setRole(studentRole);

        setupWebClientMock(Map.of(
            "access_token", "test_access_token",
            "refresh_token", "test_refresh_token",
            "expires_in", 3600
        ));
        
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getEmail());
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("test_access_token"))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()));
    }
}
