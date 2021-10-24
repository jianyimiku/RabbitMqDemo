package com.example.orderservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/17 下午3:16
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootApplication
@MapperScan(value = {"com.example.orderservice.dao"})
public class OrderServiceManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceManagerApplication.class, args);
    }
}
