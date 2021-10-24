package com.example.delivery.service;

import com.example.delivery.constant.DeliveryManStatus;
import com.example.delivery.dao.DeliverymanMapper;
import com.example.delivery.dto.OrderMessageDto;
import com.example.delivery.pojo.Deliveryman;
import com.example.delivery.util.RabbitMqUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/23 下午2:51
 * @description：
 * @modified By：
 * @version: $
 */
@Service
public class OrderMessageService {
    @Autowired
    private RabbitMqUtil rabbitMqUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliverymanMapper deliverymanMapper;

    /**
     * 新建交换机和队列 绑定
     */
    @Async
    public void handleMessage() {
        try (
                Connection connection =
                        rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            // 什么交换机
            channel.exchangeDeclare("exchange.order.deliveryman",
                    BuiltinExchangeType.DIRECT, true
                    , false, null);
            channel.queueDeclare("queue.deliveryman", true, false,
                    false, null);
            channel.queueBind("queue.deliveryman", "exchange.order.deliveryman",
                    "key.deliveryman");

            channel.basicConsume("queue.deliveryman"
                    , true, deliverCallback, consumerTag -> {
                    });

            while (true) {
                Thread.sleep(1000000);
            }

        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回调函数
     */
    DeliverCallback deliverCallback = (consumerTag, message) -> {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            OrderMessageDto orderMessageDto = objectMapper
                    .readValue(messageBody, OrderMessageDto.class);

            // 查询出所有的骑手
            List<Deliveryman> deliverymanList
                    = deliverymanMapper.selectDeliveryManByStatus(DeliveryManStatus.AVAILABLE);
            orderMessageDto.setDeliverymanId(deliverymanList.get(0).getId());

            // 返回给订单微服务
            sendToOrder(orderMessageDto);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    };

    public void sendToOrder(OrderMessageDto orderMessageDto) {
        try (
                Connection connection =
                        rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            // 给Order返回消息
            channel.basicPublish("exchange.order.deliveryman"
                    , "key.order", null, messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
