package com.example.orderservice.service;

import com.example.orderservice.dao.OrderDetailMapper;
import com.example.orderservice.dto.OrderMessageDto;
import com.example.orderservice.enummration.OrderStatusEnum;
import com.example.orderservice.pojo.OrderDetail;
import com.example.orderservice.util.RabbitMqUtil;
import com.example.orderservice.vo.OrderCreateVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/17 下午9:57
 * @description：用户请求相关逻辑
 * @modified By：
 * @version: $
 */
@Service
public class OrderService {
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RabbitMqUtil rabbitMqUtil;

    @Autowired
    private ObjectMapper objectMapper;

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

        // 构建生产者
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.exchangeDeclare("exchange.order.restaurant"
                    , BuiltinExchangeType.DIRECT, true, false, null);
            channel.basicPublish("exchange.order.restaurant"
                    , "key.restaurant", null, messageToSend.getBytes());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
