package com.example.servicea;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServiceAController {

    @GetMapping("/service-a/hello")
    public Map<String,String> hello() {
        return Map.of("service","A", "message","Hello from Service A");
    }
}