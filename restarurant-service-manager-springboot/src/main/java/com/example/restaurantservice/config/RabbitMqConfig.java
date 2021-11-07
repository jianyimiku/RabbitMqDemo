package com.example.restaurantservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/11/7 上午11:46
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
@Slf4j
public class RabbitMqConfig {
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory
                = new CachingConnectionFactory();
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setHost("192.168.114.12");
        cachingConnectionFactory.setPort(5672);
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        cachingConnectionFactory.setPublisherReturns(true);

        cachingConnectionFactory.createConnection();

        return cachingConnectionFactory;
    }


    @Bean
    public RabbitAdmin rabbitAdmin(@Autowired ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }


    @Bean
    public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate
                = new RabbitTemplate();
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("CorrelationDataId:{},ack:{},cause:{}", correlationData.getId(), ack, cause);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("Message:{},replyCode:{},replyText:{},exchange:{},routingKey:{}",
                        message, replyCode, replyText, exchange, routingKey);
            }
        });
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }


    @Bean
    public RabbitListenerContainerFactory rabbitListenerContainerFactory(@Autowired ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory
                = new SimpleRabbitListenerContainerFactory();
        simpleRabbitListenerContainerFactory.setConnectionFactory(connectionFactory);
        simpleRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleRabbitListenerContainerFactory.setPrefetchCount(2);
        simpleRabbitListenerContainerFactory.setConcurrentConsumers(2);
        simpleRabbitListenerContainerFactory.setMaxConcurrentConsumers(5);
        return simpleRabbitListenerContainerFactory;
    }
}
