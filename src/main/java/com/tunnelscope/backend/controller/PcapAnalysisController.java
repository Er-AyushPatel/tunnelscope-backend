
package com.tunnelscope.backend.controller;

import com.tunnelscope.backend.entity.Analysis;
import com.tunnelscope.backend.service.PcapAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/analyses")
public class PcapAnalysisController {

    private final PcapAnalysisService pcapAnalysisService;

    public PcapAnalysisController(
            PcapAnalysisService pcapAnalysisService) {
        this.pcapAnalysisService = pcapAnalysisService;
    }

    @PostMapping(
            value = "/upload-pcap",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Analysis uploadPcap(
            @RequestParam("file") MultipartFile file) {

        try {
            return pcapAnalysisService.analyze(file);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PCAP analysis failed: " + ex.getMessage(), ex);
        }
    }
}
