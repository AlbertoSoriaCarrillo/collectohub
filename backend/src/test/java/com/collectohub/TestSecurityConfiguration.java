package com.collectohub;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.auth.security.JwtProperties;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

@TestConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class TestSecurityConfiguration {

    @Bean
    UserDetailsService testUserDetailsService() {
        return username -> AuthenticatedUser.from(testUser(username));
    }

    public static User testUser(String email) {
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role("USER", "User"));
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }
}
