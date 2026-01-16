package com.example.logcollector.service;

import com.splunk.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
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
        ServiceArgs serviceArgs = new ServiceArgs();
        serviceArgs.setHost(host);
        serviceArgs.setPort(port);
        serviceArgs.setUsername(username);
        serviceArgs.setPassword(password);
        serviceArgs.setScheme("https");

        // Set SSL protocol and disable certificate validation for Splunk SDK
        HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpService.setSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure SSL for Splunk", e);
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

        String trimmedQuery = splQuery.trim();
        String lowerQuery = trimmedQuery.toLowerCase();

        // Don't prepend "search" for commands that must be first (savedsearch, rest, etc.)
        String finalQuery;
        if (lowerQuery.startsWith("search") ||
            lowerQuery.startsWith("| savedsearch") ||
            lowerQuery.startsWith("|savedsearch") ||
            lowerQuery.startsWith("| rest") ||
            lowerQuery.startsWith("|rest")) {
            finalQuery = trimmedQuery;
        } else {
            finalQuery = "search " + trimmedQuery;
        }

        JobArgs jobArgs = new JobArgs();
        jobArgs.setExecutionMode(JobArgs.ExecutionMode.BLOCKING);

        Job job = service.getJobs().create(finalQuery, jobArgs);

        // Wait is implicit with BLOCKING, but good to be safe if we were async.
        // Reading results
        JobResultsArgs resultsArgs = new JobResultsArgs();
        resultsArgs.setOutputMode(JobResultsArgs.OutputMode.JSON);
        resultsArgs.setCount(0); // 0 means return all results (no pagination limit)

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

    /**
     * Lists all saved searches (reports) from Splunk.
     *
     * @return A list of report names.
     */
    public List<String> listSavedSearches() {
        com.splunk.Service service = connect();
        List<String> reports = new ArrayList<>();

        SavedSearchCollection savedSearches = service.getSavedSearches();
        for (SavedSearch search : savedSearches.values()) {
            reports.add(search.getName());
        }

        return reports;
    }
}
