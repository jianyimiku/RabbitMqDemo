package com.example.delivery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/23 下午2:31
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.example.delivery.dao"})
public class DeliveryServiceManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceManagerApplication.class, args);
    }
}
