package id.ac.ui.cs.advprog.papikos.kos.config;

import id.ac.ui.cs.advprog.papikos.kos.security.TokenAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    // Mock TokenAuthenticationFilter as it might have dependencies (like RestTemplate)
    @Configuration
    static class MockTokenFilterConfiguration {
        @Bean
        public TokenAuthenticationFilter tokenAuthenticationFilter() {
            // Mock RestTemplate if TokenAuthenticationFilter requires it in constructor
            return new TokenAuthenticationFilter(mock(RestTemplate.class), mock(com.fasterxml.jackson.databind.ObjectMapper.class));
        }
    }

    @Test
    void securityFilterChainBeanIsConfigured() {
        this.contextRunner
                .withUserConfiguration(SecurityConfig.class, MockTokenFilterConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityFilterChain.class);
                    assertThat(context.getBean(SecurityFilterChain.class)).isNotNull();
                });
    }
}

