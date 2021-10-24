package com.example.delivery.config;

import com.example.delivery.service.OrderMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/23 下午3:57
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
public class RabbitMqConfig {
    @Autowired
    private OrderMessageService orderMessageService;

    @Autowired
    private void handleMessage(){
        orderMessageService.handleMessage();
    }
}
