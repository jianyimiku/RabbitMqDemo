package com.example.settlement.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/24 下午2:10
 * @description：
 * @modified By：
 * @version: $
 */
@Service
public class SettlementService {

    Random random = new Random(25);

    public Integer settlement(Integer accountId, BigDecimal amount) {
        // 用一个随机数表示处理完了
        return random.nextInt(100000000);
    }
}
