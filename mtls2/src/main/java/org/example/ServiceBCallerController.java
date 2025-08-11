package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/mtls2")
public class ServiceBCallerController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/call-service-b")
    public ResponseEntity<String> callServiceB() {
        // Call service-b via the gateway
        String response = restTemplate.getForObject("https://localhost:8080/service-b/hello", String.class);
        return ResponseEntity.ok(response);
    }
}

