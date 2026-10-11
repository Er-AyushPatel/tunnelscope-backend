package com.tunnelscope.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sampleId;

    private String trafficType;
    private Double trafficConfidence;

    private String securityVerdict;
    private Double securityConfidence;

    private Double predictedRisk;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(String trafficType) {
        this.trafficType = trafficType;
    }

    public Double getTrafficConfidence() {
        return trafficConfidence;
    }

    public void setTrafficConfidence(Double trafficConfidence) {
        this.trafficConfidence = trafficConfidence;
    }

    public String getSecurityVerdict() {
        return securityVerdict;
    }

    public void setSecurityVerdict(String securityVerdict) {
        this.securityVerdict = securityVerdict;
    }

    public Double getSecurityConfidence() {
        return securityConfidence;
    }

    public void setSecurityConfidence(Double securityConfidence) {
        this.securityConfidence = securityConfidence;
    }

    public Double getPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(Double predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}