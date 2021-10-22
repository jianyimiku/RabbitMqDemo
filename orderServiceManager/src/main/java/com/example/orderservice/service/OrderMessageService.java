package com.example.orderservice.service;

import com.example.orderservice.dao.OrderDetailMapper;
import com.example.orderservice.dto.OrderMessageDto;
import com.example.orderservice.enummration.OrderStatusEnum;
import com.example.orderservice.pojo.OrderDetail;
import com.example.orderservice.util.RabbitMqUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/17 下午9:57
 * @description：消息处理相关的逻辑
 * @modified By：
 * @version: $
 */
@Service
@Slf4j
public class OrderMessageService {
    @Autowired
    private RabbitMqUtil rabbitMqUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 声明消息队列 交换机 绑定 消息的处理
     */
    @Async
    public void handleMessage() {
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            /*------restaurant--------*/
            channel.exchangeDeclare("exchange.order.restaurant",
                    BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare("queue.order", true,
                    false, false, null);
            channel.queueBind("queue.order", "exchange.order.restaurant",
                    "key.order");

            /*----------deliveryman------------*/
            channel.exchangeDeclare("exchange.order.deliveryman",
                    BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueBind("queue.order", "exchange.order.deliverman",
                    "key.order");


            /*-----------Consumer------------*/
            channel.basicConsume("queue.order",
                    true,
                    deliverCallback, consumerTag -> {
                    });

            while (true) {
                Thread.sleep(1000000000);
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 接收到消息以后调用
    DeliverCallback deliverCallback = ((consumerTag, message) -> {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            //反序列化
            OrderMessageDto orderMessageDto
                    = objectMapper.readValue(messageBody, OrderMessageDto.class);
            // 数据库中读取订单
            OrderDetail orderDetail =
                    orderDetailMapper.selectByPrimaryKey(orderMessageDto.getOrderId());

            switch (orderDetail.getStatus()) {
                // 订单状态为正在创建
                case ORDER_CREATING:
                    // 商家微服务返回消息 消息体中已经确认并且价格不为空
                    if (orderMessageDto.getConfirmed()
                            && null != orderMessageDto.getPrice()) {
                        orderDetail.setStatus(OrderStatusEnum.RESTAURANT_CONFIRMED);
                        orderDetail.setPrice(orderMessageDto.getPrice());
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                        // 给骑手微服务发送消息
                        sendMessageToDeliveryMan(orderMessageDto);
                    } else {
                        // 订单失败
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case RESTAURANT_CONFIRMED:
                    break;
                case DELIVERYMAN_CONFIRMED:
                    break;
                case SETTLEMENT_CONFIRMED:
                    break;
                case ORDER_CREATED:
                    break;
                case FAILED:
                    break;
            }
        } catch (Exception e) {
            log.error("序列化发生异常:[{}]", e.getMessage(), e);
        }
    });

    public void sendMessageToDeliveryMan(OrderMessageDto orderMessageDto) {
        try (
                Connection connection =
                        rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            // 给骑手微服务发消息
            channel.basicPublish("exchange.order.deliveryman"
                    , "key.deliverman"
                    , null, messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
