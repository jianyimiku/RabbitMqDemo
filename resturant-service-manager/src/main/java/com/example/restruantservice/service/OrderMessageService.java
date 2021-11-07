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
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
     * 单例模式 所以全局唯一 或者创建一个Bean交给Spring管理
     */
    Channel channel;


    /**
     * 消息处理
     */
    @Async
    public void handleMessage() {
        try (
                Connection connection = rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            this.channel = channel;
            channel.exchangeDeclare("exchange.order.restaurant",
                    BuiltinExchangeType.DIRECT, true, false, null);

//            channel.exchangeDeclare("exchange.dlx", BuiltinExchangeType.TOPIC,
//                    true, false, null);
//            channel.queueDeclare("queue.dlx", true, false, false, null);
//
//            channel.queueBind("queue.dlx", "exchange.dlx", "#");

            // 设置队列消息整体的过期时间
//            Map<String, Object> args = new HashMap<>(16);
//            args.put("x-message-ttl", 15000);
//            args.put("x-max-length", 5);
//            args.put("x-dead-letter-exchange", "exchange.dlx");


            channel.queueDeclare("queue.restaurant"
                    , true, false, false, null);


//            channel.queueDeclare("queue.restaurant"
//                    , true, false, false, null);

            channel.queueBind("queue.restaurant", "exchange.order.restaurant"
                    , "key.restaurant");

            // 限流
            channel.basicQos(2);

            // 消费
            channel.basicConsume("queue.restaurant", false
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
        try {

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
//            sendToOrder(orderMessageDto, message);

            sendToOrderTestQos(orderMessageDto, message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    });

    public void sendToOrder(OrderMessageDto orderMessageDto, Delivery message) {
        try {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.confirmSelect();
            // 给调用方加载ReturnListener
            ReturnListener returnListener = new ReturnListener() {
                /**
                 * 消息要发给交换机但是交换机路由失败会调用
                 * @param replyCode 数字字码表示消息路由的结果
                 * @param replyText 返回的信息
                 * @param exchange 路由
                 * @param routingKey 路由key
                 * @param properties 消息参数
                 * @param body 消息内容
                 * @throws IOException
                 */
                @Override
                public void handleReturn(int replyCode, String replyText, String exchange,
                                         String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    log.info("Message Return Listener:replyCode:{},replyText:{},exchange:{},routingKey:{},properties:{},body:{}",
                            replyCode, replyText, exchange, routingKey, properties, new String(body));
                }
            };
            channel.addReturnListener(returnListener);

            // 手动签收
            /**
             * 要用创建消费者的那个channel进行签收而不是新的channel
             * 第一个参数是发送端发送序号
             * 第二个参数是是否多条确认
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);

//            /**
//             * 手动拒收 并且 重回队列
//             */
//            channel.basicNack(message.getEnvelope().getDeliveryTag(), false, true);

            // 给Order返回消息
            channel.basicPublish("exchange.order.restaurant"
                    , "key.order", true, null, messageToSend.getBytes());
            if (channel.waitForConfirms()) {
                log.info("message sent");
            }
            // 防止线程执行完成 退出 消息返回机制的回调还没有回调
            // 因为是Channel在执行的 线程执行完成以后channel会被自动关闭(我们用来TWR) 可以选择手动关闭不自动关闭
            Thread.sleep(1000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void sendToOrderReturnCallBack(OrderMessageDto orderMessageDto) {
        try (
                Connection connection =
                        rabbitMqUtil.getConnection();
                Channel channel = connection.createChannel();
        ) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.confirmSelect();

            // ReturnCallBack
            channel.addReturnListener(new ReturnCallback() {
                @Override
                public void handle(Return returnMessage) {
                    log.info("returnMessage:{}", returnMessage);
                }
            });
            // 给Order返回消息
            channel.basicPublish("exchange.order.restaurant"
                    , "key.order", true, null, messageToSend.getBytes());
            if (channel.waitForConfirms()) {
                log.info("message sent");
            }
            // 防止线程执行完成 退出 消息返回机制的回调还没有回调
            // 因为是Channel在执行的 线程执行完成以后channel会被自动关闭(我们用来TWR) 可以选择手动关闭不自动关闭
            Thread.sleep(1000);

        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试消息限流的方法
     *
     * @param orderMessageDto
     * @param message
     */
    public void sendToOrderTestQos(OrderMessageDto orderMessageDto, Delivery message) {
        try {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDto);
            channel.confirmSelect();
            // 给调用方加载ReturnListener
            ReturnListener returnListener = new ReturnListener() {
                /**
                 * 消息要发给交换机但是交换机路由失败会调用
                 * @param replyCode 数字字码表示消息路由的结果
                 * @param replyText 返回的信息
                 * @param exchange 路由
                 * @param routingKey 路由key
                 * @param properties 消息参数
                 * @param body 消息内容
                 * @throws IOException
                 */
                @Override
                public void handleReturn(int replyCode, String replyText, String exchange,
                                         String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    log.info("Message Return Listener:replyCode:{},replyText:{},exchange:{},routingKey:{},properties:{},body:{}",
                            replyCode, replyText, exchange, routingKey, properties, new String(body));
                }
            };
            channel.addReturnListener(returnListener);


            // 模拟消息处理很慢 用来测试限流
            Thread.sleep(3000);

            // 手动签收
            /**
             * 要用创建消费者的那个channel进行签收而不是新的channel
             * 第一个参数是发送端发送序号
             * 第二个参数是是否多条确认
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);

            // 给Order返回消息
            channel.basicPublish("exchange.order.restaurant"
                    , "key.order", true, null, messageToSend.getBytes());
            if (channel.waitForConfirms()) {
                log.info("message sent");
            }
            // 防止线程执行完成 退出 消息返回机制的回调还没有回调
            // 因为是Channel在执行的 线程执行完成以后channel会被自动关闭(我们用来TWR) 可以选择手动关闭不自动关闭
            Thread.sleep(1000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
