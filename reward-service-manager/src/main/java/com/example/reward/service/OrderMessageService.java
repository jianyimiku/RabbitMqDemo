package com.example.reward.service;

import com.example.reward.constant.RewardStatus;
import com.example.reward.dao.RewardMapper;
import com.example.reward.dto.OrderMessageDto;
import com.example.reward.pojo.Reward;
import com.example.reward.util.RabbitMqUtil;
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
import java.time.LocalDateTime;
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
    private RewardMapper rewardMapper;
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
            channel.exchangeDeclare("exchange.order.reward",
                    BuiltinExchangeType.TOPIC, true
                    , false, null);
            channel.queueDeclare("queue.reward", true, false,
                    false, null);
            channel.queueBind("queue.reward", "exchange.order.reward",
                    "key.reward");

            channel.basicConsume("queue.reward"
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
            OrderMessageDto orderMessageDto
                    = objectMapper.readValue(messageBody, OrderMessageDto.class);

            //业务代码
            Reward reward = new Reward();
            reward.setOrderId(orderMessageDto.getOrderId());
            reward.setStatus(RewardStatus.SUCCESS);
            reward.setAmount(orderMessageDto.getPrice());

            rewardMapper.insert(reward);

            orderMessageDto.setRewardId(reward.getId());
            // 返回给Order
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
            channel.basicPublish("exchange.order.reward"
                    , "key.order", null, messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
