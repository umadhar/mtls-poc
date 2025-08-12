package com.example.serviceb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/service-b")
public class ServiceBController {

    @Autowired
    private WebClient webClient;
    
    @GetMapping("/response")
    public String respond() {
        return "Hello from Service B!";
    }

    @GetMapping("/hello")
    public Mono<String> callServiceA() {
        // Using service name instead of hardcoded URL - Eureka will resolve this
        return webClient.get()
                .uri("http://service-a/service-a/hello")
                .retrieve()
                .bodyToMono(String.class);
    }


}