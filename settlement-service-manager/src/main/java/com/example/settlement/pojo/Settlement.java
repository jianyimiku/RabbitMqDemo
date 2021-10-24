package com.example.settlement.pojo;

import com.example.settlement.constant.SettlementStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Settlement {
    private Integer id;

    private Integer orderId;

    private Integer transactionId;

    private BigDecimal amount;

    private SettlementStatus status;

    private LocalDateTime date;
}