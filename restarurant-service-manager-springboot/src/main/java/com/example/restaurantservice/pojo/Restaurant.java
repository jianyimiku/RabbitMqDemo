package com.example.restaurantservice.pojo;

import com.example.restaurantservice.constant.RestruantStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/22 下午1:16
 * @description：
 * @modified By：
 * @version: $
 */
@Data
public class Restaurant {
    private Integer id;
    private String name;
    private String address;
    private RestruantStatus status;
    private Integer settlementId;
    private LocalDateTime date;
}
