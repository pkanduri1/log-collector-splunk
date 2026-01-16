package com.example.logcollector.service;

import com.example.logcollector.model.LogAnalysisResponse;
import com.example.logcollector.model.QueryHistory;
import com.example.logcollector.repository.QueryHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final QueryHistoryRepository queryHistoryRepository;

    @Value("${app.demo-mode:true}")
    private boolean demoMode;

    public IntelligentSearchService(SplunkService splunkService,
                                    LogAnalysisAgent logAnalysisAgent,
                                    QueryHistoryRepository queryHistoryRepository) {
        this.splunkService = splunkService;
        this.logAnalysisAgent = logAnalysisAgent;
        this.queryHistoryRepository = queryHistoryRepository;
    }

    /**
     * Processes a user's natural language question.
     * 1. Converts question to SPL.
     * 2. Executes search in Splunk (skipped in demo mode).
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

        // Demo mode: Skip Splunk execution and return generated SPL for testing
        if (demoMode) {
            String aiSummary = "Demo Mode: SPL query generated successfully. " +
                    "In production, this query would be executed against Splunk to retrieve and analyze logs.";
            return new LogAnalysisResponse(userQuestion, splQuery, aiSummary);
        }

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

        // Save to query history
        saveQueryHistory(userQuestion, splQuery);

        // Return Response
        return new LogAnalysisResponse(userQuestion, splQuery, aiSummary);
    }

    /**
     * Saves a query to the history database.
     */
    private void saveQueryHistory(String question, String generatedSpl) {
        QueryHistory history = new QueryHistory(question, generatedSpl);
        queryHistoryRepository.save(history);
    }

    /**
     * Retrieves recent query history.
     *
     * @return List of recent queries, newest first.
     */
    public List<QueryHistory> getQueryHistory() {
        return queryHistoryRepository.findTop10ByOrderByTimestampDesc();
    }
}
