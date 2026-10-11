package com.tunnelscope.backend.service;

import com.tunnelscope.backend.dto.AnalysisInput;
import com.tunnelscope.backend.dto.AnalysisResponse;
import com.tunnelscope.backend.dto.AnalysisSummaryResponse;
import com.tunnelscope.backend.entity.Analysis;
import com.tunnelscope.backend.entity.AnalysisRecord;
import com.tunnelscope.backend.entity.Dataset;
import com.tunnelscope.backend.repository.AnalysisRecordRepository;
import com.tunnelscope.backend.repository.AnalysisRepository;
import com.tunnelscope.backend.repository.DatasetRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final DatasetRepository datasetRepository;

    private final CsvService csvService;
    private final OnnxModelService onnxModelService;

    public AnalysisService(
            AnalysisRepository analysisRepository,
            AnalysisRecordRepository analysisRecordRepository,
            DatasetRepository datasetRepository,
            CsvService csvService,
            OnnxModelService onnxModelService
    ) {
        this.analysisRepository = analysisRepository;
        this.analysisRecordRepository = analysisRecordRepository;
        this.datasetRepository = datasetRepository;
        this.csvService = csvService;
        this.onnxModelService = onnxModelService;
    }

    // =========================================================
    // ANALYZE DATASET
    // =========================================================

    public Analysis analyzeDataset(
            Long datasetId,
            MultipartFile file
    ) throws Exception {

        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Dataset not found: " + datasetId
                        )
                );

        List<AnalysisInput> inputs =
                csvService.readUploadedCsv(file);

        Analysis analysis = new Analysis();

        analysis.setDatasetId(dataset.getId());
        analysis.setDatasetName(dataset.getName());
        analysis.setStatus("Analyzing");
        analysis.setStartedAt(LocalDateTime.now());
        analysis.setTotalRecords(inputs.size());
        analysis.setFlaggedRecords(0);

        analysis = analysisRepository.save(analysis);

        try {

            double totalRisk = 0.0;
            int flaggedRecords = 0;

            String overallVerdict = "Secure";

            // =====================================================
            // ANALYZE EVERY CSV ROW
            // =====================================================

            for (AnalysisInput input : inputs) {

                // Run all three ONNX models
                AnalysisResponse modelResponse =
                        onnxModelService.predict(input);

                // Preserve CSV sample ID in API response
                modelResponse.setSampleId(
                        input.getSampleId()
                );

                // =================================================
                // RISK
                // =================================================

                double riskScore =
                        normalizeRisk(
                                modelResponse.getPredictedRisk()
                        );

                String riskLevel =
                        calculateRiskLevel(riskScore);

                // =================================================
                // SECURITY
                // =================================================

                String verdict =
                        mapSecurityVerdict(
                                modelResponse.getSecurityVerdict()
                        );

                // =================================================
                // CREATE ANALYSIS RECORD
                // =================================================

                AnalysisRecord record =
                        new AnalysisRecord();

                record.setAnalysisId(
                        analysis.getId()
                );

                // Current dataset does not contain IP addresses.
                record.setSourceIp(null);
                record.setDestinationIp(null);

                // Current dataset represents IPsec traffic.
                record.setProtocol("IPsec");

                record.setSourcePort(null);
                record.setDestinationPort(null);

                // Preserve actual ONNX traffic class.
                record.setPrediction(
                        mapTrafficPrediction(
                                modelResponse.getTrafficType()
                        )
                );

                record.setRiskScore(riskScore);
                record.setRiskLevel(riskLevel);
                record.setVerdict(verdict);

                // Current CSV does not contain packet count
                // or total bytes.
                record.setPackets(null);
                record.setBytes(null);

                // Session duration is available.
                record.setDurationSec(
                        input.getSessionDurationSec()
                );

                // Flags are not present in current dataset.
                record.setFlags(null);

                // Preserve all relevant CSV/model input data.
                record.setRawFeatures(
                        createRawFeatures(
                                input,
                                modelResponse
                        )
                );

                analysisRecordRepository.save(record);

                // =================================================
                // OVERALL STATISTICS
                // =================================================

                totalRisk += riskScore;

                if (isFlagged(riskLevel)) {
                    flaggedRecords++;
                }

                overallVerdict =
                        getHigherRiskVerdict(
                                overallVerdict,
                                verdict
                        );
            }

            // =====================================================
            // FINAL ANALYSIS VALUES
            // =====================================================

            double averageRisk =
                    inputs.isEmpty()
                            ? 0.0
                            : totalRisk / inputs.size();

            String overallRiskLevel =
                    calculateRiskLevel(averageRisk);

            // =====================================================
            // UPDATE ANALYSIS
            // =====================================================

            analysis.setStatus("Completed");

            analysis.setCompletedAt(
                    LocalDateTime.now()
            );

            analysis.setFlaggedRecords(
                    flaggedRecords
            );

            analysis.setRiskScore(
                    round(averageRisk)
            );

            analysis.setRiskLevel(
                    overallRiskLevel
            );

            analysis.setSecurityVerdict(
                    overallVerdict
            );

            analysis.setSummaryExplanation(
                    createSummaryExplanation(
                            inputs.size(),
                            flaggedRecords,
                            averageRisk,
                            overallVerdict
                    )
            );

            return analysisRepository.save(analysis);

        } catch (Exception e) {

            analysis.setStatus("Failed");

            analysis.setCompletedAt(
                    LocalDateTime.now()
            );

            analysis.setSummaryExplanation(
                    "Analysis failed: " + e.getMessage()
            );

            analysisRepository.save(analysis);

            throw e;
        }
    }

    // =========================================================
    // GET ANALYSIS REPORT
    // =========================================================

    public AnalysisSummaryResponse getAnalysisSummary(
            Long analysisId
    ) {

        Analysis analysis =
                analysisRepository.findById(analysisId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Analysis not found: "
                                                + analysisId
                                )
                        );

        List<AnalysisRecord> records =
                analysisRecordRepository
                        .findByAnalysisId(analysisId);

        AnalysisSummaryResponse response =
                new AnalysisSummaryResponse();

        response.setId(
                analysis.getId()
        );

        response.setDatasetId(
                analysis.getDatasetId()
        );

        response.setDatasetName(
                analysis.getDatasetName()
        );

        response.setCompletedAt(
                analysis.getCompletedAt()
        );

        response.setStatus(
                analysis.getStatus()
        );

        response.setVerdict(
                analysis.getSecurityVerdict()
        );

        response.setRiskScore(
                analysis.getRiskScore()
        );

        response.setRiskLevel(
                analysis.getRiskLevel()
        );

        response.setTotalRecords(
                analysis.getTotalRecords()
        );

        response.setFlaggedRecords(
                analysis.getFlaggedRecords()
        );

        response.setSummaryExplanation(
                analysis.getSummaryExplanation()
        );

        // =====================================================
        // TRAFFIC PREDICTION DISTRIBUTION
        // =====================================================

        Map<String, Long> predictionCounts =
                new HashMap<>();

        for (AnalysisRecord record : records) {

            String prediction =
                    record.getPrediction();

            if (prediction == null ||
                    prediction.isBlank()) {

                prediction = "Unknown";
            }

            predictionCounts.merge(
                    prediction,
                    1L,
                    Long::sum
            );
        }

        List<AnalysisSummaryResponse.PredictionSummary>
                predictions =
                new ArrayList<>();

        for (Map.Entry<String, Long> entry :
                predictionCounts.entrySet()) {

            double percentage =
                    records.isEmpty()
                            ? 0.0
                            : ((double) entry.getValue()
                            / records.size()) * 100.0;

            predictions.add(
                    new AnalysisSummaryResponse.PredictionSummary(
                            entry.getKey(),
                            entry.getValue(),
                            round(percentage)
                    )
            );
        }

        response.setPredictions(
                predictions
        );

        // =====================================================
        // SECURITY VERDICT DISTRIBUTION
        // =====================================================

        Map<String, Long> securityCounts =
                new HashMap<>();

        for (AnalysisRecord record : records) {

            String verdict =
                    record.getVerdict();

            if (verdict == null ||
                    verdict.isBlank()) {

                verdict = "Unknown";
            }

            securityCounts.merge(
                    verdict,
                    1L,
                    Long::sum
            );
        }

        List<AnalysisSummaryResponse.SecurityDistribution>
                securityDistribution =
                new ArrayList<>();

        for (Map.Entry<String, Long> entry :
                securityCounts.entrySet()) {

            double percentage =
                    records.isEmpty()
                            ? 0.0
                            : ((double) entry.getValue()
                            / records.size()) * 100.0;

            securityDistribution.add(
                    new AnalysisSummaryResponse.SecurityDistribution(
                            entry.getKey(),
                            entry.getValue(),
                            round(percentage),
                            getDistributionColor(
                                    entry.getKey()
                            )
                    )
            );
        }

        response.setSecurityDistribution(
                securityDistribution
        );

        return response;
    }

    // =========================================================
    // GET ALL ANALYSIS RECORDS
    // =========================================================

    public List<AnalysisRecord> getAnalysisRecords(
            Long analysisId
    ) {

        if (!analysisRepository.existsById(analysisId)) {

            throw new RuntimeException(
                    "Analysis not found: " + analysisId
            );
        }

        return analysisRecordRepository
                .findByAnalysisId(analysisId);
    }

    // =========================================================
    // GET SINGLE ANALYSIS RECORD
    // =========================================================

    public AnalysisRecord getAnalysisRecord(
            Long analysisId,
            Long recordId
    ) {

        if (!analysisRepository.existsById(analysisId)) {

            throw new RuntimeException(
                    "Analysis not found: " + analysisId
            );
        }

        AnalysisRecord record =
                analysisRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Record not found: "
                                                + recordId
                                )
                        );

        if (!record.getAnalysisId().equals(analysisId)) {

            throw new RuntimeException(
                    "Record does not belong to analysis: "
                            + analysisId
            );
        }

        return record;
    }

    // =========================================================
    // RISK NORMALIZATION
    // =========================================================

    private double normalizeRisk(
            double modelRisk
    ) {

        double normalized =
                modelRisk * 10.0;

        return Math.max(
                0.0,
                Math.min(
                        100.0,
                        normalized
                )
        );
    }

    // =========================================================
    // RISK LEVEL
    // =========================================================

    private String calculateRiskLevel(
            double riskScore
    ) {

        if (riskScore <= 20) {
            return "minimal";
        }

        if (riskScore <= 40) {
            return "low";
        }

        if (riskScore <= 60) {
            return "moderate";
        }

        if (riskScore <= 80) {
            return "high";
        }

        return "critical";
    }

    // =========================================================
    // SECURITY VERDICT
    // =========================================================

    private String mapSecurityVerdict(
            String modelVerdict
    ) {

        if (modelVerdict == null ||
                modelVerdict.isBlank()) {

            return "Unknown";
        }

        return switch (
                modelVerdict.trim().toLowerCase()
                ) {

            case "strong" ->
                    "Secure";

            case "medium" ->
                    "Moderate Risk";

            case "weak" ->
                    "High Risk";

            case "critical" ->
                    "Critical";

            default ->
                    modelVerdict;
        };
    }

    // =========================================================
    // TRAFFIC PREDICTION
    // =========================================================

    private String mapTrafficPrediction(
            String trafficType
    ) {

        if (trafficType == null ||
                trafficType.isBlank()) {

            return "Unknown";
        }

        return trafficType.trim();
    }

    // =========================================================
    // FLAGGED RECORD
    // =========================================================

    private boolean isFlagged(
            String riskLevel
    ) {

        return "high".equalsIgnoreCase(riskLevel)
                || "critical".equalsIgnoreCase(riskLevel);
    }

    // =========================================================
    // HIGHER RISK VERDICT
    // =========================================================

    private String getHigherRiskVerdict(
            String current,
            String newVerdict
    ) {

        int currentLevel =
                verdictPriority(current);

        int newLevel =
                verdictPriority(newVerdict);

        return newLevel > currentLevel
                ? newVerdict
                : current;
    }

    private int verdictPriority(
            String verdict
    ) {

        if (verdict == null) {
            return 0;
        }

        return switch (verdict) {

            case "Secure" ->
                    1;

            case "Moderate Risk" ->
                    2;

            case "High Risk" ->
                    3;

            case "Critical" ->
                    4;

            default ->
                    0;
        };
    }

    // =========================================================
    // SUMMARY EXPLANATION
    // =========================================================

    private String createSummaryExplanation(
            int totalRecords,
            int flaggedRecords,
            double averageRisk,
            String verdict
    ) {

        double flaggedPercentage =
                totalRecords == 0
                        ? 0.0
                        : ((double) flaggedRecords
                        / totalRecords) * 100.0;

        return String.format(
                "Analyzed %d records. %d records (%.1f%%) "
                        + "were classified as high or critical risk. "
                        + "The average risk score was %.1f/100 "
                        + "and the overall security verdict was %s.",
                totalRecords,
                flaggedRecords,
                flaggedPercentage,
                averageRisk,
                verdict
        );
    }

    // =========================================================
    // RAW FEATURES
    // =========================================================

    private String createRawFeatures(
            AnalysisInput input,
            AnalysisResponse response
    ) {

        return String.format(
                "{\"sampleId\":\"%s\","
                        + "\"mode\":\"%s\","
                        + "\"ipVersion\":\"%s\","
                        + "\"encryptionAlgo\":\"%s\","
                        + "\"dhGroup\":\"%s\","
                        + "\"pfs\":\"%s\","
                        + "\"trafficType\":\"%s\","
                        + "\"securityVerdict\":\"%s\","
                        + "\"originalRiskScore\":%.4f,"
                        + "\"predictedTraffic\":\"%s\","
                        + "\"trafficConfidence\":%.4f,"
                        + "\"predictedSecurity\":\"%s\","
                        + "\"securityConfidence\":%.4f,"
                        + "\"predictedRisk\":%.4f,"
                        + "\"avgPacketSizeBytes\":%.4f,"
                        + "\"packetRatePerSec\":%.4f,"
                        + "\"sessionDurationSec\":%.4f,"
                        + "\"burstinessIndex\":%.4f,"
                        + "\"ikeHandshakeTimeMs\":%.4f}",
                escapeJson(input.getSampleId()),
                escapeJson(input.getMode()),
                escapeJson(input.getIpVersion()),
                escapeJson(input.getEncryptionAlgo()),
                escapeJson(input.getDhGroup()),
                escapeJson(input.getPfs()),
                escapeJson(input.getTrafficType()),
                escapeJson(input.getSecurityVerdict()),
                input.getOriginalRiskScore(),
                escapeJson(response.getTrafficType()),
                response.getTrafficConfidence(),
                escapeJson(response.getSecurityVerdict()),
                response.getSecurityConfidence(),
                response.getPredictedRisk(),
                input.getAvgPacketSizeBytes(),
                input.getPacketRatePerSec(),
                input.getSessionDurationSec(),
                input.getBurstinessIndex(),
                input.getIkeHandshakeTimeMs()
        );
    }

    // =========================================================
    // DISTRIBUTION COLOR
    // =========================================================

    private String getDistributionColor(
            String verdict
    ) {

        if (verdict == null) {
            return "gray";
        }

        return switch (verdict) {

            case "Secure" ->
                    "green";

            case "Moderate Risk" ->
                    "yellow";

            case "High Risk" ->
                    "orange";

            case "Critical" ->
                    "red";

            default ->
                    "gray";
        };
    }

    // =========================================================
    // ROUND NUMBER
    // =========================================================

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    // =========================================================
    // JSON ESCAPE
    // =========================================================

    private String escapeJson(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}