package com.nexora.core.user;

import com.nexora.core.user.dto.UserResponse;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;
import com.nexora.core.user.services.UserService;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.repository.ProfilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfilesRepository profilesRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        Roles studentRole = roleRepository.findByName(Role.ROLE_STUDENT.name())
                .orElseGet(() -> {
                    Roles r = new Roles();
                    r.setName(Role.ROLE_STUDENT.name());
                    return roleRepository.save(r);
                });

        testUser = new User();
        testUser.setEmail("service.test@utp.edu.pe");
        testUser.setRole(studentRole);
        testUser = userRepository.save(testUser);

        Profiles profile = new Profiles();
        profile.setUser(testUser);
        profile.setUsername("service_tester");
        profile.setFullName("Service Tester");
        profile = profilesRepository.save(profile);
        
        testUser.setProfile(profile);
        userRepository.save(testUser);
    }

    @Test
    void getUserByIdShouldReturnUserResponse() {
        UserResponse response = userService.getUserById(testUser.getId());

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals("service_tester", response.getUsername());
    }

    @Test
    void getUserByIdShouldThrowExceptionWhenNotFound() {
        UUID randomId = UUID.randomUUID();
        assertThrows(RuntimeException.class, () -> userService.getUserById(randomId));
    }
}
