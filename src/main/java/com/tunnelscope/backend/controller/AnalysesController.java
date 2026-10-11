package com.tunnelscope.backend.controller;

import com.tunnelscope.backend.dto.AnalysisSummaryResponse;
import com.tunnelscope.backend.entity.Analysis;
import com.tunnelscope.backend.entity.AnalysisRecord;
import com.tunnelscope.backend.service.AnalysisService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/analyses")
public class AnalysesController {

    private final AnalysisService analysisService;

    public AnalysesController(
            AnalysisService analysisService
    ) {
        this.analysisService = analysisService;
    }

    // =====================================================
    // UPLOAD + ANALYZE
    // =====================================================

    @PostMapping("/upload")
    public Analysis uploadAndAnalyze(
            @RequestParam("datasetId") Long datasetId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        return analysisService.analyzeDataset(
                datasetId,
                file
        );
    }

    // =====================================================
    // ANALYSIS REPORT
    // =====================================================

    @GetMapping("/{id}")
    public AnalysisSummaryResponse getAnalysis(
            @PathVariable Long id
    ) {

        return analysisService.getAnalysisSummary(id);
    }

    // =====================================================
    // ALL RECORDS
    // =====================================================

    @GetMapping("/{id}/records")
    public List<AnalysisRecord> getAnalysisRecords(
            @PathVariable Long id
    ) {

        return analysisService.getAnalysisRecords(id);
    }

    // =====================================================
    // SINGLE RECORD
    // =====================================================

    @GetMapping("/{id}/records/{recordId}")
    public AnalysisRecord getAnalysisRecord(
            @PathVariable Long id,
            @PathVariable Long recordId
    ) {

        return analysisService.getAnalysisRecord(
                id,
                recordId
        );
    }
}