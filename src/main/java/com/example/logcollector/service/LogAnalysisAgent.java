package com.example.logcollector.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
/**
 * LangChain4j Agent Interface for AI operations.
 * Defines the contract for interacting with the LLM.
 */
public interface LogAnalysisAgent {

    /**
     * Converts a natural language question into a valid Splunk Search Query.
     * 
     * @param naturalLanguageQuery The user's question.
     * @return The generated SPL string.
     */
    @SystemMessage("You are a Splunk SPL expert. Convert the user's question into a raw SPL query. Return ONLY the query string.")
    String convertToSpl(@UserMessage String naturalLanguageQuery);

    /**
     * Summarizes raw Splunk logs for a business user.
     * 
     * @param rawLogs The text content of the logs.
     * @return A structured summary (root cause, impact, fix).
     */
    @SystemMessage("You are a Banking Ops Analyst. Summarize these logs into a JSON object with fields: rootCause, impact, fix.")
    String summarizeLogs(@UserMessage String rawLogs);
}
