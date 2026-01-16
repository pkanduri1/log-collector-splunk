package com.example.logcollector.repository;

import com.example.logcollector.model.QueryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for QueryHistory entity.
 * Provides database access for query history records.
 */
@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {

    /**
     * Find all query history entries ordered by timestamp descending (newest first).
     */
    List<QueryHistory> findAllByOrderByTimestampDesc();

    /**
     * Find top N most recent queries.
     */
    List<QueryHistory> findTop10ByOrderByTimestampDesc();
}
