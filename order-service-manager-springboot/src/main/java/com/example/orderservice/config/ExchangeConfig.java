package com.example.orderservice.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/29 下午10:41
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
public class ExchangeConfig {
    @Bean(name = "restaurant")
    public Exchange restaurantExchange() {
        DirectExchange directExchange
                = new DirectExchange("exchange.order.restaurant",
                true, false);
        return directExchange;
    }

    @Bean(name = "deliveryman")
    public Exchange deliveryManExchange(){
        // 构造函数效果和上面一样
        return new DirectExchange("exchange.order.deliveryman");
    }

    @Bean(name = "order.exchange.settlement")
    public Exchange orderSettlementExchange(){
        return new FanoutExchange("exchange.settlement.order");
    }

    @Bean(name = "settlement.exchange.order")
    public Exchange settlementOrderExchange(){
        return new FanoutExchange("exchange.order.settlement");
    }

    @Bean(name = "reward")
    public Exchange rewardExchange(){
        return new TopicExchange("exchange.order.reward");
    }
}
