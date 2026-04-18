package com.nexora.core.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.core.auth.dto.RegisterUpdateRequest;
import com.nexora.core.profile.entity.AcademicInterests;
import com.nexora.core.profile.entity.Courses;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.ProfilesInterests;
import com.nexora.core.profile.repository.AcademicInterestsRepository;
import com.nexora.core.profile.repository.CoursesRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private ProfilesRepository profilesRepository;

    @MockBean
    private CoursesRepository coursesRepository;

    @MockBean
    private AcademicInterestsRepository academicInterestsRepository;

    @MockBean
    private ProfilesInterestsRepository profilesInterestsRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void catalogsShouldReturnCareersAndInterests() throws Exception {
        Courses course = new Courses();
        course.setName("Ingenieria de Sistemas");

        AcademicInterests interest = new AcademicInterests();
        interest.setName("IA");

        when(coursesRepository.findAllByOrderByNameAsc()).thenReturn(List.of(course));
        when(academicInterestsRepository.findAllByOrderByNameAsc()).thenReturn(List.of(interest));

        mockMvc.perform(get("/api/auth/catalogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.careers[0]").value("Ingenieria de Sistemas"))
                .andExpect(jsonPath("$.data.academicInterests[0]").value("IA"));
    }

    @Test
    void completeRegistrationShouldUpdateProfileAndInterests() throws Exception {
        Roles role = new Roles();
        role.setName(Role.ROLE_STUDENT.name());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("update@utp.edu.pe");
        user.setRole(role);
        user.setIsActive(true);

        Profiles profile = new Profiles();
        profile.setUser(user);
        profile.setUsername("initial_username");
        profile.setFullName("Nombre Inicial");
        profile.setBio("bio inicial");

        Courses course = new Courses();
        course.setName("Ingenieria de Software");

        AcademicInterests interest = new AcademicInterests();
        interest.setName("Cloud");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(profilesRepository.findByUser_Id(user.getId())).thenReturn(profile);
        when(coursesRepository.findByNameIgnoreCase("Ingenieria de Software")).thenReturn(Optional.of(course));
        when(academicInterestsRepository.findByName("Cloud")).thenReturn(Optional.of(interest));
        when(profilesRepository.save(any(Profiles.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profilesInterestsRepository.save(any(ProfilesInterests.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUpdateRequest updateRequest = new RegisterUpdateRequest();
        updateRequest.setUsername("updated_user");
        updateRequest.setFullName("Usuario Actualizado");
        updateRequest.setBio("Biografia actualizada");
        updateRequest.setCareer("Ingenieria de Software");
        updateRequest.setAcademicInterests(new String[]{"Cloud"});

        mockMvc.perform(put("/api/auth/register")
                .with(jwt().jwt(jwt -> jwt.subject(user.getId().toString()).claim("email", user.getEmail())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("updated_user"));
    }

    @Test
    void sessionShouldResolveCurrentUser() throws Exception {
        Roles role = new Roles();
        role.setName(Role.ROLE_STUDENT.name());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("session.user@utp.edu.pe");
        user.setRole(role);
        user.setIsActive(true);

        Profiles profile = new Profiles();
        profile.setUser(user);
        profile.setUsername("session.user");
        profile.setFullName("Session User");
        profile.setBio("Bio session");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profilesRepository.findByUser_Id(user.getId())).thenReturn(profile);
        when(profilesInterestsRepository.countByProfile(profile)).thenReturn(1L);

        mockMvc.perform(get("/api/auth/session")
                .with(jwt().jwt(jwt -> jwt.subject(user.getId().toString()).claim("email", user.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()));
    }
}
