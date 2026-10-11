package com.tunnelscope.backend.dto;

public class AnalysisInput {

    // Original CSV identification / labels
    private String sampleId;
    private String trafficType;
    private String securityVerdict;
    private double originalRiskScore;

    // IPsec configuration
    private String mode;
    private String ipVersion;
    private String encryptionAlgo;
    private String dhGroup;
    private String pfs;

    // Traffic / session features
    private double avgPacketSizeBytes;
    private double packetRatePerSec;
    private double sessionDurationSec;
    private double burstinessIndex;
    private double ikeHandshakeTimeMs;

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

    public String getSecurityVerdict() {
        return securityVerdict;
    }

    public void setSecurityVerdict(String securityVerdict) {
        this.securityVerdict = securityVerdict;
    }

    public double getOriginalRiskScore() {
        return originalRiskScore;
    }

    public void setOriginalRiskScore(double originalRiskScore) {
        this.originalRiskScore = originalRiskScore;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(String ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getEncryptionAlgo() {
        return encryptionAlgo;
    }

    public void setEncryptionAlgo(String encryptionAlgo) {
        this.encryptionAlgo = encryptionAlgo;
    }

    public String getDhGroup() {
        return dhGroup;
    }

    public void setDhGroup(String dhGroup) {
        this.dhGroup = dhGroup;
    }

    public String getPfs() {
        return pfs;
    }

    public void setPfs(String pfs) {
        this.pfs = pfs;
    }

    public double getAvgPacketSizeBytes() {
        return avgPacketSizeBytes;
    }

    public void setAvgPacketSizeBytes(double avgPacketSizeBytes) {
        this.avgPacketSizeBytes = avgPacketSizeBytes;
    }

    public double getPacketRatePerSec() {
        return packetRatePerSec;
    }

    public void setPacketRatePerSec(double packetRatePerSec) {
        this.packetRatePerSec = packetRatePerSec;
    }

    public double getSessionDurationSec() {
        return sessionDurationSec;
    }

    public void setSessionDurationSec(double sessionDurationSec) {
        this.sessionDurationSec = sessionDurationSec;
    }

    public double getBurstinessIndex() {
        return burstinessIndex;
    }

    public void setBurstinessIndex(double burstinessIndex) {
        this.burstinessIndex = burstinessIndex;
    }

    public double getIkeHandshakeTimeMs() {
        return ikeHandshakeTimeMs;
    }

    public void setIkeHandshakeTimeMs(double ikeHandshakeTimeMs) {
        this.ikeHandshakeTimeMs = ikeHandshakeTimeMs;
    }
}