package com.sosd.config;


import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MessageQueueConfig {
    @Bean
    public DirectExchange retryExchange() {
        System.out.println("retryExchange registered");
        return new DirectExchange("retry.direct");
    }
    @Bean
    public Queue republishQueue() {
        return new Queue("republish.queue",true);
    }

    @Bean
    public Binding republishBinding(DirectExchange retryExchange, Queue republishQueue) {
        System.out.println("republish binding registered");
        return BindingBuilder.bind(republishQueue).to(retryExchange).with("publish.retry");
    }

    @Bean
    public Queue reupdateQueue() {
        return new Queue("reUpdate.queue",true);
    }

    @Bean
    public Binding reupdateBinding(DirectExchange retryExchange, Queue reupdateQueue) {
        System.out.println("reUpdate binding registered");
        return BindingBuilder.bind(reupdateQueue).to(retryExchange).with("reUpdate.retry");
    }

    @Bean
    public Queue redeleteQueue() {
        return new Queue("reDelete.queue",true);
    }

    @Bean
    public Binding redeleteBinding(DirectExchange retryExchange, Queue redeleteQueue) {
        System.out.println("reDelete binding registered");
        return BindingBuilder.bind(redeleteQueue).to(retryExchange).with("reDelete.retry");
    }


    @PostConstruct
    public void init() {
        System.out.println("MessageQueueConfig init");
    }


}
