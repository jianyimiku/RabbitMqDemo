package com.example.restruantservice.service;

import com.example.restruantservice.constant.ProductStatus;
import com.example.restruantservice.constant.RestruantStatus;
import com.example.restruantservice.dao.ProductMapper;
import com.example.restruantservice.dao.RestaurantMapper;
import com.example.restruantservice.dto.OrderMessageDto;
import com.example.restruantservice.pojo.Product;
import com.example.restruantservice.pojo.Restaurant;
import com.example.restruantservice.util.RabbitMqUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RabbitMqUtil rabbitMqUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RestaurantMapper restaurantMapper;


    /**
     * 消息处理
     */
    @Async
    public void handleMessage() {
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {

            channel.exchangeDeclare("exchange.order.restaurant",
                    BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare("queue.restaurant"
                    , true, false, false, null);

            channel.queueBind("queue.restaurant", "exchange.order.restaurant"
                    , "key.restaurant");


            // 消费
            channel.basicConsume("queue.restaurant", true
                    , deliverCallback, consumerTag -> {
                    });

            while (true) {
                Thread.sleep(10000000);
            }

        } catch (TimeoutException | IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }


    DeliverCallback deliverCallback = ((consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {

            OrderMessageDto orderMessageDto
                    = objectMapper.readValue(messageBody, OrderMessageDto.class);

            // 查询出商品
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

            // 传送的消息体构造完成 开始传输消息
            sendToOrder(orderMessageDto);

        } catch (TimeoutException e) {
            log.error(e.getMessage(), e);
        }
    });

    public void sendToOrder(OrderMessageDto orderMessageDto) {
        try (
                Connection connection =
                        rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            // 给Order返回消息
            channel.basicPublish("exchange.order.restaurant"
                    , "key.order", null, messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
