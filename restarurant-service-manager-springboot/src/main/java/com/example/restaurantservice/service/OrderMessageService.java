package com.example.restaurantservice.service;

import com.example.restaurantservice.constant.ProductStatus;
import com.example.restaurantservice.constant.RestruantStatus;
import com.example.restaurantservice.dao.ProductMapper;
import com.example.restaurantservice.dao.RestaurantMapper;
import com.example.restaurantservice.dto.OrderMessageDto;
import com.example.restaurantservice.pojo.Product;
import com.example.restaurantservice.pojo.Restaurant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/22 下午3:30
 * @description：
 * @modified By：
 * @version: $
 */
@Service
@Slf4j
public class OrderMessageService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RestaurantMapper restaurantMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @RabbitListener(containerFactory = "rabbitListenerContainerFactory", queues = {"queue.restaurant"})
    public void handleMessage(@Payload Message message, Channel channel) {
        String messageBody = new String(message.getBody());
        try {
            OrderMessageDto orderMessageDto
                    = objectMapper.readValue(messageBody, OrderMessageDto.class);
            Product product = productMapper.selectByPrimaryKey(orderMessageDto.getProductId());
            // 查询出商品关联的餐馆
            Restaurant restaurant
                    = restaurantMapper.selectByPrimaryKey(product.getResturantId());

            // 满足商户微服务确认订单的条件
            if (product.getStatus() == ProductStatus.AVALIABLE &&
                    restaurant.getStatus() == RestruantStatus.OPEN) {
                orderMessageDto.setConfirmed(true);
                orderMessageDto.setPrice(product.getPrice());
            } else {
                orderMessageDto.setConfirmed(false);
            }


            sendToOrder(orderMessageDto, message, channel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendToOrder(OrderMessageDto orderMessageDto, Message message, Channel channel) throws IOException {
        String messageToSend = null;
        try {
            messageToSend = objectMapper.writeValueAsString(orderMessageDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (messageToSend != null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setExpiration("15000");
            Message sendMessage = new Message(messageToSend.getBytes(), messageProperties);
            CorrelationData correlationData = new CorrelationData();
            correlationData.setId(orderMessageDto.getOrderId().toString());
            rabbitTemplate.send("exchange.order.restaurant", "key.order", sendMessage,correlationData);
        }
    }
}
