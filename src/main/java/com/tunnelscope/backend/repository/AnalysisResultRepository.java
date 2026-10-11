package com.tunnelscope.backend.repository;

import com.tunnelscope.backend.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnalysisResultRepository
        extends JpaRepository<AnalysisResult, Long> {

    long countBySecurityVerdict(String securityVerdict);

    @Query("SELECT AVG(a.predictedRisk) FROM AnalysisResult a")
    Double getAverageRisk();

    @Query("SELECT AVG(a.trafficConfidence) FROM AnalysisResult a")
    Double getAverageTrafficConfidence();

    @Query("SELECT AVG(a.securityConfidence) FROM AnalysisResult a")
    Double getAverageSecurityConfidence();
}