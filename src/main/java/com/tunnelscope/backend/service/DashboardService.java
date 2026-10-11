package com.tunnelscope.backend.service;

import com.tunnelscope.backend.dto.DashboardResponse;
import com.tunnelscope.backend.entity.Analysis;
import com.tunnelscope.backend.entity.AnalysisRecord;
import com.tunnelscope.backend.repository.AnalysisRecordRepository;
import com.tunnelscope.backend.repository.AnalysisRepository;
import com.tunnelscope.backend.repository.DatasetRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DatasetRepository datasetRepository;
    private final AnalysisRepository analysisRepository;
    private final AnalysisRecordRepository analysisRecordRepository;

    public DashboardService(
            DatasetRepository datasetRepository,
            AnalysisRepository analysisRepository,
            AnalysisRecordRepository analysisRecordRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.analysisRepository = analysisRepository;
        this.analysisRecordRepository = analysisRecordRepository;
    }

    public DashboardResponse getDashboard() {

        DashboardResponse response =
                new DashboardResponse();

        // ============================================
        // TOTAL DATASETS
        // ============================================

        long totalDatasets =
                datasetRepository.count();

        response.setTotalDatasets(totalDatasets);

        // ============================================
        // ALL ANALYSES
        // ============================================

        List<Analysis> analyses =
                analysisRepository.findAll();

        // ============================================
        // TOTAL RECORDS ANALYZED
        // ============================================

        long totalRecordsAnalyzed = 0;

        for (Analysis analysis : analyses) {

            if (analysis.getTotalRecords() != null) {

                totalRecordsAnalyzed +=
                        analysis.getTotalRecords();
            }
        }

        response.setTotalRecordsAnalyzed(
                totalRecordsAnalyzed
        );

        // ============================================
        // HIGH RISK RECORDS
        // ============================================

        long highRiskRecords = 0;

        for (Analysis analysis : analyses) {

            if (analysis.getId() == null) {
                continue;
            }

            highRiskRecords +=
                    analysisRecordRepository
                            .countByAnalysisIdAndRiskLevel(
                                    analysis.getId(),
                                    "high"
                            );

            highRiskRecords +=
                    analysisRecordRepository
                            .countByAnalysisIdAndRiskLevel(
                                    analysis.getId(),
                                    "critical"
                            );
        }

        response.setHighRiskRecordsCount(
                highRiskRecords
        );

        // ============================================
        // HIGH RISK PERCENTAGE
        // ============================================

        double highRiskPercentage =
                totalRecordsAnalyzed == 0
                        ? 0.0
                        : ((double) highRiskRecords
                        / totalRecordsAnalyzed) * 100.0;

        response.setHighRiskPercentage(
                Math.round(highRiskPercentage * 10.0) / 10.0
        );

        // ============================================
        // LATEST ANALYSIS
        // ============================================

        Analysis latestAnalysis =
                analyses.stream()
                        .filter(a ->
                                a.getStartedAt() != null
                        )
                        .max(
                                Comparator.comparing(
                                        Analysis::getStartedAt
                                )
                        )
                        .orElse(null);

        if (latestAnalysis != null) {

            response.setLatestVerdict(
                    latestAnalysis.getSecurityVerdict()
            );

            response.setLatestRiskScore(
                    latestAnalysis.getRiskScore() == null
                            ? 0.0
                            : latestAnalysis.getRiskScore()
            );

        } else {

            response.setLatestVerdict("Unknown");
            response.setLatestRiskScore(0.0);
        }

        // ============================================
        // SORT ANALYSES
        // ============================================

        List<Analysis> sortedAnalyses =
                new ArrayList<>(analyses);

        sortedAnalyses.sort(
                Comparator.comparing(
                        Analysis::getStartedAt,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        // ============================================
        // RECENT 10 ANALYSES
        // ============================================

        List<Analysis> recent =
                sortedAnalyses.size() > 10
                        ? sortedAnalyses.subList(0, 10)
                        : sortedAnalyses;

        List<DashboardResponse.RecentAnalysis>
                recentResponses =
                new ArrayList<>();

        for (Analysis analysis : recent) {

            DashboardResponse.RecentAnalysis item =
                    new DashboardResponse.RecentAnalysis();

            item.setId(
                    analysis.getId()
            );

            item.setDatasetId(
                    analysis.getDatasetId()
            );

            item.setDatasetName(
                    analysis.getDatasetName()
            );

            item.setCompletedAt(
                    analysis.getCompletedAt() == null
                            ? null
                            : analysis
                            .getCompletedAt()
                            .toString()
            );

            item.setStatus(
                    analysis.getStatus()
            );

            item.setVerdict(
                    analysis.getSecurityVerdict()
            );

            item.setRiskScore(
                    analysis.getRiskScore() == null
                            ? 0.0
                            : analysis.getRiskScore()
            );

            item.setRiskLevel(
                    analysis.getRiskLevel()
            );

            item.setTotalRecords(
                    analysis.getTotalRecords() == null
                            ? 0
                            : analysis.getTotalRecords()
            );

            item.setFlaggedRecords(
                    analysis.getFlaggedRecords() == null
                            ? 0
                            : analysis.getFlaggedRecords()
            );

            // ========================================
            // SECURITY DISTRIBUTION
            // ========================================

            item.setSecurityDistribution(
                    buildSecurityDistribution(
                            analysis.getId()
                    )
            );

            recentResponses.add(item);
        }

        response.setRecentAnalyses(
                recentResponses
        );

        return response;
    }

    // =================================================
    // BUILD SECURITY DISTRIBUTION
    // =================================================

    private List<DashboardResponse.SecurityDistribution>
    buildSecurityDistribution(Long analysisId) {

        List<AnalysisRecord> records =
                analysisRecordRepository
                        .findByAnalysisId(analysisId);

        Map<String, Long> counts =
                new LinkedHashMap<>();

        // Keep frontend order consistent
        counts.put("Secure", 0L);
        counts.put("Low Risk", 0L);
        counts.put("Moderate Risk", 0L);
        counts.put("High Risk", 0L);
        counts.put("Critical", 0L);
        counts.put("Unknown", 0L);

        for (AnalysisRecord record : records) {

            String verdict = record.getVerdict();

            if (verdict == null || verdict.isBlank()) {
                verdict = "Unknown";
            }

            counts.put(
                    verdict,
                    counts.getOrDefault(verdict, 0L) + 1
            );
        }

        long total = records.size();

        List<DashboardResponse.SecurityDistribution>
                distribution =
                new ArrayList<>();

        for (Map.Entry<String, Long> entry :
                counts.entrySet()) {

            long count = entry.getValue();

            double percentage =
                    total == 0
                            ? 0.0
                            : ((double) count / total) * 100.0;

            percentage =
                    Math.round(percentage * 10.0) / 10.0;

            distribution.add(
                    new DashboardResponse.SecurityDistribution(
                            entry.getKey(),
                            count,
                            percentage,
                            getDistributionColor(
                                    entry.getKey()
                            )
                    )
            );
        }

        return distribution;
    }

    // =================================================
    // COLORS
    // =================================================

    private String getDistributionColor(String label) {

        return switch (label) {

            case "Secure" ->
                    "#22C55E";

            case "Low Risk" ->
                    "#84CC16";

            case "Moderate Risk" ->
                    "#F59E0B";

            case "High Risk" ->
                    "#F97316";

            case "Critical" ->
                    "#EF4444";

            default ->
                    "#94A3B8";
        };
    }
}