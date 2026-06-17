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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@TestConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class TestSecurityConfiguration {

    @Bean
    UserDetailsService testUserDetailsService() {
        return username -> {
            if (username.startsWith("shop-owner")) {
                return AuthenticatedUser.from(testUser(username, "USER", "SHOP_OWNER"));
            }
            if (username.startsWith("admin")) {
                return AuthenticatedUser.from(testUser(username, "ADMIN"));
            }
            return AuthenticatedUser.from(testUser(username));
        };
    }

    public static User testUser(String email) {
        return testUser(email, "USER");
    }

    public static User testUser(String email, String... roleCodes) {
        Set<String> codes = new LinkedHashSet<>(Arrays.asList(roleCodes));
        if (codes.isEmpty()) {
            codes.add("USER");
        }
        String firstRole = codes.iterator().next();
        User user = User.register(email, "$2a$10$test-password-hash", "Test User", new Role(firstRole, firstRole));
        codes.stream()
                .skip(1)
                .map(code -> new Role(code, code))
                .forEach(user::addRole);
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }
}
