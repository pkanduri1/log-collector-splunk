package com.example.logcollector.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntelligentSearchService {

    // Mocking ChatClient for now to fix build
    public IntelligentSearchService() {
    }

    public String convertQuestionToSpl(String question) {
        // Mock implementation
        return "search index='main' " + question;
    }

    public String summarizeLogs(List<String> logs) {
        // Mock implementation
        return "Using Mock AI: Logs analysis not available without Spring AI dependency.";
    }
}
