package com.example.orderservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/29 下午10:56
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
public class QueueConfig {

    @Bean("order")
    public Queue orderQueue() {
        return new Queue("queue.order", true,
                false, false, null);
    }

    @Bean
    public Queue restaurantQueue(){
        return new Queue("queue.restaurant");
    }
}
