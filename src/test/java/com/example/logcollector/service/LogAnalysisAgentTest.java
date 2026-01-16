package com.example.logcollector.service;

import dev.langchain4j.service.SystemMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests to verify the In-Context Learning configuration
 * in LogAnalysisAgent's @SystemMessage annotations.
 */
class LogAnalysisAgentTest {

    /**
     * Helper method to extract the system message string from the annotation.
     * The @SystemMessage annotation returns String[], so we join them.
     */
    private String getSystemMessage(SystemMessage annotation) {
        return String.join("\n", annotation.value());
    }

    @Test
    @DisplayName("convertToSpl method should have @SystemMessage with Knowledge Base")
    void convertToSpl_shouldHaveKnowledgeBaseInSystemMessage() throws NoSuchMethodException {
        Method method = LogAnalysisAgent.class.getMethod("convertToSpl", String.class);
        SystemMessage annotation = method.getAnnotation(SystemMessage.class);

        assertNotNull(annotation, "@SystemMessage annotation should be present");

        String systemMessage = getSystemMessage(annotation);

        // Verify Knowledge Base section exists
        assertTrue(systemMessage.contains("KNOWLEDGE BASE"),
                "System message should contain KNOWLEDGE BASE section");

        // Verify zt1030/transaction logs rule is present
        assertTrue(systemMessage.contains("Transaction Logs / ZT1030"),
                "System message should contain rule for Transaction Logs / ZT1030");

        // Verify trigger keywords are documented
        assertTrue(systemMessage.contains("transaction logs"),
                "System message should mention 'transaction logs' as trigger");
        assertTrue(systemMessage.contains("zt1030"),
                "System message should mention 'zt1030' as trigger");

        // Verify Golden Query pattern components are present
        assertTrue(systemMessage.contains("index=default_recovery"),
                "System message should contain the correct index");
        assertTrue(systemMessage.contains("transaction_logs.txt"),
                "System message should contain the source pattern");
        assertTrue(systemMessage.contains("streamstats"),
                "System message should contain 'streamstats' command");
        assertTrue(systemMessage.contains("mvexpand"),
                "System message should contain 'mvexpand' command");
        assertTrue(systemMessage.contains("location_code"),
                "System message should extract location_code field");
        assertTrue(systemMessage.contains("account_number"),
                "System message should extract account_number field");
        assertTrue(systemMessage.contains("error_text"),
                "System message should extract error_text field");
    }

    @Test
    @DisplayName("convertToSpl should instruct LLM to return only SPL query")
    void convertToSpl_shouldInstructToReturnOnlySpl() throws NoSuchMethodException {
        Method method = LogAnalysisAgent.class.getMethod("convertToSpl", String.class);
        SystemMessage annotation = method.getAnnotation(SystemMessage.class);

        String systemMessage = getSystemMessage(annotation);

        assertTrue(systemMessage.contains("Return ONLY the query string") ||
                        systemMessage.contains("ONLY the SPL query"),
                "System message should instruct to return only the SPL query");
    }

    @Test
    @DisplayName("summarizeLogs method should remain unchanged with Banking Ops Analyst role")
    void summarizeLogs_shouldHaveBankingOpsAnalystRole() throws NoSuchMethodException {
        Method method = LogAnalysisAgent.class.getMethod("summarizeLogs", String.class);
        SystemMessage annotation = method.getAnnotation(SystemMessage.class);

        assertNotNull(annotation, "@SystemMessage annotation should be present");

        String systemMessage = getSystemMessage(annotation);

        assertTrue(systemMessage.contains("Banking Ops Analyst"),
                "System message should have Banking Ops Analyst role");
        assertTrue(systemMessage.contains("rootCause"),
                "System message should mention rootCause field");
        assertTrue(systemMessage.contains("impact"),
                "System message should mention impact field");
        assertTrue(systemMessage.contains("fix"),
                "System message should mention fix field");
    }

    @Test
    @DisplayName("Golden Query should have correct regex patterns for multi-line parsing")
    void goldenQuery_shouldHaveCorrectRegexPatterns() throws NoSuchMethodException {
        Method method = LogAnalysisAgent.class.getMethod("convertToSpl", String.class);
        SystemMessage annotation = method.getAnnotation(SystemMessage.class);

        String systemMessage = getSystemMessage(annotation);

        // Verify replace patterns for line normalization
        assertTrue(systemMessage.contains("replace(_raw"),
                "Golden Query should use replace for line normalization");

        // Verify split pattern for multi-line handling
        assertTrue(systemMessage.contains("split(_raw"),
                "Golden Query should use split for multi-line records");

        // Verify streamstats for carrying forward values
        assertTrue(systemMessage.contains("streamstats last(temp_loc)"),
                "Golden Query should use streamstats to carry forward location");
        assertTrue(systemMessage.contains("streamstats last(acct_only)"),
                "Golden Query should use streamstats to carry forward account");

        // Verify rex patterns for field extraction
        assertTrue(systemMessage.contains("LOCATION:"),
                "Golden Query should extract LOCATION field");
        assertTrue(systemMessage.contains("ERROR MESSAGE:"),
                "Golden Query should search for ERROR MESSAGE");
    }
}
