package com.example.logcollector.model;

/**
 * DTO for the Log Analysis Request.
 * Contains the natural language question from the user.
 * 
 * @param question The user's question about the logs.
 */
public record LogAnalysisRequest(String question) {
}
