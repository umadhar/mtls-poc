package com.example.serviceb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/service-b")
public class ServiceBController {
    
    @GetMapping("/response")
    public String respond() {
        return "Hello from Service B!";
    }

    @GetMapping("/hello")
    public Map<String,String> hello() {
        return Map.of("service","B", "message","Hello from Service B");
    }
}