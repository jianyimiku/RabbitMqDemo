package com.example.orderservice.controller;

import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.OrderCreateVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public void createOrder(@RequestBody OrderCreateVo orderCreateVo) {
        log.info("createOrder:orderCreateVO:[{}]", orderCreateVo);
        orderService.createOrder(orderCreateVo);
    }
}