package com.example.logcollector.service;

import com.splunk.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
/**
 * Service for interacting with the Splunk SDK.
 * Handles authentication and executing blocking search jobs.
 */
public class SplunkService {

    @Value("${splunk.host}")
    private String host;

    @Value("${splunk.port}")
    private int port;

    @Value("${splunk.username}")
    private String username;

    @Value("${splunk.password}")
    private String password;

    /**
     * Connects to the Splunk Service.
     * Note: Disables SSL validation for Dev purposes.
     * 
     * @return The authenticated Service instance.
     */
    private com.splunk.Service connect() {
        // Ensure SSL bypass is active globally before connecting
        com.example.logcollector.util.SslTrustManagerHelper.trustAllCertificates();

        ServiceArgs serviceArgs = new ServiceArgs();
        serviceArgs.setHost(host);
        serviceArgs.setPort(port);
        serviceArgs.setUsername(username);
        serviceArgs.setPassword(password);
        serviceArgs.setScheme("https"); // Explicitly set HTTPS scheme

        // Explicitly disable certificate validation for SDK
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[] {
                    new javax.net.ssl.X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            // Set the socket factory for the Splunk SDK's HttpService
            HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
            // Assuming SDK uses HttpsURLConnection.setDefaultSSLSocketFactory if not set,
            // but we already did that globally.
            // Let's re-force it here just before connection.
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return com.splunk.Service.connect(serviceArgs);
    }

    /**
     * Executes a blocking search query on Splunk.
     * 
     * @param splQuery The raw SPL query to execute (e.g., "search index=main | head
     *                 5").
     * @return A list of raw log strings found.
     */
    public List<String> executeSearch(String splQuery) {
        com.splunk.Service service = connect();

        // Normalize the query: trim and ensure it starts with "search" command
        String normalizedQuery = splQuery.trim();

        // Remove "search" if it's already there, then add it back to ensure proper
        // formatting
        if (normalizedQuery.toLowerCase().startsWith("search ")) {
            normalizedQuery = normalizedQuery.substring(7).trim(); // Remove "search " prefix
        }

        String finalQuery = "search " + normalizedQuery;

        JobArgs jobArgs = new JobArgs();
        jobArgs.setExecutionMode(JobArgs.ExecutionMode.BLOCKING);

        Job job = service.getJobs().create(finalQuery, jobArgs);

        // Wait is implicit with BLOCKING, but good to be safe if we were async.
        // Reading results
        JobResultsArgs resultsArgs = new JobResultsArgs();
        resultsArgs.setOutputMode(JobResultsArgs.OutputMode.JSON);

        List<String> rawLogs = new ArrayList<>();

        try (InputStream results = job.getResults(resultsArgs)) {
            ResultsReaderJson resultsReader = new ResultsReaderJson(results);

            Event event;
            while ((event = resultsReader.getNextEvent()) != null) {
                // Usually the raw log is in "_raw", but let's grab the whole string
                // representation or specific field
                String raw = event.get("_raw");
                if (raw != null) {
                    rawLogs.add(raw);
                } else {
                    // Fallback to toString if _raw is missing
                    rawLogs.add(event.toString());
                }
            }
            resultsReader.close();
        } catch (IOException e) {
            throw new RuntimeException("Error reading Splunk results", e);
        }

        return rawLogs;
    }
}
