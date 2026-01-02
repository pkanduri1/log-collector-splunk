package com.example.logcollector.model;

public record LogAnalysisResponse(
        String originalQuestion,
        String generatedSpl,
        String aiSummary) {
}
