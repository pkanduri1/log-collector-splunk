package com.example.logcollector.controller;

import com.example.logcollector.model.LogAnalysisRequest;
import com.example.logcollector.model.LogAnalysisResponse;
import com.example.logcollector.service.IntelligentSearchService;
import com.example.logcollector.service.SplunkService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analyze")
public class LogAnalysisController {

    private final SplunkService splunkService;
    private final IntelligentSearchService intelligentSearchService;

    public LogAnalysisController(SplunkService splunkService, IntelligentSearchService intelligentSearchService) {
        this.splunkService = splunkService;
        this.intelligentSearchService = intelligentSearchService;
    }

    @PostMapping
    public LogAnalysisResponse analyze(@RequestBody LogAnalysisRequest request) {
        // 1. Convert Question to SPL
        String splQuery = intelligentSearchService.convertQuestionToSpl(request.question());

        // 2. Execute Search in Splunk
        List<String> rawLogs = splunkService.executeSearch(splQuery);

        // 3. Summarize Logs
        String summary = intelligentSearchService.summarizeLogs(rawLogs);

        return new LogAnalysisResponse(splQuery, summary);
    }
}
