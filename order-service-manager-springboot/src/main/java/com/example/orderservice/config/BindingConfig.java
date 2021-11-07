package com.example.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/29 下午10:59
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
public class BindingConfig {

    @Bean(value = "order.restaurant")
    public Binding restaurantOrderBinding() {
        return new Binding("queue.order", Binding.DestinationType.QUEUE,
                "exchange.order.restaurant", "key.order", null);
    }

    @Bean
    public Binding orderRestaurant() {
        return new Binding("queue.restaurant", Binding.DestinationType.QUEUE, "exchange.order.restaurant",
                "key.restaurant", null);
    }

    @Bean(value = "order.deliveryman")
    public Binding deliveryOrderBinding() {
        return new Binding("queue.order", Binding.DestinationType.QUEUE,
                "exchange.order.deliveryman", "key.order", null);
    }

    @Bean(value = "order.settlement")
    public Binding settlementToOrder() {
        return new Binding("queue.order", Binding.DestinationType.QUEUE,
                "exchange.settlement.order", "", null);
    }

    @Bean(value = "order.reward")
    public Binding orderRewardBinding() {
        return new Binding("queue.order", Binding.DestinationType.QUEUE,
                "exchange.order.reward", "key.order", null);
    }
}
