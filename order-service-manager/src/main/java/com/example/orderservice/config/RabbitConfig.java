package com.example.orderservice.config;

import com.example.orderservice.service.OrderMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/20 下午10:37
 * @description：
 * @modified By：
 * @version: $
 */
@Configuration
public class RabbitConfig {
    @Autowired
    private OrderMessageService orderMessageService;

    @Autowired //表示这个类自动执行
    public void startListenMessage(){
        orderMessageService.handleMessage();
    }
}
