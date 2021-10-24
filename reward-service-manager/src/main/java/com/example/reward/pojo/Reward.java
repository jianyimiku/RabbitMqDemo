package com.example.reward.pojo;

import com.example.reward.constant.RewardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Reward {
    private Integer id;

    private Integer orderId;

    private BigDecimal amount;

    private RewardStatus status;

    private LocalDateTime date;
}