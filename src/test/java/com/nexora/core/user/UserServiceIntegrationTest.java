package com.nexora.core.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.nexora.core.user.dto.UserResponse;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.UserRepository;
import com.nexora.core.user.services.UserService;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void getUserByUuidShouldReturnUser() {
        User user = new User();
        user.setUsername("nexora_user");
        user.setEmail("uuid-user@example.com");
        user.setPassword("secret-hash");
        user.setIsActive(true);
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        UUID id = saved.getId();

        UserResponse response = userService.getUserById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(saved.getEmail(), response.getEmail());
    }
}
