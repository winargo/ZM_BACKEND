package com.billermanagement.config;

import com.billermanagement.consumer.CallbackRabbitConsumer;
import com.billermanagement.util.InitDB;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "bmcallback_queue";
    public static final String EXCHANGE = "bmcallback_exchange";
    public static final String ROUTING_KEY = "bmcallback_routingKey";


    private final InitDB initDB = InitDB.getInstance();

    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer callbackListenerContainer(CallbackRabbitConsumer callbackRabbitConsumer, ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        String concurrency = initDB.get("Callback.Consumer.Concurrency");
        int prefetchCount = Integer.valueOf(initDB.get("Callback.Consumer.PrefetchCount"));
        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.setQueueNames(QUEUE);
        listenerContainer.setMessageListener(callbackRabbitConsumer);
        listenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        listenerContainer.setConcurrency(concurrency);
        listenerContainer.setPrefetchCount(prefetchCount);
        return listenerContainer;
    }
}
