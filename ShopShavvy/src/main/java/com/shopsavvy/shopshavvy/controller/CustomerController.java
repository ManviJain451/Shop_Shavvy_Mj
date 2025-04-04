package com.shopsavvy.shopshavvy.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy")
public class CustomerController {

    @GetMapping("/customer/hello")
    public String sayHello(String token){
        return "hello customer";
    }
}
