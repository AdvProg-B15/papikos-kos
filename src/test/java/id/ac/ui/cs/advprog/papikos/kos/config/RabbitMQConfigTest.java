package id.ac.ui.cs.advprog.papikos.kos.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(RabbitAutoConfiguration.class));

    @Test
    void rabbitMQBeansAreConfigured() {
        this.contextRunner.withUserConfiguration(RabbitMQConfig.class).run(context -> {
            assertThat(context).hasSingleBean(TopicExchange.class);
            assertThat(context.getBean(TopicExchange.class).getName()).isEqualTo(RabbitMQConfig.TOPIC_EXCHANGE_NAME);

            assertThat(context).hasSingleBean(MessageConverter.class);
            assertThat(context.getBean(MessageConverter.class)).isInstanceOf(org.springframework.amqp.support.converter.Jackson2JsonMessageConverter.class);

            assertThat(context).hasSingleBean(RabbitTemplate.class);

            assertThat(context).hasSingleBean(Binding.class);
            Binding kosBinding = context.getBean("kosBinding", Binding.class); // Bean name is method name by default
            assertThat(kosBinding.getExchange()).isEqualTo(RabbitMQConfig.TOPIC_EXCHANGE_NAME);
            assertThat(kosBinding.getRoutingKey()).isEqualTo(RabbitMQConfig.ROUTING_KEY_RENTAL_CREATED);
            assertThat(kosBinding.getDestination()).isEqualTo(RabbitMQConfig.KOS_QUEUE_NAME);
        });
    }
}

