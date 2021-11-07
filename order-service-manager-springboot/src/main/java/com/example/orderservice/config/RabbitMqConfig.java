package com.example.orderservice.config;

import com.example.orderservice.service.OrderMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.MethodInvoker;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/30 下午3:14
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
@Slf4j
public class RabbitMqConfig {
    @Autowired
    private OrderMessageService orderMessageService;

    @Autowired
    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    @Bean
    public ConnectionFactory connectionFactory() {
        // SpringBoot Amqp的
        CachingConnectionFactory connectionFactory
                = new CachingConnectionFactory();

        connectionFactory.setHost("192.168.114.12");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
//        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE);
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);
        connectionFactory.createConnection();
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(@Autowired ConnectionFactory connectionFactory) {
        // RabbitAdmin
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(@Autowired ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate
                = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(returnCallback);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        return rabbitTemplate;
    }

    RabbitTemplate.ReturnCallback returnCallback = (message, replyCode, replyText, exchange, routingKey) -> {
        log.info("Message Return:replyCode:{},replyText:{},exchange:{},routingKey:{},message:{}",
                replyCode, replyText, exchange, routingKey, message);
    };

    RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        log.info("confirm:correlationData:{},ack:{},cause:{}", correlationData, ack, cause);
    };

    @Bean
    public RabbitListenerContainerFactory rabbitListenerContainerFactory(@Autowired ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory =
                new SimpleRabbitListenerContainerFactory();

        simpleRabbitListenerContainerFactory.setConnectionFactory(connectionFactory);
        simpleRabbitListenerContainerFactory.setPrefetchCount(2);
        // 设置手动确认
        simpleRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return simpleRabbitListenerContainerFactory;
    }


//    @Bean
//    public SimpleMessageListenerContainer
//    messageListenerContainer(@Autowired ConnectionFactory connectionFactory) {
//        SimpleMessageListenerContainer simpleMessageListenerContainer
//                = new SimpleMessageListenerContainer();
//        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
//        // 需要监听的队列
//        simpleMessageListenerContainer.setQueueNames("queue.order");
//        // 同时有几个消费者线程消费队列
//        simpleMessageListenerContainer.setConcurrentConsumers(3);
//        simpleMessageListenerContainer.setMaxConcurrentConsumers(5);
//        // 自动确认
////        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
////        simpleMessageListenerContainer.setMessageListener(message -> {
////            log.info("message:{}", message);
////        });
//
//        // 手动确认
////        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
////        simpleMessageListenerContainer.setMessageListener(new ChannelAwareMessageListener() {
////            @Override
////            public void onMessage(Message message, Channel channel) throws Exception {
////                orderMessageService.handleMessage(message.getBody());
////                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
////            }
////        });
//        simpleMessageListenerContainer.setPrefetchCount(1);
//
//        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
//        // 设置Conveter
//        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter);
//        // 设置代理方法
//        messageListenerAdapter.setDelegate(orderMessageService);
//        Map<String, String> methodMap = new HashMap<>(8);
//        // 给队列指定执行的消费方法
//        methodMap.put("queue.order", "handleMessageNoByte");
//        messageListenerAdapter.setQueueOrTagToMethodName(methodMap);
//
//        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
//        return simpleMessageListenerContainer;
//    }
}
