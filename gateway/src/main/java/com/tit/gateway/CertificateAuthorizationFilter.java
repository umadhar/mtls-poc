package com.tit.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;

@Component
public class CertificateAuthorizationFilter extends AbstractGatewayFilterFactory<Object> {
    
    @Autowired
    private RbacService rbacService;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            
            // Check if it's a public endpoint (no certificate required)
            if (rbacService.isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }
            
            // Extract client certificate with null safety
            if (exchange.getRequest().getSslInfo() == null) {
                return unauthorized(exchange);
            }

            X509Certificate[] certs = exchange.getRequest().getSslInfo().getPeerCertificates();
            if (certs == null || certs.length == 0) {
                return unauthorized(exchange);
            }
            
            String clientCN = extractCN(certs[0].getSubjectX500Principal());
            if (clientCN == null) {
                return unauthorized(exchange);
            }

            // RBAC using service
            String role = rbacService.getRoleForCertificate(clientCN);
            if (role == null) {
                return unauthorized(exchange);
            }
            
            if (rbacService.hasPermission(role, path)) {
                return chain.filter(exchange);
            } else {
                return forbidden(exchange);
            }
        };
    }
    
    private String extractCN(X500Principal principal) {
        String dn = principal.getName();
        for (String part : dn.split(",")) {
            if (part.trim().startsWith("CN=")) {
                return part.trim().substring(3);
            }
        }
        return null;
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
    
    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

}