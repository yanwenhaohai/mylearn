package com.example.myself.Controller;

import com.example.myself.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(value="order",produces = "application/json;charset=UTF-8")
public class OrderController {
    @Autowired
    OrderService orderService;

    @PostMapping("addOrder")
    public void addOrder() {
        orderService.addOrder();
    }

    @GetMapping("getOrderById")
    public String getOrderById(Integer id) {
        return orderService.getOrderById(id);
    }
}
