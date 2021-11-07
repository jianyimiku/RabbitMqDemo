package com.example.restaurantservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ：sjq
 * @date ：Created in 2021/11/7 下午12:55
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.example.restaurantservice.dao"})
public class RestaurantServiceManagerSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceManagerSpringBootApplication.class
        , args);
    }
}
