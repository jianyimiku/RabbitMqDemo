package com.example.settlement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/24 下午1:53
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.example.settlement.dao"})
public class SettlementServiceManager {
    public static void main(String[] args) {
        SpringApplication.run(SettlementServiceManager.class, args);
    }
}
