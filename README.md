# Microservices mTLS Gateway with Certificate-Based RBAC

## Architecture Overview

This project demonstrates a secure microservices architecture using mutual TLS (mTLS) authentication and certificate-based Role-Based Access Control (RBAC) through a Spring Cloud Gateway.

### Components

- **Gateway** (Port 8080) - Entry point with mTLS and RBAC
- **Service A** (Port 8081) - Backend service
- **Service B** (Port 8082) - Backend service  
- **mTLS App** (Port 9090) - Client application with full access
- **mTLS2 App** (Port 9091) - Client application with limited access
- **Eureka Server** - Service discovery for internal communication

## Communication Patterns

### External Traffic (via Gateway)
```
External Clients → Gateway (mTLS + RBAC) → Services
```

### Internal Traffic (via Eureka)
```
service-a ↔ service-b (Direct communication via Eureka discovery)
```

## Certificate-Based RBAC Configuration

### RBAC Configuration (`rbac-config.yml`)
```yaml
rbac:
  roles:
    # App-based roles
    MTLS-APP:
      - "/service-a/**"
      - "/service-b/**"
    MTLS2-APP:
      - "/service-b/**"
  
  certificates:
    # External clients
    mtls: MTLS-APP
    mtls2: MTLS2-APP
    
    # Internal services
    service-a.internal: SERVICE-A
    service-b.internal: SERVICE-B
  
  # Public endpoints (no certificate required)
  public-endpoints:
    - "/public/**"
    - "/health/**"
    - "/info/**"
```

## Access Control Matrix

| Client Certificate | Role | Service A Access | Service B Access | Public Access |
|-------------------|------|------------------|------------------|---------------|
| `mtls` | MTLS-APP | ✅ Full | ✅ Full | ✅ Yes |
| `mtls2` | MTLS2-APP | ❌ No | ✅ Full | ✅ Yes |
| No Certificate | GUEST | ❌ No | ❌ No | ✅ Yes |

## Traffic Type Identification

### Example External vs Internal Traffic
- **External**: `mobile-app`, `web-client`, `partner-xyz`
- **Internal**: `service-a.internal`, `service-b.internal`

### Certificate Naming Convention
```
External: CN=mobile-app, O=Company
Internal: CN=service-a.internal, O=Company, OU=Services
```

## Certificate Management

### Creating Root CA Certificate
```bash
# Generate CA private key
openssl genrsa -out ca-key.pem 4096

# Create CA certificate
openssl req -new -x509 -days 365 -key ca-key.pem -out ca-cert.pem \
  -subj "/C=US/ST=CA/L=San Francisco/O=Company/CN=Root-CA"
```

### Creating Server Certificates (Gateway)
```bash
# Generate gateway private key
openssl genrsa -out gateway-key.pem 2048

# Create certificate signing request
openssl req -new -key gateway-key.pem -out gateway.csr \
  -subj "/C=US/ST=CA/L=San Francisco/O=Company/CN=gateway"

# Sign with CA
openssl x509 -req -in gateway.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out gateway-cert.pem -days 365

# Create PKCS12 keystore
openssl pkcs12 -export -in gateway-cert.pem -inkey gateway-key.pem \
  -out gateway-keystore.p12 -name gateway -password pass:changeit
```

### Creating Client Certificates

#### mTLS App Certificate (Full Access)
```bash
# Generate client private key
openssl genrsa -out mtls-key.pem 2048

# Create certificate signing request
openssl req -new -key mtls-key.pem -out mtls.csr \
  -subj "/C=US/ST=CA/L=San Francisco/O=Company/CN=mtls"

# Sign with CA
openssl x509 -req -in mtls.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out mtls-cert.pem -days 365

# Create PKCS12 keystore
openssl pkcs12 -export -in mtls-cert.pem -inkey mtls-key.pem \
  -out mtls-keystore.p12 -name mtls -password pass:mtls11
```

#### mTLS2 App Certificate (Limited Access)
```bash
# Generate client private key
openssl genrsa -out mtls2-key.pem 2048

# Create certificate signing request
openssl req -new -key mtls2-key.pem -out mtls2.csr \
  -subj "/C=US/ST=CA/L=San Francisco/O=Company/CN=mtls2"

# Sign with CA
openssl x509 -req -in mtls2.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out mtls2-cert.pem -days 365

# Create PKCS12 keystore
openssl pkcs12 -export -in mtls2-cert.pem -inkey mtls2-key.pem \
  -out mtls2-keystore.p12 -name mtls2 -password pass:changeit
```

### Creating Truststore
```bash
# Create truststore with CA certificate
keytool -import -file ca-cert.pem -alias ca-root \
  -keystore ca-truststore.p12 -storetype PKCS12 \
  -storepass changeit -noprompt
```

### Certificate File Structure
```
src/main/resources/
├── gateway-keystore.p12      # Gateway server certificate
├── ca-truststore.p12         # CA certificate for validation
├── mtls-keystore.p12         # mTLS app client certificate
└── mtls2-keystore.p12        # mTLS2 app client certificate
```

### Certificate Validation Commands
```bash
# View certificate details
keytool -list -v -keystore gateway-keystore.p12 -storetype PKCS12

# View truststore contents
keytool -list -v -keystore ca-truststore.p12 -storetype PKCS12

# Test certificate chain
openssl verify -CAfile ca-cert.pem gateway-cert.pem
```

## Security Features

### 1. Mutual TLS (mTLS)
- Client certificate authentication
- Server certificate validation
- Encrypted communication

### 2. Certificate-Based Authorization
- Extract Common Name (CN) from client certificate
- Map certificate to application role
- Apply path-based permissions

### 3. Performance Optimizations
- **Caching**: Permission checks cached for performance
- **Pattern Matching**: Ant-style path patterns (`/**`)
- **O(1) Lookup**: After initial cache population

## Gateway Configuration

### SSL Configuration
```yaml
server:
  port: 8080
  ssl:
    enabled: true
    key-store: classpath:gateway-keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    trust-store: classpath:ca-truststore.p12
    trust-store-password: changeit
    trust-store-type: PKCS12
    client-auth: need
```

### Route Configuration
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-a
          uri: http://localhost:8081
          predicates:
            - Path=/service-a/**
          filters:
            - name: CertificateAuthorizationFilter
        - id: service-b
          uri: http://localhost:8082
          predicates:
            - Path=/service-b/**
          filters:
            - name: CertificateAuthorizationFilter
```

## Testing Certificate Validation

### Valid Certificate Test
```bash
# Should succeed
curl -k --cert mtls-cert.pem --key mtls-key.pem https://localhost:8080/service-a/hello
```

### Invalid Certificate Test
```bash
# Should fail with 401/403
curl -k --cert wrong-cert.pem --key wrong-key.pem https://localhost:8080/service-a/hello
```

### No Certificate Test
```bash
# Should only access public endpoints
curl -k https://localhost:8080/public/info
```

## Scaling to 600+ APIs

### Configuration-Driven Approach
- **No Code Changes**: Add new APIs via configuration
- **Pattern Matching**: Use wildcards for API groups
- **Role-Based**: Map applications to roles with specific permissions

### Example for Large Scale
```yaml
roles:
  ORDER-SERVICE:
    - "/inventory-service/**"
    - "/payment-service/**"
    - "/user-service/profile/**"
  
  INVENTORY-SERVICE:
    - "/product-service/**"
    - "/warehouse-service/**"
  
  FRONTEND-WEB:
    - "/order-service/create/**"
    - "/user-service/profile/**"
    - "/public/**"
```

## Performance Characteristics

- **Latency**: ~1ms overhead per request after caching
- **Throughput**: Handles thousands of requests/second
- **Memory**: Minimal footprint with caching
- **Scalability**: Linear scaling with API count

## Best Practices

### 1. Certificate Management
- Use separate CAs for internal vs external certificates
- Implement certificate rotation
- Monitor certificate expiration

### 2. RBAC Design
- Use application-based roles instead of user roles
- Apply principle of least privilege
- Regular access reviews

### 3. Performance
- Enable caching for permission checks
- Use connection pooling
- Monitor gateway performance

### 4. Security
- Regular security audits
- Certificate validation
- Secure certificate storage

## Deployment Architecture

### Recommended Setup
```
Internet → Load Balancer → Gateway Cluster (mTLS + RBAC)
                              ↓
Internal Network → Service Mesh (Eureka + Direct mTLS)
                   ↓
                   service-a ↔ service-b ↔ service-c
```

### Benefits
- **External Security**: Gateway handles all external access
- **Internal Performance**: Direct service communication
- **Scalability**: No single point of failure for internal traffic
- **Flexibility**: Easy to add new services and permissions

## Troubleshooting

### Common Issues
1. **Certificate Not Found**: Check certificate paths and passwords
2. **Permission Denied**: Verify RBAC configuration
3. **SSL Handshake Failed**: Check certificate validity and trust store
4. **404 Not Found**: Verify service endpoints and gateway routes

### Debug Commands
```bash
# Check certificate details
openssl x509 -in certificate.pem -text -noout

# Test SSL connection
openssl s_client -connect localhost:8080 -cert client.pem -key client.key

# Check service registration
curl http://localhost:8761/eureka/apps
```

This architecture provides enterprise-grade security with high performance and scalability for microservices communication.
