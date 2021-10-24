package com.example.reward;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/24 下午3:16
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.example.reward.dao"})
public class RewardServiceManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RewardServiceManagerApplication.class, args);
    }
}
