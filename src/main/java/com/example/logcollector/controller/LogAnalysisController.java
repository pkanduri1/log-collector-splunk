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
public class LogAnalysisController {

    private final IntelligentSearchService intelligentSearchService;

    public LogAnalysisController(IntelligentSearchService intelligentSearchService) {
        this.intelligentSearchService = intelligentSearchService;
    }

    @PostMapping("/analyze")
    public LogAnalysisResponse analyze(@RequestBody LogAnalysisRequest request) {
        return intelligentSearchService.processQuery(request.question());
    }
}
