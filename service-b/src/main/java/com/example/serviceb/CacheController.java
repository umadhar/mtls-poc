package com.example.serviceb;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @PostMapping("/clear")
    public String clearCache() {
        try {
            // Force refresh the local cache from Eureka server
            eurekaClient.getApplications();
            return "Cache cleared and refreshed successfully!";
        } catch (Exception e) {
            return "Failed to clear cache: " + e.getMessage();
        }
    }

    @PostMapping("/refresh")
    public String refreshServices() {
        try {
            // Get fresh list of services
            var services = discoveryClient.getServices();
            return "Services refreshed: " + services.toString();
        } catch (Exception e) {
            return "Failed to refresh services: " + e.getMessage();
        }
    }
}
