package com.collectohub;

import com.collectohub.auth.infrastructure.RefreshTokenRepository;
import com.collectohub.shops.infrastructure.ShopMemberRepository;
import com.collectohub.shops.infrastructure.ShopRepository;
import com.collectohub.users.infrastructure.RoleRepository;
import com.collectohub.users.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration",
        "collectohub.security.jwt.secret=local-development-jwt-secret-change-before-production"
})
@ActiveProfiles("test")
@Import(CollectohubApplicationTests.RepositoryMocks.class)
class CollectohubApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class RepositoryMocks {

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        RoleRepository roleRepository() {
            return mock(RoleRepository.class);
        }

        @Bean
        RefreshTokenRepository refreshTokenRepository() {
            return mock(RefreshTokenRepository.class);
        }

        @Bean
        ShopRepository shopRepository() {
            return mock(ShopRepository.class);
        }

        @Bean
        ShopMemberRepository shopMemberRepository() {
            return mock(ShopMemberRepository.class);
        }
    }
}
