package com.example.myself.service.impl;

import com.example.myself.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private static String ORDER_KEY = "ord:";
    @Resource
    private RedisTemplate redisTemplate;

    public void addOrder() {
        int keyId = ThreadLocalRandom.current().nextInt(1000) + 1;
        String seriaNo = UUID.randomUUID().toString();
        String key = ORDER_KEY + keyId;
        String value = "京东订单:" + seriaNo;

        redisTemplate.opsForValue().set(key, value);
        log.info("*************key:{}, value:{}", key, value);
    }

    public String getOrderById(Integer keyId) {
        return (String) redisTemplate.opsForValue().get(ORDER_KEY + keyId);
    }

}
