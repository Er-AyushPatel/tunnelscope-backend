package com.tunnelscope.backend.controller;

import com.tunnelscope.backend.dto.DatasetResponse;
import com.tunnelscope.backend.entity.Dataset;
import com.tunnelscope.backend.service.DatasetService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(
            DatasetService datasetService
    ) {
        this.datasetService = datasetService;
    }

    // =====================================================
    // UPLOAD DATASET
    // =====================================================

    @PostMapping("/upload")
    public Dataset uploadDataset(
            @RequestParam("file")
            MultipartFile file
    ) throws Exception {

        return datasetService.saveDataset(file);
    }

    // =====================================================
    // GET ALL DATASETS
    // =====================================================

    @GetMapping
    public List<DatasetResponse> getAllDatasets() {

        return datasetService.getAllDatasets();
    }

    // =====================================================
    // GET DATASET BY ID
    // =====================================================

    @GetMapping("/{id}")
    public DatasetResponse getDataset(
            @PathVariable Long id
    ) {

        return datasetService.getDatasetById(id);
    }

    // =====================================================
    // DELETE DATASET
    // =====================================================

    @DeleteMapping("/{id}")
    public String deleteDataset(
            @PathVariable Long id
    ) {

        datasetService.deleteDataset(id);

        return "Dataset deleted successfully";
    }
}