package com.example.delivery.pojo;

import com.example.delivery.constant.DeliveryManStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Deliveryman {
    private Integer id;

    private String name;

    private DeliveryManStatus status;

    private LocalDateTime date;
}