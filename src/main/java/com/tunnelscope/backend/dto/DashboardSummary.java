package com.tunnelscope.backend.dto;

public class DashboardSummary {

    private long totalAnalyses;
    private long strongCount;
    private long mediumCount;
    private long weakCount;

    private Double averageRisk;
    private Double averageTrafficConfidence;
    private Double averageSecurityConfidence;

    public long getTotalAnalyses() {
        return totalAnalyses;
    }

    public void setTotalAnalyses(long totalAnalyses) {
        this.totalAnalyses = totalAnalyses;
    }

    public long getStrongCount() {
        return strongCount;
    }

    public void setStrongCount(long strongCount) {
        this.strongCount = strongCount;
    }

    public long getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(long mediumCount) {
        this.mediumCount = mediumCount;
    }

    public long getWeakCount() {
        return weakCount;
    }

    public void setWeakCount(long weakCount) {
        this.weakCount = weakCount;
    }

    public Double getAverageRisk() {
        return averageRisk;
    }

    public void setAverageRisk(Double averageRisk) {
        this.averageRisk = averageRisk;
    }

    public Double getAverageTrafficConfidence() {
        return averageTrafficConfidence;
    }

    public void setAverageTrafficConfidence(Double averageTrafficConfidence) {
        this.averageTrafficConfidence = averageTrafficConfidence;
    }

    public Double getAverageSecurityConfidence() {
        return averageSecurityConfidence;
    }

    public void setAverageSecurityConfidence(Double averageSecurityConfidence) {
        this.averageSecurityConfidence = averageSecurityConfidence;
    }
}