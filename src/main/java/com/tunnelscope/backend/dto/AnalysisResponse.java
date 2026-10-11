package com.tunnelscope.backend.dto;

public class AnalysisResponse {

    private String sampleId;

    private String trafficType;
    private double trafficConfidence;

    private String securityVerdict;
    private double securityConfidence;

    private double predictedRisk;

    public AnalysisResponse() {
    }

    public AnalysisResponse(
            String trafficType,
            double trafficConfidence,
            String securityVerdict,
            double securityConfidence,
            double predictedRisk
    ) {
        this.trafficType = trafficType;
        this.trafficConfidence = trafficConfidence;
        this.securityVerdict = securityVerdict;
        this.securityConfidence = securityConfidence;
        this.predictedRisk = predictedRisk;
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

    public double getTrafficConfidence() {
        return trafficConfidence;
    }

    public void setTrafficConfidence(double trafficConfidence) {
        this.trafficConfidence = trafficConfidence;
    }

    public String getSecurityVerdict() {
        return securityVerdict;
    }

    public void setSecurityVerdict(String securityVerdict) {
        this.securityVerdict = securityVerdict;
    }

    public double getSecurityConfidence() {
        return securityConfidence;
    }

    public void setSecurityConfidence(double securityConfidence) {
        this.securityConfidence = securityConfidence;
    }

    public double getPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(double predictedRisk) {
        this.predictedRisk = predictedRisk;
    }
}