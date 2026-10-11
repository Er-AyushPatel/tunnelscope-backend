package com.tunnelscope.backend.repository;

import com.tunnelscope.backend.entity.AnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRecordRepository
        extends JpaRepository<AnalysisRecord, Long> {

    List<AnalysisRecord> findByAnalysisId(Long analysisId);

    long countByAnalysisId(Long analysisId);

    long countByAnalysisIdAndRiskLevel(
            Long analysisId,
            String riskLevel
    );
}