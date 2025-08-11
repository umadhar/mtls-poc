package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/mtls")
public class ServiceCallerController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/call-service-a")
    public ResponseEntity<String> callServiceA() {
        String response = restTemplate.getForObject("https://localhost:8080/service-a/hello", String.class);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/call-service-b")
    public ResponseEntity<String> callServiceB() {
        String response = restTemplate.getForObject("https://localhost:8080/service-b/hello", String.class);
        return ResponseEntity.ok(response);
    }
}