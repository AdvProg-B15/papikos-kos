package id.ac.ui.cs.advprog.papikos.kos.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void restTemplateBeanIsConfigured() {
        this.contextRunner.withUserConfiguration(AppConfig.class).run(context -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context.getBean(RestTemplate.class)).isNotNull();
        });
    }
}

