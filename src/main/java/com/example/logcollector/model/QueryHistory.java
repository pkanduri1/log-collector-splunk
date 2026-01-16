package com.example.logcollector.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a query history entry.
 * Stores user questions and generated SPL queries for future reference.
 */
@Entity
@Table(name = "query_history")
public class QueryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(length = 4000)
    private String generatedSpl;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public QueryHistory() {
        this.timestamp = LocalDateTime.now();
    }

    public QueryHistory(String question, String generatedSpl) {
        this.question = question;
        this.generatedSpl = generatedSpl;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getGeneratedSpl() {
        return generatedSpl;
    }

    public void setGeneratedSpl(String generatedSpl) {
        this.generatedSpl = generatedSpl;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
