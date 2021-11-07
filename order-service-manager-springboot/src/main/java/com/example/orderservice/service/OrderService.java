package com.example.orderservice.service;

import com.example.orderservice.constant.OrderStatusEnum;
import com.example.orderservice.dao.OrderDetailMapper;
import com.example.orderservice.dto.OrderMessageDto;
import com.example.orderservice.pojo.OrderDetail;
import com.example.orderservice.vo.OrderCreateVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConfirmCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/17 下午9:57
 * @description：用户请求相关逻辑
 * @modified By：
 * @version: $
 */
@Service
@Slf4j
public class OrderService {
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void createOrder(OrderCreateVo orderCreateVo) {
        // 订单创建
        OrderDetail orderDetail = new OrderDetail();
        BeanUtils.copyProperties(orderCreateVo, orderDetail);
        orderDetail.setStatus(OrderStatusEnum.ORDER_CREATING);
        orderDetail.setDate(LocalDateTime.now());

        orderDetailMapper.insert(orderDetail);

        // 餐厅微服务发送消息体
        OrderMessageDto orderMessageDto = new OrderMessageDto();
        orderMessageDto.setOrderId(orderDetail.getId());
        orderMessageDto.setProductId(orderDetail.getProductId());
        orderMessageDto.setAccountId(orderDetail.getAccountId());

        try {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            // 这边必须声明MessageProperties
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setExpiration("15000");
            Message message = new Message(messageToSend.getBytes(), messageProperties);
            // Base class for correlating publisher confirms to sent messages.
            // 用于将发送消息与确认信息相关联的基类
            CorrelationData correlationData = new CorrelationData();
            correlationData.setId(orderMessageDto.getOrderId().toString());
            rabbitTemplate.send("exchange.order.restaurant", "key.restaurant",
                    message, correlationData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
