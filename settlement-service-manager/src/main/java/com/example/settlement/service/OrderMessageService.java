package com.example.settlement.service;

import com.example.settlement.constant.SettlementStatus;
import com.example.settlement.dao.SettlementMapper;
import com.example.settlement.dto.OrderMessageDto;
import com.example.settlement.pojo.Settlement;
import com.example.settlement.util.RabbitMqUtil;
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
    private SettlementService settlementService;

    @Autowired
    private SettlementMapper settlementMapper;

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
            channel.exchangeDeclare("exchange.order.settlement",
                    BuiltinExchangeType.FANOUT, true
                    , false, null);
            channel.queueDeclare("queue.settlement", true, false,
                    false, null);
            channel.queueBind("queue.settlement", "exchange.order.settlement",
                    "key.settlement");

            channel.basicConsume("queue.settlement"
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

            Settlement settlement = new Settlement();
            settlement.setAmount(orderMessageDto.getPrice());
            settlement.setDate(LocalDateTime.now());
            settlement.setOrderId(orderMessageDto.getOrderId());
            // 调用结算接口
            Integer id = settlementService.settlement(orderMessageDto.getAccountId(), orderMessageDto.getPrice());
            settlement.setTransactionId(id);
            settlement.setStatus(SettlementStatus.SUCCESS);
            settlementMapper.insert(settlement);

            orderMessageDto.setSettlementId(settlement.getId());

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
            // 给Order返回消息 Fanout绑定Exchange的队列都能收到 所以这边如果Order和Settlement都绑定Exchange的话 就会无限循环
            channel.basicPublish("exchange.settlement.order"
                    , "", null, messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
