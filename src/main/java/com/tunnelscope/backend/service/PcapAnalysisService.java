
package com.tunnelscope.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunnelscope.backend.dto.AnalysisInput;
import com.tunnelscope.backend.dto.AnalysisResponse;
import com.tunnelscope.backend.entity.Analysis;
import com.tunnelscope.backend.entity.AnalysisRecord;
import com.tunnelscope.backend.repository.AnalysisRecordRepository;
import com.tunnelscope.backend.repository.AnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PcapAnalysisService {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private final TsharkPcapParser parser;
    private final OnnxModelService onnxModelService;
    private final AnalysisRepository analysisRepository;
    private final AnalysisRecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PcapAnalysisService(
            TsharkPcapParser parser,
            OnnxModelService onnxModelService,
            AnalysisRepository analysisRepository,
            AnalysisRecordRepository recordRepository) {

        this.parser = parser;
        this.onnxModelService = onnxModelService;
        this.analysisRepository = analysisRepository;
        this.recordRepository = recordRepository;
    }

    public Analysis analyze(MultipartFile file) throws Exception {
        validate(file);

        Path tempFile = Files.createTempFile("tunnelscope-", ".pcap");
        Analysis analysis = new Analysis();

        try {
            file.transferTo(tempFile);

            analysis.setDatasetId(null);
            analysis.setDatasetName(file.getOriginalFilename());
            analysis.setStatus("Analyzing");
            analysis.setStartedAt(LocalDateTime.now());
            analysis.setTotalRecords(0);
            analysis.setFlaggedRecords(0);

            analysisRepository.save(analysis);

            List<TsharkPcapParser.Session> sessions = parser.parse(tempFile);

            if (sessions.isEmpty()) {
                throw new IOException(
                        "No sessions were extracted from the PCAP file.");
            }

            double totalRisk = 0.0;
            int flagged = 0;
            int secure = 0;
            int moderate = 0;
            int high = 0;
            int critical = 0;

            for (int i = 0; i < sessions.size(); i++) {
                TsharkPcapParser.Session session = sessions.get(i);

                AnalysisInput input = toModelInput(session, i);
                AnalysisResponse prediction = onnxModelService.predict(input);

                double risk = normalizeRisk(prediction.getPredictedRisk());
                String riskLevel = riskLevel(risk);
                String verdict = normalizeVerdict(
                        prediction.getSecurityVerdict());

                AnalysisRecord record = new AnalysisRecord();

                record.setAnalysisId(analysis.getId());
                record.setSampleId("PCAP-" + (i + 1));
                record.setSourceIp(session.sourceIp());
                record.setDestinationIp(session.destinationIp());
                record.setProtocol(session.protocol());
                record.setSourcePort(session.sourcePort());
                record.setDestinationPort(session.destinationPort());
                record.setPrediction(prediction.getTrafficType());
                record.setRiskScore(risk);
                record.setRiskLevel(riskLevel);
                record.setVerdict(verdict);
                record.setPackets(session.packets());
                record.setBytes(session.bytes());
                record.setDurationSec(session.durationSec());

                record.setFlags(
                        "Source: PCAP; encryption metadata may be unavailable");

                record.setRawFeatures(
                        objectMapper.writeValueAsString(
                                buildRawFeatures(session, prediction, risk)));

                recordRepository.save(record);

                totalRisk += risk;

                if ("High".equals(riskLevel)
                        || "Critical".equals(riskLevel)) {
                    flagged++;
                }

                switch (verdict) {
                    case "Secure" -> secure++;
                    case "Moderate Risk" -> moderate++;
                    case "High Risk" -> high++;
                    case "Critical" -> critical++;
                    default -> {
                        // Unknown or unmapped verdict.
                    }
                }
            }

            double averageRisk = totalRisk / sessions.size();

            analysis.setTotalRecords(sessions.size());
            analysis.setFlaggedRecords(flagged);
            analysis.setRiskScore(round2(averageRisk));
            analysis.setRiskLevel(riskLevel(averageRisk));

            analysis.setSecurityVerdict(
                    overallVerdict(secure, moderate, high, critical));

            analysis.setSummaryExplanation(
                    "PCAP analysis completed. Extracted "
                            + sessions.size()
                            + " sessions using TShark. Risk scores and "
                            + "classifications are model estimates based on "
                            + "available packet metadata; missing IKE "
                            + "negotiation details were not inferred.");

            analysis.setStatus("Completed");
            analysis.setCompletedAt(LocalDateTime.now());

            return analysisRepository.save(analysis);

        } catch (Exception ex) {
            analysis.setStatus("Failed");
            analysis.setCompletedAt(LocalDateTime.now());
            analysis.setSummaryExplanation(
                    "PCAP analysis failed: " + safeMessage(ex));

            analysisRepository.save(analysis);
            throw ex;

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private AnalysisInput toModelInput(
            TsharkPcapParser.Session session,
            int index) {

        AnalysisInput input = new AnalysisInput();

        input.setSampleId("PCAP-" + (index + 1));
        input.setTrafficType(session.protocol());
        input.setSecurityVerdict("Unknown");
        input.setOriginalRiskScore(0.0);

        // These values must not be presented as observed
        // if the capture does not contain IKE negotiation data.
        input.setMode("Unknown");

        input.setIpVersion(
                session.sourceIp().contains(":") ? "IPv6" : "IPv4");

        input.setEncryptionAlgo("Unknown");
        input.setDhGroup("Unknown");
        input.setPfs("Unknown");

        input.setAvgPacketSizeBytes(session.avgPacketSizeBytes());
        input.setPacketRatePerSec(session.packetRatePerSec());
        input.setSessionDurationSec(session.durationSec());
        input.setBurstinessIndex(session.burstinessIndex());
        input.setIkeHandshakeTimeMs(session.ikeHandshakeTimeMs());

        return input;
    }

    private Map<String, Object> buildRawFeatures(
            TsharkPcapParser.Session session,
            AnalysisResponse prediction,
            double normalizedRisk) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("source", "PCAP");
        data.put("sourceIp", session.sourceIp());
        data.put("destinationIp", session.destinationIp());
        data.put("protocol", session.protocol());
        data.put("packets", session.packets());
        data.put("bytes", session.bytes());
        data.put("durationSec", session.durationSec());
        data.put("avgPacketSizeBytes", session.avgPacketSizeBytes());
        data.put("packetRatePerSec", session.packetRatePerSec());
        data.put("burstinessIndex", session.burstinessIndex());
        data.put("ikeHandshakeTimeMs", session.ikeHandshakeTimeMs());
        data.put("encryptionMetadata", "Unknown unless observed in IKE");
        data.put("trafficPrediction", prediction.getTrafficType());
        data.put("securityVerdict", prediction.getSecurityVerdict());
        data.put("riskScore", normalizedRisk);

        return data;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a PCAP file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "PCAP file exceeds the 50 MB upload limit.");
        }

        String name = file.getOriginalFilename();

        if (name == null
                || !(name.toLowerCase().endsWith(".pcap")
                || name.toLowerCase().endsWith(".pcapng"))) {

            throw new IllegalArgumentException(
                    "Only .pcap and .pcapng files are supported.");
        }
    }

    private double normalizeRisk(Double value) {
        if (value == null || !Double.isFinite(value)) {
            return 0.0;
        }

        // Matches the existing CSV AnalysisService convention.
        return round2(Math.max(0.0, Math.min(100.0, value * 10.0)));
    }

    private String riskLevel(double risk) {
        if (risk <= 20) return "Minimal";
        if (risk <= 40) return "Low";
        if (risk <= 60) return "Moderate";
        if (risk <= 80) return "High";
        return "Critical";
    }

    private String normalizeVerdict(String verdict) {
        if (verdict == null) {
            return "Unknown";
        }

        return switch (verdict.trim().toLowerCase()) {
            case "strong", "secure" -> "Secure";
            case "medium", "moderate", "moderate risk" -> "Moderate Risk";
            case "weak", "high risk" -> "High Risk";
            case "critical" -> "Critical";
            default -> verdict;
        };
    }

    private String overallVerdict(
            int secure,
            int moderate,
            int high,
            int critical) {

        if (critical > 0) return "Critical";
        if (high > 0) return "High Risk";
        if (moderate > 0) return "Moderate Risk";
        if (secure > 0) return "Secure";

        return "Unknown";
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();

        return message == null
                ? ex.getClass().getSimpleName()
                : message;
    }
}
