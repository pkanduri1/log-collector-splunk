package com.example.logcollector.controller;

import com.example.logcollector.model.LogAnalysisRequest;
import com.example.logcollector.model.LogAnalysisResponse;
import com.example.logcollector.model.QueryHistory;
import com.example.logcollector.service.IntelligentSearchService;
import com.example.logcollector.service.SplunkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
/**
 * REST Controller for Log Analysis operations.
 * Exposes endpoints to analyze logs using Intelligent Search.
 */
public class LogAnalysisController {

    private final IntelligentSearchService intelligentSearchService;
    private final SplunkService splunkService;

    public LogAnalysisController(IntelligentSearchService intelligentSearchService, SplunkService splunkService) {
        this.intelligentSearchService = intelligentSearchService;
        this.splunkService = splunkService;
    }

    /**
     * Analyzes a natural language question about logs.
     *
     * @param request The request containing the user's question.
     * @return LogAnalysisResponse containing the generated SPL and AI summary.
     */
    @PostMapping("/analyze")
    public LogAnalysisResponse analyze(@RequestBody LogAnalysisRequest request) {
        return intelligentSearchService.processQuery(request.question());
    }

    /**
     * Retrieves recent query history.
     *
     * @return List of recent queries with timestamps.
     */
    @GetMapping("/history")
    public List<QueryHistory> getHistory() {
        return intelligentSearchService.getQueryHistory();
    }

    /**
     * Lists all available Splunk saved searches (reports).
     *
     * @return List of report names.
     */
    @GetMapping("/reports")
    public List<String> getReports() {
        return splunkService.listSavedSearches();
    }
}
