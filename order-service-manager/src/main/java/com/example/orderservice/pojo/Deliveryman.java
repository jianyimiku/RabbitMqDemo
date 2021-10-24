package com.example.orderservice.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Deliveryman {
    private Integer id;

    private String name;

    private String status;

    private LocalDateTime date;
}