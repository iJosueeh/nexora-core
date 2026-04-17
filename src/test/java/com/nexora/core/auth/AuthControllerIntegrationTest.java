package com.nexora.core.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.core.auth.dto.RegisterStartRequest;
import com.nexora.core.auth.dto.RegisterUpdateRequest;
import com.nexora.core.auth.dto.LoginRequest;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.repository.ProfilesRepository;
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
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private ProfilesRepository profilesRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebClient supabaseWebClient;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unchecked")
    private void setupWebClientMock(Object responseBody) {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(supabaseWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        
        if (responseBody instanceof Map) {
            when(responseSpec.bodyToMono(ArgumentMatchers.<Class<Map>>any()))
                .thenReturn(Mono.just((Map<String, Object>) responseBody));
        } else {
            when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());
        }
    }

    @Test
    void registerStartShouldSucceed() throws Exception {
        String email = "test.new@utp.edu.pe";
        String supabaseId = UUID.randomUUID().toString();
        setupWebClientMock(Map.of("id", supabaseId));

        Roles role = new Roles();
        role.setName(Role.ROLE_STUDENT.name());
        when(roleRepository.findByName(any())).thenReturn(Optional.of(role));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RegisterStartRequest request = new RegisterStartRequest();
        request.setEmail(email);
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void completeRegistrationShouldUpdateProfile() throws Exception {
        Roles role = new Roles();
        role.setName(Role.ROLE_STUDENT.name());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("update@utp.edu.pe");
        user.setRole(role);

        Profiles profile = new Profiles();
        profile.setUser(user);
        profile.setUsername("initial_username"); // Fix: Set initial username
        
        when(profilesRepository.findByUser_Id(any())).thenReturn(profile);
        when(profilesRepository.save(any())).thenReturn(profile);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        RegisterUpdateRequest updateRequest = new RegisterUpdateRequest();
        updateRequest.setUsername("updated_user");

        mockMvc.perform(put("/api/auth/register")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("updated_user"));
    }

    @Test
    void loginShouldSucceedWhenSupabaseOk() throws Exception {
        Roles role = new Roles();
        role.setName(Role.ROLE_STUDENT.name());

        User user = new User();
        user.setEmail("login@utp.edu.pe");
        user.setRole(role);

        setupWebClientMock(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getEmail());
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
