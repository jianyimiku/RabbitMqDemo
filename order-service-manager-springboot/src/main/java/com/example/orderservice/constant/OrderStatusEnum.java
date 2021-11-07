package com.example.orderservice.constant;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/17 下午8:59
 * @description：
 * @modified By：
 * @version: $
 */
public enum OrderStatusEnum {
    /**
     * 创建中
     */
    ORDER_CREATING,

    /**
     * 餐厅已经确认
     */
    RESTAURANT_CONFIRMED,

    /**
     * 骑手确认
     */
    DELIVERYMAN_CONFIRMED,

    /**
     * 结算完成
     */
    SETTLEMENT_CONFIRMED,

    /**
     * 订单已创建
     */
    ORDER_CREATED,

    /**
     * 订单创建失败
     */
    FAILED;
}
