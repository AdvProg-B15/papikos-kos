package id.ac.ui.cs.advprog.papikos.kos.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TOPIC_EXCHANGE_NAME = "rental.topic";
    public static final String ROUTING_KEY_RENTAL_CREATED = "rental.created";

    // --- Common beans for all services ---
    @Bean
    public TopicExchange rentalTopicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // --- Beans specific to each consumer service will declare their Queues and Bindings ---
    // For example, in KosService:
    // public static final String KOS_QUEUE_NAME = "kos.rental.created.queue";
    // @Bean Queue kosQueue() { return new Queue(KOS_QUEUE_NAME, false); } // false = durable
    // @Bean Binding kosBinding(Queue kosQueue, TopicExchange exchange) {
    //    return BindingBuilder.bind(kosQueue).to(exchange).with(ROUTING_KEY_RENTAL_CREATED);
    // }
}
