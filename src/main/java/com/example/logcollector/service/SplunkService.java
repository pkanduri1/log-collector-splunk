package com.example.logcollector.service;

import com.splunk.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SplunkService {

    @Value("${splunk.host}")
    private String host;

    @Value("${splunk.port}")
    private int port;

    @Value("${splunk.username}")
    private String username;

    @Value("${splunk.password}")
    private String password;

    private com.splunk.Service connect() {
        ServiceArgs serviceArgs = new ServiceArgs();
        serviceArgs.setHost(host);
        serviceArgs.setPort(port);
        serviceArgs.setUsername(username);
        serviceArgs.setPassword(password);

        // Disable certificate validation for POC/Dev
        HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
        // Note: For real production, you might need proper SSL context or check
        // certificates.

        return com.splunk.Service.connect(serviceArgs);
    }

    public List<String> executeSearch(String splQuery) {
        com.splunk.Service service = connect();

        // Ensure query starts with search if not present, though usually full SPL
        // implies it
        String finalQuery = splQuery.trim().toLowerCase().startsWith("search") ? splQuery : "search " + splQuery;

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
