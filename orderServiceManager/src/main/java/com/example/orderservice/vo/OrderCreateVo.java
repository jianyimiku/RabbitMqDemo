package com.example.orderservice.vo;

import lombok.Data;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/17 下午4:18
 * @description：
 * @modified By：
 * @version: $
 */
@Data
public class OrderCreateVo {
    /**
     * 用户ID
     */
    private Integer accountId;
    /**
     * 地址
     */
    private String address;
    /**
     * 产品ID
     */
    private Integer productId;
}
