package com.example.logcollector.controller;

import com.example.logcollector.model.LogAnalysisRequest;
import com.example.logcollector.model.LogAnalysisResponse;
import com.example.logcollector.service.IntelligentSearchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
/**
 * REST Controller for Log Analysis operations.
 * Exposes endpoints to analyze logs using Intelligent Search.
 */
public class LogAnalysisController {

    private final IntelligentSearchService intelligentSearchService;

    public LogAnalysisController(IntelligentSearchService intelligentSearchService) {
        this.intelligentSearchService = intelligentSearchService;
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
}
