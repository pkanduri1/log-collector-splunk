package com.example.logcollector.service;

import com.example.logcollector.model.LogAnalysisResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
/**
 * Service orchestrating the Intelligent Search workflow.
 * Combines LangChain4j Agents and Splunk Service to answer user questions.
 */
public class IntelligentSearchService {

    private final SplunkService splunkService;
    private final LogAnalysisAgent logAnalysisAgent;

    public IntelligentSearchService(SplunkService splunkService, LogAnalysisAgent logAnalysisAgent) {
        this.splunkService = splunkService;
        this.logAnalysisAgent = logAnalysisAgent;
    }

    /**
     * Processes a user's natural language question.
     * 1. Converts question to SPL.
     * 2. Executes search in Splunk.
     * 3. Summarizes results using AI.
     * 
     * @param userQuestion The natural language question (e.g., "Why did batch
     *                     fail?").
     * @return The complete analysis response.
     */
    public LogAnalysisResponse processQuery(String userQuestion) {
        // Step A: Text-to-SPL via LangChain4j Agent
        String splQuery = logAnalysisAgent.convertToSpl(userQuestion);

        // Sanitize output just in case
        splQuery = splQuery.replace("```splunk", "").replace("```", "").trim();

        // Step B: Search Splunk
        List<String> rawLogs = splunkService.executeSearch(splQuery);

        // Step C: Summarize
        String aiSummary;
        if (rawLogs.isEmpty()) {
            aiSummary = "No logs were found for the generated SPL: " + splQuery;
        } else {
            String logsContent = rawLogs.stream().limit(50).collect(Collectors.joining("\n"));
            // Step D: AI Summary via LangChain4j Agent
            aiSummary = logAnalysisAgent.summarizeLogs(logsContent);
        }

        // Return Response
        return new LogAnalysisResponse(userQuestion, splQuery, aiSummary);
    }
}
