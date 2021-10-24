package com.example.restruantservice.config;

import com.example.restruantservice.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/23 下午2:28
 * @description：
 * @modified By：
 * @version: $
 */
@Slf4j
@Configuration
public class RabbitMqConfig {
    @Autowired
    private OrderMessageService orderMessageService;

    @Autowired
    public void startListenMessage() {
        orderMessageService.handleMessage();
    }
}
