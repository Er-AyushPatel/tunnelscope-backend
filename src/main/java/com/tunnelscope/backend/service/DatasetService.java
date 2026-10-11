package com.tunnelscope.backend.service;

import com.tunnelscope.backend.dto.DatasetResponse;
import com.tunnelscope.backend.entity.Analysis;
import com.tunnelscope.backend.entity.Dataset;
import com.tunnelscope.backend.repository.AnalysisRepository;
import com.tunnelscope.backend.repository.DatasetRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final AnalysisRepository analysisRepository;

    public DatasetService(
            DatasetRepository datasetRepository,
            AnalysisRepository analysisRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.analysisRepository = analysisRepository;
    }

    // =====================================================
    // UPLOAD DATASET
    // =====================================================

    public Dataset saveDataset(
            MultipartFile file
    ) throws Exception {

        Dataset dataset = new Dataset();

        dataset.setName(
                file.getOriginalFilename()
        );

        dataset.setFileName(
                file.getOriginalFilename()
        );

        dataset.setFileSize(
                file.getSize()
        );

        dataset.setStatus("Uploaded");

        try (
                InputStreamReader reader =
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8
                        )
        ) {

            CSVFormat format =
                    CSVFormat.DEFAULT.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .get();

            try (
                    CSVParser parser =
                            CSVParser.parse(
                                    reader,
                                    format
                            )
            ) {

                dataset.setColumnCount(
                        parser.getHeaderMap().size()
                );

                int rowCount = 0;

                for (CSVRecord ignored : parser) {
                    rowCount++;
                }

                dataset.setRowCount(rowCount);
            }
        }

        dataset.setUploadedAt(
                java.time.LocalDateTime.now()
        );

        return datasetRepository.save(dataset);
    }

    // =====================================================
    // GET ALL DATASETS
    // =====================================================

    public List<DatasetResponse> getAllDatasets() {

        return datasetRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET DATASET BY ID
    // =====================================================

    public DatasetResponse getDatasetById(
            Long id
    ) {

        Dataset dataset =
                datasetRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Dataset not found: " + id
                                )
                        );

        return toResponse(dataset);
    }

    // =====================================================
    // DELETE DATASET
    // =====================================================

    public void deleteDataset(
            Long id
    ) {

        if (!datasetRepository.existsById(id)) {

            throw new RuntimeException(
                    "Dataset not found: " + id
            );
        }

        datasetRepository.deleteById(id);
    }

    // =====================================================
    // ENTITY -> RESPONSE DTO
    // =====================================================

    private DatasetResponse toResponse(
            Dataset dataset
    ) {

        DatasetResponse response =
                new DatasetResponse();

        response.setId(dataset.getId());
        response.setName(dataset.getName());
        response.setFileName(dataset.getFileName());

        response.setSizeBytes(
                dataset.getFileSize() == null
                        ? 0L
                        : dataset.getFileSize()
        );

        response.setRowCount(
                dataset.getRowCount()
        );

        response.setColumnCount(
                dataset.getColumnCount()
        );

        response.setStatus(
                dataset.getStatus()
        );

        response.setUploadedAt(
                dataset.getUploadedAt()
        );

        // ================================================
        // FIND LATEST ANALYSIS FOR THIS DATASET
        // ================================================

        List<Analysis> analyses =
                analysisRepository
                        .findByDatasetIdOrderByStartedAtDesc(
                                dataset.getId()
                        );

        if (!analyses.isEmpty()) {

            Analysis latestAnalysis =
                    analyses.stream()
                            .max(
                                    Comparator.comparing(
                                            Analysis::getStartedAt,
                                            Comparator.nullsLast(
                                                    Comparator.naturalOrder()
                                            )
                                    )
                            )
                            .orElse(null);

            if (latestAnalysis != null) {

                response.setAnalysisId(
                        latestAnalysis.getId()
                );
            }
        }

        return response;
    }
}