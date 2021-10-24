package com.example.restruantservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/22 上午11:44
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.example.restruantservice.dao"})
public class ResturantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResturantServiceApplication.class, args);
    }
}
