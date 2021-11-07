package com.example.orderservice.pojo;
import com.example.orderservice.constant.OrderStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetail {
    private Integer id;

    private OrderStatusEnum status;

    private String address;

    private Integer accountId;

    private Integer productId;

    private Integer deliverymanId;

    private Integer settlementId;

    private Integer rewardId;

    private BigDecimal price;

    private LocalDateTime date;
}