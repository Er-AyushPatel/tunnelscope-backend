package com.tunnelscope.backend.dto;

import java.util.List;

public class DashboardResponse {

    private long totalDatasets;
    private long totalRecordsAnalyzed;
    private long highRiskRecordsCount;
    private double highRiskPercentage;

    private String latestVerdict;
    private double latestRiskScore;

    private List<RecentAnalysis> recentAnalyses;

    public long getTotalDatasets() {
        return totalDatasets;
    }

    public void setTotalDatasets(long totalDatasets) {
        this.totalDatasets = totalDatasets;
    }

    public long getTotalRecordsAnalyzed() {
        return totalRecordsAnalyzed;
    }

    public void setTotalRecordsAnalyzed(long totalRecordsAnalyzed) {
        this.totalRecordsAnalyzed = totalRecordsAnalyzed;
    }

    public long getHighRiskRecordsCount() {
        return highRiskRecordsCount;
    }

    public void setHighRiskRecordsCount(long highRiskRecordsCount) {
        this.highRiskRecordsCount = highRiskRecordsCount;
    }

    public double getHighRiskPercentage() {
        return highRiskPercentage;
    }

    public void setHighRiskPercentage(double highRiskPercentage) {
        this.highRiskPercentage = highRiskPercentage;
    }

    public String getLatestVerdict() {
        return latestVerdict;
    }

    public void setLatestVerdict(String latestVerdict) {
        this.latestVerdict = latestVerdict;
    }

    public double getLatestRiskScore() {
        return latestRiskScore;
    }

    public void setLatestRiskScore(double latestRiskScore) {
        this.latestRiskScore = latestRiskScore;
    }

    public List<RecentAnalysis> getRecentAnalyses() {
        return recentAnalyses;
    }

    public void setRecentAnalyses(List<RecentAnalysis> recentAnalyses) {
        this.recentAnalyses = recentAnalyses;
    }

    public static class RecentAnalysis {

        private Long id;
        private Long datasetId;
        private String datasetName;
        private String completedAt;
        private String status;
        private String verdict;
        private double riskScore;
        private String riskLevel;
        private int totalRecords;
        private int flaggedRecords;

        private List<SecurityDistribution> securityDistribution;

        public RecentAnalysis() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getDatasetId() {
            return datasetId;
        }

        public void setDatasetId(Long datasetId) {
            this.datasetId = datasetId;
        }

        public String getDatasetName() {
            return datasetName;
        }

        public void setDatasetName(String datasetName) {
            this.datasetName = datasetName;
        }

        public String getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(String completedAt) {
            this.completedAt = completedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getVerdict() {
            return verdict;
        }

        public void setVerdict(String verdict) {
            this.verdict = verdict;
        }

        public double getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(double riskScore) {
            this.riskScore = riskScore;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getFlaggedRecords() {
            return flaggedRecords;
        }

        public void setFlaggedRecords(int flaggedRecords) {
            this.flaggedRecords = flaggedRecords;
        }

        public List<SecurityDistribution> getSecurityDistribution() {
            return securityDistribution;
        }

        public void setSecurityDistribution(
                List<SecurityDistribution> securityDistribution
        ) {
            this.securityDistribution = securityDistribution;
        }
    }

    public static class SecurityDistribution {

        private String label;
        private long count;
        private double percentage;
        private String color;

        public SecurityDistribution() {
        }

        public SecurityDistribution(
                String label,
                long count,
                double percentage,
                String color
        ) {
            this.label = label;
            this.count = count;
            this.percentage = percentage;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}