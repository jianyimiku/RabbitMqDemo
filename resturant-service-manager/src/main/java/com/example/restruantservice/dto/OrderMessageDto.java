package com.example.restruantservice.dto;

import com.example.restruantservice.constant.OrderStatusEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/22 下午1:05
 * @description：
 * @modified By：
 * @version: $
 */
@Data
public class OrderMessageDto {
    /**
     * 订单ID
     */
    private Integer orderId;
    /**
     * 订单状态
     */
    private OrderStatusEnum orderStatus;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 骑手ID
     */
    private Integer deliverymanId;

    /**
     * 产品ID
     */
    private Integer productId;

    /**
     * 用户ID
     */
    private Integer accountId;

    /**
     * 结算ID
     */
    private Integer settlementId;

    /**
     * 积分结算ID
     */
    private Integer rewardId;

    /**
     * 积分奖励数量
     */
    private Integer rewardAmount;

    /**
     * 确认
     */
    private Boolean confirmed;
}
