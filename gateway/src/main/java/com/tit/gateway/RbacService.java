package com.tit.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConfigurationProperties(prefix = "rbac")
public class RbacService {
    
    private Map<String, List<String>> roles = new HashMap<>();
    private Map<String, String> certificates = new HashMap<>();
    private List<String> publicEndpoints = new ArrayList<>();
    
    // Cache for performance
    private final Map<String, Boolean> permissionCache = new ConcurrentHashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    public String getRoleForCertificate(String cn) {
        return certificates.get(cn);
    }
    
    public boolean hasPermission(String role, String path) {
        String cacheKey = role + ":" + path;
        return permissionCache.computeIfAbsent(cacheKey, k -> checkPermission(role, path));
    }
    
    public boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    public boolean isInternalTraffic(String cn) {
        // Method 1: Check CN suffix
        if (cn.endsWith(".internal")) return true;
        
        // Method 2: Check if it's in internal services list
        return cn.startsWith("service-") || cn.startsWith("internal-");
    }
    
    public boolean isExternalTraffic(String cn) {
        return !isInternalTraffic(cn);
    }
    
    private boolean checkPermission(String role, String path) {
        List<String> patterns = roles.get(role);
        if (patterns == null) return false;
        
        return patterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    // Getters and setters for configuration binding
    public Map<String, List<String>> getRoles() { return roles; }
    public void setRoles(Map<String, List<String>> roles) { this.roles = roles; }
    public Map<String, String> getCertificates() { return certificates; }
    public void setCertificates(Map<String, String> certificates) { this.certificates = certificates; }
    public List<String> getPublicEndpoints() { return publicEndpoints; }
    public void setPublicEndpoints(List<String> publicEndpoints) { this.publicEndpoints = publicEndpoints; }
}