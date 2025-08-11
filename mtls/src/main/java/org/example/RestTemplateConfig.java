package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Configuration
public class RestTemplateConfig {

    @Value("${resttemplate.ssl.key-store}")
    private String keyStorePath;

    @Value("${resttemplate.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${resttemplate.ssl.key-store-type}")
    private String keyStoreType;

    @Value("${resttemplate.ssl.trust-store}")
    private String trustStorePath;

    @Value("${resttemplate.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${resttemplate.ssl.trust-store-type}")
    private String trustStoreType;

    @Bean
    public RestTemplate restTemplate() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        try (InputStream ksStream = getClass().getClassLoader()
                .getResourceAsStream(keyStorePath.replace("classpath:", ""))) {
            keyStore.load(ksStream, keyStorePassword.toCharArray());
        }

        KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        try (InputStream tsStream = getClass().getClassLoader()
                .getResourceAsStream(trustStorePath.replace("classpath:", ""))) {
            trustStore.load(tsStream, trustStorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        
        return new RestTemplate();
    }
}

