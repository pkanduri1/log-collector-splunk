package com.example.logcollector.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface LogAnalysisAgent {

    @SystemMessage("You are a Splunk SPL expert. Convert the user's question into a raw SPL query. Return ONLY the query string.")
    String convertToSpl(@UserMessage String naturalLanguageQuery);

    @SystemMessage("You are a Banking Ops Analyst. Summarize these logs into a JSON object with fields: rootCause, impact, fix.")
    String summarizeLogs(@UserMessage String rawLogs);
}
