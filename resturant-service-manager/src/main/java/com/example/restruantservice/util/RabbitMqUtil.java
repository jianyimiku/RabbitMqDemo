package com.example.restruantservice.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/17 下午10:04
 * @description：
 * @modified By：
 * @version: $
 */
@Component
public class RabbitMqUtil {
    public static ConnectionFactory connectionFactory;

    static {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.114.12");
        connectionFactory.setPort(5672);
    }

    public Connection getConnection() throws IOException, TimeoutException {
        return connectionFactory.newConnection();
    }
}
