package com.example.logcollector.util;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Helper class to bypass SSL certificate verification.
 * WARNING: This should ONLY be used in development environments or when
 * specific proxy issues prevent standard checks.
 * DO NOT use this in production with real data.
 */
public class SslTrustManagerHelper {

    /**
     * Trusts all certificates globally for HttpsURLConnection.
     */
    public static void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            // Set default SSLContext for the entire JVM
            SSLContext.setDefault(sc);

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            // Disable hostname verification for JDK HttpClient as well
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

            System.out.println("WARNING: SSL Certificate verification disabled globally via SslTrustManagerHelper.");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.err.println("Failed to disable SSL verification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configures an OkHttpClient Builder to trust all certificates.
     * Use this for libraries that rely on OkHttp (like LangChain4j/OpenAI).
     * 
     * @param builder The OkHttpClient.Builder to configure (type depends on library
     *                version, returning generic Object or requiring specific dep).
     *                Since we might not want to hardcode the dependency here if not
     *                needed,
     *                we can simply return the SSLContext and TrustManager if the
     *                caller needs to build it.
     */
    public static X509TrustManager getTrustManager() {
        return new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        };
    }

    public static SSLContext getSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { getTrustManager() }, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL Context", e);
        }
    }

    /**
     * Creates an OkHttpClient with SSL verification disabled.
     * This is specifically for LangChain4j models that use OkHttp internally.
     * 
     * @return OkHttpClient configured to trust all certificates
     */
    public static okhttp3.OkHttpClient createUnsafeOkHttpClient() {
        try {
            X509TrustManager trustManager = getTrustManager();
            SSLContext sslContext = getSslContext();

            return new okhttp3.OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsafe OkHttpClient", e);
        }
    }
}
