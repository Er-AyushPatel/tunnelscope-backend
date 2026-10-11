package com.tunnelscope.backend.controller;

import com.tunnelscope.backend.dto.AnalysisInput;
import com.tunnelscope.backend.dto.AnalysisResponse;
import com.tunnelscope.backend.dto.DashboardSummary;
import com.tunnelscope.backend.entity.AnalysisResult;
import com.tunnelscope.backend.repository.AnalysisResultRepository;
import com.tunnelscope.backend.service.CsvService;
import com.tunnelscope.backend.service.OnnxModelService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final OnnxModelService onnxModelService;
    private final CsvService csvService;
    private final AnalysisResultRepository repository;

    public AnalysisController(
            OnnxModelService onnxModelService,
            CsvService csvService,
            AnalysisResultRepository repository
    ) {
        this.onnxModelService = onnxModelService;
        this.csvService = csvService;
        this.repository = repository;
    }

    // =====================================================
    // DIRECT PREDICTION
    // =====================================================

    @PostMapping("/predict")
    public AnalysisResponse predict(
            @RequestBody AnalysisInput input
    ) throws Exception {

        AnalysisResponse response =
                onnxModelService.predict(input);

        saveResult(response);

        return response;
    }

    // =====================================================
    // PREDICT ALL BUNDLED CSV ROWS
    // =====================================================

    @GetMapping("/csv")
    public List<AnalysisResponse> predictFromBundledCsv()
            throws Exception {

        List<AnalysisInput> inputs =
                csvService.readBundledCsv();

        List<AnalysisResponse> results =
                new ArrayList<>();

        for (AnalysisInput input : inputs) {

            AnalysisResponse response =
                    onnxModelService.predict(input);

            saveResult(response);

            results.add(response);
        }

        return results;
    }

    // =====================================================
    // PREDICT ONE BUNDLED CSV ROW
    // =====================================================

    @GetMapping("/csv/{rowIndex}")
    public AnalysisResponse predictFromBundledCsvRow(
            @PathVariable int rowIndex
    ) throws Exception {

        AnalysisInput input =
                csvService.readBundledCsvRow(rowIndex);

        AnalysisResponse response =
                onnxModelService.predict(input);

        saveResult(response);

        return response;
    }

    // =====================================================
    // PREDICT ONE UPLOADED CSV ROW
    // =====================================================

    @PostMapping("/upload/{rowIndex}")
    public AnalysisResponse predictFromUploadedCsvRow(
            @RequestParam("file") MultipartFile file,
            @PathVariable int rowIndex
    ) throws Exception {

        AnalysisInput input =
                csvService.readUploadedCsvRow(
                        file,
                        rowIndex
                );

        AnalysisResponse response =
                onnxModelService.predict(input);

        saveResult(response);

        return response;
    }

    // =====================================================
    // PREDICT ALL UPLOADED CSV ROWS
    // =====================================================

    @PostMapping("/upload")
    public List<AnalysisResponse> predictFromUploadedCsv(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        List<AnalysisInput> inputs =
                csvService.readUploadedCsv(file);

        List<AnalysisResponse> results =
                new ArrayList<>();

        for (AnalysisInput input : inputs) {

            AnalysisResponse response =
                    onnxModelService.predict(input);

            saveResult(response);

            results.add(response);
        }

        return results;
    }

    // =====================================================
    // ALL SAVED RESULTS
    // =====================================================

    @GetMapping("/results")
    public List<AnalysisResult> getAllResults() {
        return repository.findAll();
    }

    // =====================================================
    // DASHBOARD SUMMARY
    // =====================================================

    @GetMapping("/summary")
    public DashboardSummary getDashboardSummary() {

        DashboardSummary summary =
                new DashboardSummary();

        summary.setTotalAnalyses(
                repository.count()
        );

        summary.setStrongCount(
                repository.countBySecurityVerdict("Strong")
        );

        summary.setMediumCount(
                repository.countBySecurityVerdict("Medium")
        );

        summary.setWeakCount(
                repository.countBySecurityVerdict("Weak")
        );

        summary.setAverageRisk(
                repository.getAverageRisk()
        );

        summary.setAverageTrafficConfidence(
                repository.getAverageTrafficConfidence()
        );

        summary.setAverageSecurityConfidence(
                repository.getAverageSecurityConfidence()
        );

        return summary;
    }

    // =====================================================
    // SAVE RESULT
    // =====================================================

    private void saveResult(AnalysisResponse response) {

        AnalysisResult result =
                new AnalysisResult();

        result.setTrafficType(
                response.getTrafficType()
        );

        result.setTrafficConfidence(
                response.getTrafficConfidence()
        );

        result.setSecurityVerdict(
                response.getSecurityVerdict()
        );

        result.setSecurityConfidence(
                response.getSecurityConfidence()
        );

        result.setPredictedRisk(
                response.getPredictedRisk()
        );

        result.setCreatedAt(
                LocalDateTime.now()
        );

        repository.save(result);
    }
}