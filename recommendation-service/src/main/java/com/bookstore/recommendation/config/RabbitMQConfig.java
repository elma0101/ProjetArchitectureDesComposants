package com.bookstore.recommendation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${rabbitmq.exchange.loan}")
    private String loanExchange;
    
    @Value("${rabbitmq.queue.loan-events}")
    private String loanEventsQueue;
    
    @Value("${rabbitmq.routing-key.loan}")
    private String loanRoutingKey;
    
    @Bean
    public TopicExchange loanExchange() {
        return new TopicExchange(loanExchange);
    }
    
    @Bean
    public Queue loanEventsQueue() {
        return new Queue(loanEventsQueue, true);
    }
    
    @Bean
    public Binding loanEventsBinding() {
        return BindingBuilder
            .bind(loanEventsQueue())
            .to(loanExchange())
            .with(loanRoutingKey);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
