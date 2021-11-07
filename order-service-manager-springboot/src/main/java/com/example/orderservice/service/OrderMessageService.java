package com.example.orderservice.service;

import com.example.orderservice.constant.OrderStatusEnum;
import com.example.orderservice.dao.OrderDetailMapper;
import com.example.orderservice.dto.OrderMessageDto;
import com.example.orderservice.pojo.OrderDetail;
import com.example.orderservice.util.RabbitMqUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
    private ObjectMapper objectMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RabbitMqUtil rabbitMqUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    // 接收到消息以后调用
    public void handleMessageWithByte(byte[] message) {
        log.info("handleMessage:{}", new String(message));
//        log.info("Channel:{}", channel);
        String messageBody = new String(message, StandardCharsets.UTF_8);

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
                    // 骑手微服务分配骑手以后 返回消息给Order
                    if (!ObjectUtils.isEmpty(orderMessageDto.getDeliverymanId())) {
                        orderDetail.setStatus(OrderStatusEnum.DELIVERYMAN_CONFIRMED);
                        orderDetail.setDeliverymanId(orderMessageDto.getDeliverymanId());
                        // 更新订单状态
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);

                        // 发送给结算微服务
                        sendMessageToSettlement(orderMessageDto);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case DELIVERYMAN_CONFIRMED:
                    // 表示结算成功
                    if (!ObjectUtils.isEmpty(orderMessageDto.getSettlementId())) {
                        orderDetail.setStatus(OrderStatusEnum.SETTLEMENT_CONFIRMED);
                        orderDetail.setSettlementId(orderMessageDto.getSettlementId());
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);

                        // 给积分微服务发消息
                        sendMessageToReward(orderMessageDto);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case SETTLEMENT_CONFIRMED:
                    if (!ObjectUtils.isEmpty(orderMessageDto.getRewardId())) {
                        orderDetail.setStatus(OrderStatusEnum.ORDER_CREATED);
                        orderDetail.setRewardId(orderMessageDto.getRewardId());
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case ORDER_CREATED:
                    break;
                case FAILED:
                    break;
            }
        } catch (Exception e) {
            log.error("序列化发生异常:[{}]", e.getMessage(), e);
        }
    }

    public void sendMessageToDeliveryMan(OrderMessageDto orderMessageDto) {
        String messageTosend = null;
        try {
            messageTosend = objectMapper.writeValueAsString(orderMessageDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (messageTosend != null) {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setExpiration("15000");
            Message message = new Message(messageTosend.getBytes(), messageProperties);
            CorrelationData correlationData = new CorrelationData();
            correlationData.setId(String.valueOf(orderMessageDto.getOrderId()));
            rabbitTemplate.send("exchange.order.deliveryman", "key.deliveryman", message, correlationData);
        } else {
            // 进行记录
            log.error("发送给配送员消息构建失败");
        }
    }

    public void sendMessageToSettlement(OrderMessageDto orderMessageDto) {
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.basicPublish("exchange.order.settlement", "", null
                    , messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToReward(OrderMessageDto orderMessageDto) {
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.basicPublish("exchange.order.reward", "key.reward", null
                    , messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }


    // 接收到消息以后调用
    public void handleMessageNoByte(OrderMessageDto orderMessageDto) {
        log.info("handleMessage:{}", orderMessageDto);
        try {
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
                    // 骑手微服务分配骑手以后 返回消息给Order
                    if (!ObjectUtils.isEmpty(orderMessageDto.getDeliverymanId())) {
                        orderDetail.setStatus(OrderStatusEnum.DELIVERYMAN_CONFIRMED);
                        orderDetail.setDeliverymanId(orderMessageDto.getDeliverymanId());
                        // 更新订单状态
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);

                        // 发送给结算微服务
                        sendMessageToSettlement(orderMessageDto);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case DELIVERYMAN_CONFIRMED:
                    // 表示结算成功
                    if (!ObjectUtils.isEmpty(orderMessageDto.getSettlementId())) {
                        orderDetail.setStatus(OrderStatusEnum.SETTLEMENT_CONFIRMED);
                        orderDetail.setSettlementId(orderMessageDto.getSettlementId());
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);

                        // 给积分微服务发消息
                        sendMessageToReward(orderMessageDto);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case SETTLEMENT_CONFIRMED:
                    if (!ObjectUtils.isEmpty(orderMessageDto.getRewardId())) {
                        orderDetail.setStatus(OrderStatusEnum.ORDER_CREATED);
                        orderDetail.setRewardId(orderMessageDto.getRewardId());
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case ORDER_CREATED:
                    break;
                case FAILED:
                    break;
            }
        } catch (Exception e) {
            log.error("序列化发生异常:[{}]", e.getMessage(), e);
        }
    }


    // 接收到消息以后调用
    @RabbitListener(containerFactory = "rabbitListenerContainerFactory", queues = "queue.order")
    public void handleMessageWithRabbitListener(@Payload Message message, Channel channel) {
        log.info("handleMessage:{}", new String(message.getBody()));
        log.info("Channel:{}", channel);
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
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                        // 给骑手微服务发送消息
                        sendMessageToDeliveryMan(orderMessageDto);
                    } else {
                        // 订单失败
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case RESTAURANT_CONFIRMED:
                    // 骑手微服务分配骑手以后 返回消息给Order
                    if (!ObjectUtils.isEmpty(orderMessageDto.getDeliverymanId())) {
                        orderDetail.setStatus(OrderStatusEnum.DELIVERYMAN_CONFIRMED);
                        orderDetail.setDeliverymanId(orderMessageDto.getDeliverymanId());
                        // 更新订单状态
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);

                        // 发送给结算微服务
                        sendMessageToSettlement(orderMessageDto);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case DELIVERYMAN_CONFIRMED:
                    // 表示结算成功
                    if (!ObjectUtils.isEmpty(orderMessageDto.getSettlementId())) {
                        orderDetail.setStatus(OrderStatusEnum.SETTLEMENT_CONFIRMED);
                        orderDetail.setSettlementId(orderMessageDto.getSettlementId());
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);

                        // 给积分微服务发消息
                        sendMessageToReward(orderMessageDto);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case SETTLEMENT_CONFIRMED:
                    if (!ObjectUtils.isEmpty(orderMessageDto.getRewardId())) {
                        orderDetail.setStatus(OrderStatusEnum.ORDER_CREATED);
                        orderDetail.setRewardId(orderMessageDto.getRewardId());
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    } else {
                        orderDetail.setStatus(OrderStatusEnum.FAILED);
                        orderDetailMapper.updateByPrimaryKeySelective(orderDetail);
                    }
                    break;
                case ORDER_CREATED:
                    break;
                case FAILED:
                    break;
            }
        } catch (Exception e) {
            log.error("序列化发生异常:[{}]", e.getMessage(), e);
        }
    }
}
