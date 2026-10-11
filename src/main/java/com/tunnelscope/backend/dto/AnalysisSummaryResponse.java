package com.tunnelscope.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisSummaryResponse {

    private Long id;
    private Long datasetId;
    private String datasetName;
    private LocalDateTime completedAt;
    private String status;

    private String verdict;
    private Double riskScore;
    private String riskLevel;

    private Integer totalRecords;
    private Integer flaggedRecords;

    private String summaryExplanation;

    private List<PredictionSummary> predictions;
    private List<SecurityDistribution> securityDistribution;

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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
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

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getFlaggedRecords() {
        return flaggedRecords;
    }

    public void setFlaggedRecords(Integer flaggedRecords) {
        this.flaggedRecords = flaggedRecords;
    }

    public String getSummaryExplanation() {
        return summaryExplanation;
    }

    public void setSummaryExplanation(String summaryExplanation) {
        this.summaryExplanation = summaryExplanation;
    }

    public List<PredictionSummary> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<PredictionSummary> predictions) {
        this.predictions = predictions;
    }

    public List<SecurityDistribution> getSecurityDistribution() {
        return securityDistribution;
    }

    public void setSecurityDistribution(
            List<SecurityDistribution> securityDistribution
    ) {
        this.securityDistribution = securityDistribution;
    }

    public static class PredictionSummary {

        private String category;
        private Long count;
        private Double percentage;

        public PredictionSummary() {
        }

        public PredictionSummary(
                String category,
                Long count,
                Double percentage
        ) {
            this.category = category;
            this.count = count;
            this.percentage = percentage;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class SecurityDistribution {

        private String label;
        private Long count;
        private Double percentage;
        private String color;

        public SecurityDistribution() {
        }

        public SecurityDistribution(
                String label,
                Long count,
                Double percentage,
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

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
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