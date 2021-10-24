package com.example.orderservice.service;

import com.example.orderservice.dao.OrderDetailMapper;
import com.example.orderservice.dto.OrderMessageDto;
import com.example.orderservice.enummration.OrderStatusEnum;
import com.example.orderservice.pojo.OrderDetail;
import com.example.orderservice.util.RabbitMqUtil;
import com.example.orderservice.vo.OrderCreateVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            // 开启确认模式
            channel.confirmSelect();
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.exchangeDeclare("exchange.order.restaurant"
                    , BuiltinExchangeType.DIRECT, true, false, null);
            channel.basicPublish("exchange.order.restaurant"
                    , "key.restaurant", null, messageToSend.getBytes());
            log.info("message sent");
            // 单条消息确认 发送端是否发送
            if (channel.waitForConfirms()) {
                log.info("RabbitMq confirm success");
            } else {
                log.error("RabbitMq confirm failed");
            }
        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 多条同步确认
     *
     * @param orderCreateVo
     */
    public void createOrderManyConfirm(OrderCreateVo orderCreateVo) {
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
            // 开启确认模式
            channel.confirmSelect();
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.exchangeDeclare("exchange.order.restaurant"
                    , BuiltinExchangeType.DIRECT, true, false, null);
            for (int i = 0; i < 50; i++) {
                // 模拟多次请求确认
                channel.basicPublish("exchange.order.restaurant"
                        , "key.restaurant", null, messageToSend.getBytes());
                // 单条消息确认 发送端是否发送
                if (channel.waitForConfirms()) {
                    log.info("RabbitMq confirm success");
                } else {
                    log.error("RabbitMq confirm failed");
                }
            }
        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 异步同步确认
     *
     * @param orderCreateVo
     */
    public void createOrderAsyncConfirm(OrderCreateVo orderCreateVo) {
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
            // 开启确认模式
            channel.confirmSelect();
            ConfirmListener confirmListener = new ConfirmListener() {
                /**
                 * @param deliveryTag 发送端的消息序号 发送端发送的第几条消息
                 * @param multiple 多条 true 还是 单条 false
                 * @throws IOException
                 */
                @Override
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    log.info("Ack,deliveryTag:{},multiple:{}", deliveryTag, multiple);
                }

                /**
                 * 确认失败会调用
                 * @param deliveryTag
                 * @param multiple
                 * @throws IOException
                 */
                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    log.info("Nack,deliveryTag:{},multiple:{}", deliveryTag, multiple);
                }
            };
            channel.addConfirmListener(confirmListener);
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.exchangeDeclare("exchange.order.restaurant"
                    , BuiltinExchangeType.DIRECT, true, false, null);
//            channel.basicPublish("exchange.order.restaurant"
//                    , "key.restaurant", null, messageToSend.getBytes());
            for (int i = 0; i < 10; i++) {
                // 模拟多次请求确认
                //Ack,deliveryTag:1,multiple:false 表示确认的第一条 单条确认
                //Ack,deliveryTag:10,multiple:true 表示连带着前面10条全部确认 多条确认模式
                channel.basicPublish("exchange.order.restaurant"
                        , "key.restaurant", null, messageToSend.getBytes());
                log.info("message sent");
            }
            /**
             * 保证回调会收到
             */
            Thread.sleep(10000);
        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送带有过期时间的消息
     *
     * @param orderCreateVo
     */
    public void createOrderWithTTL(OrderCreateVo orderCreateVo) {
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
            // 开启确认模式
            channel.confirmSelect();
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.exchangeDeclare("exchange.order.restaurant"
                    , BuiltinExchangeType.DIRECT, true, false, null);
            AMQP.BasicProperties props = new AMQP.BasicProperties();
            AMQP.BasicProperties properties = props.builder().expiration("15000").build();
            channel.basicPublish("exchange.order.restaurant"
                    , "key.restaurant", properties, messageToSend.getBytes());
            log.info("message sent");
            // 单条消息确认 发送端是否发送
            if (channel.waitForConfirms()) {
                log.info("RabbitMq confirm success");
            } else {
                log.error("RabbitMq confirm failed");
            }
        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
