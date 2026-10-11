package com.tunnelscope.backend.service;

import com.tunnelscope.backend.dto.AnalysisInput;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    public List<AnalysisInput> readBundledCsv() throws Exception {

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("data/synthetic_ipsec_dataset.csv");

        if (inputStream == null) {
            throw new RuntimeException("synthetic_ipsec_dataset.csv not found");
        }

        try (InputStreamReader reader =
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            try (CSVParser parser = CSVParser.parse(reader, format)) {

                List<AnalysisInput> inputs = new ArrayList<>();

                for (CSVRecord row : parser) {
                    inputs.add(convertRow(row));
                }

                return inputs;
            }
        }
    }

    public List<AnalysisInput> readUploadedCsv(MultipartFile file) throws Exception {

        try (InputStreamReader reader =
                     new InputStreamReader(
                             file.getInputStream(),
                             StandardCharsets.UTF_8)) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            try (CSVParser parser = CSVParser.parse(reader, format)) {

                List<AnalysisInput> inputs = new ArrayList<>();

                for (CSVRecord row : parser) {
                    inputs.add(convertRow(row));
                }

                return inputs;
            }
        }
    }

    public AnalysisInput readBundledCsvRow(int rowIndex) throws Exception {

        List<AnalysisInput> inputs = readBundledCsv();

        if (rowIndex < 1 || rowIndex > inputs.size()) {
            throw new RuntimeException(
                    "Invalid row number. Available rows: 1-" + inputs.size()
            );
        }

        return inputs.get(rowIndex - 1);
    }

    public AnalysisInput readUploadedCsvRow(
            MultipartFile file,
            int rowIndex
    ) throws Exception {

        List<AnalysisInput> inputs = readUploadedCsv(file);

        if (rowIndex < 1 || rowIndex > inputs.size()) {
            throw new RuntimeException(
                    "Invalid row number. Available rows: 1-" + inputs.size()
            );
        }

        return inputs.get(rowIndex - 1);
    }

    private AnalysisInput convertRow(CSVRecord row) {

        AnalysisInput input = new AnalysisInput();

        // Original CSV information
        input.setSampleId(row.get("sample_id"));
        input.setTrafficType(row.get("traffic_type"));
        input.setSecurityVerdict(row.get("security_verdict"));

        input.setOriginalRiskScore(
                Double.parseDouble(row.get("risk_score"))
        );

        // IPsec configuration
        input.setMode(row.get("mode"));
        input.setIpVersion(row.get("ip_version"));
        input.setEncryptionAlgo(row.get("encryption_algo"));
        input.setDhGroup(row.get("dh_group"));
        input.setPfs(row.get("pfs"));

        // Traffic / session features
        input.setAvgPacketSizeBytes(
                Double.parseDouble(row.get("avg_packet_size_bytes"))
        );

        input.setPacketRatePerSec(
                Double.parseDouble(row.get("packet_rate_per_sec"))
        );

        input.setSessionDurationSec(
                Double.parseDouble(row.get("session_duration_sec"))
        );

        input.setBurstinessIndex(
                Double.parseDouble(row.get("burstiness_index"))
        );

        input.setIkeHandshakeTimeMs(
                Double.parseDouble(row.get("ike_handshake_time_ms"))
        );

        return input;
    }
}