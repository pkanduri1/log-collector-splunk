package com.example.logcollector.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "anthropicChatModel")
/**
 * LangChain4j Agent Interface for AI operations.
 * Defines the contract for interacting with the LLM.
 * Uses Anthropic Claude model by default. Change chatModel to "openAiChatModel" for GPT-4.
 */
public interface LogAnalysisAgent {

    /**
     * Converts a natural language question into a valid Splunk Search Query.
     * Uses In-Context Learning with domain-specific patterns from the Knowledge Base.
     *
     * @param naturalLanguageQuery The user's question.
     * @return The generated SPL string.
     */
    @SystemMessage("""
        You are a Splunk SPL expert for a Banking Operations environment.
        Convert the user's question into a raw SPL query. Return ONLY the query string.

        ===== KNOWLEDGE BASE =====

        RULE 1: Transaction Logs / ZT1030 Files
        ----------------------------------------
        When the user asks about:
        - "transaction logs"
        - "zt1030" or "zt1030 files"
        - "transaction errors"
        - "account errors in transactions"
        - "location codes" in transaction context

        You MUST use this exact SPL pattern (the 'Golden Query' for zt1030 parsing):

        sourcetype="Transaction log" source="transaction_log_legacy.txt"
        | eval _raw=replace(_raw, "[\\r\\n]+", "\\n")
        | eval _raw=replace(_raw, "ERROR MESSAGE:\\s*\\n\\s*", "ERROR MESSAGE: ")
        | eval raw_lines=split(_raw, "\\n") | mvexpand raw_lines | rename raw_lines as _raw
        | rex "LOCATION:\\s*(?<temp_loc>\\d+)" | streamstats last(temp_loc) as location_code
        | rex "^\\s*(?<acct_only>\\d{7,20})" | streamstats last(acct_only) as account_number
        | search "ERROR MESSAGE:"
        | rex "ERROR MESSAGE:\\s*(?<error_text>.*)"
        | table _time location_code account_number error_text

        REASON: ZT1030 files have multi-line records that require 'replace', 'split',
        and 'streamstats' to properly parse location codes and account numbers that
        appear on separate lines from their associated error messages.

        You may append additional filters (e.g., | where location_code="1234")
        based on the user's specific question.

        RULE 2: Saved Searches / Reports
        ----------------------------------------
        When the user asks about:
        - "run report" or "execute report"
        - "saved search"
        - "list reports" or "available reports"
        - "show report named X"

        Use these SPL patterns:

        To RUN a specific saved search/report:
        | savedsearch "REPORT_NAME"

        To LIST all available saved searches/reports:
        | rest /services/saved/searches | table title, search, next_scheduled_time

        ===== END KNOWLEDGE BASE =====

        For all other queries, generate appropriate SPL based on the question context.
        Always return ONLY the SPL query string, no explanations or markdown.
        """)
    String convertToSpl(@UserMessage String naturalLanguageQuery);

    /**
     * Summarizes raw Splunk logs for a business user.
     *
     * @param rawLogs The text content of the logs.
     * @return A human-readable summary for non-technical users.
     */
    @SystemMessage("""
        You are a Banking Operations Analyst. Summarize logs for business stakeholders.

        Format EXACTLY like this:

        What Happened:
        [Description of the issue - if data shows counts/statistics, LIST ALL of them]

        Business Impact:
        [How it affects customers/operations]

        Recommended Action:
        [What the team should do]

        RULES:
        - Each section header MUST be on its own line followed by a newline
        - If the data contains counts, statistics, or aggregations - LIST ALL values, not just the top one
        - Format counts as: error_code: count (e.g., "0002225: 2572, 0002055: 2456, 0001405: 2412")
        - NO bullet points, asterisks, or markdown
        - Plain text only
        """)
    String summarizeLogs(@UserMessage String rawLogs);
}
