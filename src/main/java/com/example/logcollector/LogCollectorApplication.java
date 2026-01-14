package com.example.logcollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Main entry point for the Log Collector Application.
 * Bootstraps the Spring Boot application and initializes context.
 */
public class LogCollectorApplication {

    public static void main(String[] args) {
        // Set JVM-level properties to disable SSL verification
        System.setProperty("javax.net.ssl.trustAll", "true");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        System.setProperty("com.sun.net.ssl.checkRevocation", "false");

        // Disable SSL verification for development environments facing proxy issues
        com.example.logcollector.util.SslTrustManagerHelper.trustAllCertificates();
        SpringApplication.run(LogCollectorApplication.class, args);
    }

}
