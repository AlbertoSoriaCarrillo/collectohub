package com.collectohub;

import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseAutoConfigurationClasspathTest {

    @Test
    void springBootLiquibaseAutoConfigurationIsAvailable() {
        assertThat(ClassUtils.isPresent(
                "org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration",
                getClass().getClassLoader()))
                .isTrue();
    }
}
