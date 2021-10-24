package com.example.restruantservice.pojo;

import com.example.restruantservice.constant.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/22 下午1:07
 * @description：
 * @modified By：
 * @version: $
 */
@Data
public class Product {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer resturantId;
    private ProductStatus status;
    private LocalDateTime date;
}
