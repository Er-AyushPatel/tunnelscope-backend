package com.tunnelscope.backend.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import com.tunnelscope.backend.dto.AnalysisInput;
import com.tunnelscope.backend.dto.AnalysisResponse;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OnnxModelService {

    private final OrtEnvironment environment;

    private final OrtSession trafficSession;
    private final OrtSession securitySession;
    private final OrtSession riskSession;

    public OnnxModelService() {

        try {

            this.environment = OrtEnvironment.getEnvironment();

            // =====================================================
            // LOAD MODELS
            // =====================================================

            this.trafficSession =
                    createSession("models/traffic_classifier.onnx");

            this.securitySession =
                    createSession("models/security_classifier.onnx");

            this.riskSession =
                    createSession("models/risk_regressor.onnx");

            System.out.println("=================================");
            System.out.println("NEW ONNX MODELS LOADED SUCCESSFULLY");
            System.out.println("Traffic Model  : READY");
            System.out.println("Security Model : READY");
            System.out.println("Risk Model     : READY");
            System.out.println("=================================");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load ONNX models",
                    e
            );
        }
    }

    // =============================================================
    // CREATE SESSION
    // =============================================================

    private OrtSession createSession(
            String resourcePath
    ) throws Exception {

        var inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException(
                    "ONNX model not found in classpath: "
                            + resourcePath
            );
        }

        byte[] modelBytes;

        try (inputStream) {
            modelBytes = inputStream.readAllBytes();
        }

        return environment.createSession(
                modelBytes,
                new OrtSession.SessionOptions()
        );
    }

    // =============================================================
    // MAIN PREDICTION
    // =============================================================

    public AnalysisResponse predict(
            AnalysisInput input
    ) throws Exception {

        TrafficResult traffic =
                predictTraffic(input);

        SecurityResult security =
                predictSecurity(input);

        double risk =
                predictRisk(input);

        return new AnalysisResponse(
                traffic.label,
                traffic.confidence,
                security.label,
                security.confidence,
                risk
        );
    }

    // =============================================================
    // TRAFFIC MODEL
    // =============================================================

    private TrafficResult predictTraffic(
            AnalysisInput input
    ) throws Exception {

        Map<String, OnnxTensor> inputs =
                createTrafficInputs(input);

        try {

            try (OrtSession.Result result =
                         trafficSession.run(inputs)) {

                // -------------------------------------------------
                // LABEL
                // -------------------------------------------------

                OnnxValue labelValue =
                        result.get("label")
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Traffic model output 'label' not found"
                                        )
                                );

                Object labelObject =
                        labelValue.getValue();

                String label;

                if (labelObject instanceof String[]) {
                    label = ((String[]) labelObject)[0];

                } else if (labelObject instanceof String[][]) {
                    label = ((String[][]) labelObject)[0][0];

                } else {
                    throw new RuntimeException(
                            "Unexpected traffic label output type: "
                                    + labelObject.getClass()
                    );
                }

                // -------------------------------------------------
                // PROBABILITIES
                // -------------------------------------------------

                OnnxValue probabilityValue =
                        result.get("probabilities")
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Traffic model output 'probabilities' not found"
                                        )
                                );

                Object probabilityObject =
                        probabilityValue.getValue();

                double maxProbability = 0.0;

                if (probabilityObject instanceof float[][]) {

                    float[][] probabilities =
                            (float[][]) probabilityObject;

                    for (float probability :
                            probabilities[0]) {

                        if (probability > maxProbability) {
                            maxProbability = probability;
                        }
                    }

                } else if (probabilityObject instanceof float[]) {

                    float[] probabilities =
                            (float[]) probabilityObject;

                    for (float probability :
                            probabilities) {

                        if (probability > maxProbability) {
                            maxProbability = probability;
                        }
                    }

                } else {
                    throw new RuntimeException(
                            "Unexpected traffic probability output type: "
                                    + probabilityObject.getClass()
                    );
                }

                double confidence =
                        maxProbability * 100.0;

                return new TrafficResult(
                        label,
                        confidence
                );
            }

        } finally {

            closeInputs(inputs);
        }
    }

    // =============================================================
    // SECURITY MODEL
    // =============================================================

    private SecurityResult predictSecurity(
            AnalysisInput input
    ) throws Exception {

        Map<String, OnnxTensor> inputs =
                createSecurityInputs(input);

        try {

            try (OrtSession.Result result =
                         securitySession.run(inputs)) {

                // -------------------------------------------------
                // LABEL
                // -------------------------------------------------

                OnnxValue labelValue =
                        result.get("label")
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Security model output 'label' not found"
                                        )
                                );

                Object labelObject =
                        labelValue.getValue();

                String label;

                if (labelObject instanceof String[]) {
                    label = ((String[]) labelObject)[0];

                } else if (labelObject instanceof String[][]) {
                    label = ((String[][]) labelObject)[0][0];

                } else {
                    throw new RuntimeException(
                            "Unexpected security label output type: "
                                    + labelObject.getClass()
                    );
                }

                // -------------------------------------------------
                // PROBABILITIES
                // -------------------------------------------------

                OnnxValue probabilityValue =
                        result.get("probabilities")
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Security model output 'probabilities' not found"
                                        )
                                );

                Object probabilityObject =
                        probabilityValue.getValue();

                double maxProbability = 0.0;

                if (probabilityObject instanceof float[][]) {

                    float[][] probabilities =
                            (float[][]) probabilityObject;

                    for (float probability :
                            probabilities[0]) {

                        if (probability > maxProbability) {
                            maxProbability = probability;
                        }
                    }

                } else if (probabilityObject instanceof float[]) {

                    float[] probabilities =
                            (float[]) probabilityObject;

                    for (float probability :
                            probabilities) {

                        if (probability > maxProbability) {
                            maxProbability = probability;
                        }
                    }

                } else {
                    throw new RuntimeException(
                            "Unexpected security probability output type: "
                                    + probabilityObject.getClass()
                    );
                }

                double confidence =
                        maxProbability * 100.0;

                return new SecurityResult(
                        label,
                        confidence
                );
            }

        } finally {

            closeInputs(inputs);
        }
    }

    // =============================================================
    // RISK MODEL
    // =============================================================

    private double predictRisk(
            AnalysisInput input
    ) throws Exception {

        Map<String, OnnxTensor> inputs =
                createSecurityInputs(input);

        try {

            try (OrtSession.Result result =
                         riskSession.run(inputs)) {

                OnnxValue riskOutput =
                        result.get("variable")
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Risk model output 'variable' not found"
                                        )
                                );

                Object riskObject =
                        riskOutput.getValue();

                if (riskObject instanceof float[][]) {

                    float[][] riskValue =
                            (float[][]) riskObject;

                    return riskValue[0][0];

                } else if (riskObject instanceof float[]) {

                    float[] riskValue =
                            (float[]) riskObject;

                    return riskValue[0];

                } else if (riskObject instanceof double[][]) {

                    double[][] riskValue =
                            (double[][]) riskObject;

                    return riskValue[0][0];

                } else if (riskObject instanceof double[]) {

                    double[] riskValue =
                            (double[]) riskObject;

                    return riskValue[0];

                } else {
                    throw new RuntimeException(
                            "Unexpected risk output type: "
                                    + riskObject.getClass()
                    );
                }
            }

        } finally {

            closeInputs(inputs);
        }
    }

    // =============================================================
    // TRAFFIC INPUTS
    // =============================================================

    private Map<String, OnnxTensor> createTrafficInputs(
            AnalysisInput input
    ) throws OrtException {

        return Map.of(

                "avg_packet_size_bytes",

                OnnxTensor.createTensor(
                        environment,
                        new float[][]{
                                {
                                        (float)
                                                input.getAvgPacketSizeBytes()
                                }
                        }
                ),

                "packet_rate_per_sec",

                OnnxTensor.createTensor(
                        environment,
                        new float[][]{
                                {
                                        (float)
                                                input.getPacketRatePerSec()
                                }
                        }
                ),

                "session_duration_sec",

                OnnxTensor.createTensor(
                        environment,
                        new float[][]{
                                {
                                        (float)
                                                input.getSessionDurationSec()
                                }
                        }
                ),

                "burstiness_index",

                OnnxTensor.createTensor(
                        environment,
                        new float[][]{
                                {
                                        (float)
                                                input.getBurstinessIndex()
                                }
                        }
                ),

                "ike_handshake_time_ms",

                OnnxTensor.createTensor(
                        environment,
                        new float[][]{
                                {
                                        (float)
                                                input.getIkeHandshakeTimeMs()
                                }
                        }
                )
        );
    }

    // =============================================================
    // SECURITY + RISK INPUTS
    // =============================================================

    private Map<String, OnnxTensor> createSecurityInputs(
            AnalysisInput input
    ) throws OrtException {

        return Map.of(

                "mode",

                OnnxTensor.createTensor(
                        environment,
                        new String[][]{
                                {
                                        input.getMode()
                                }
                        }
                ),

                "encryption_algo",

                OnnxTensor.createTensor(
                        environment,
                        new String[][]{
                                {
                                        input.getEncryptionAlgo()
                                }
                        }
                ),

                "dh_group",

                OnnxTensor.createTensor(
                        environment,
                        new String[][]{
                                {
                                        input.getDhGroup()
                                }
                        }
                ),

                "pfs",

                OnnxTensor.createTensor(
                        environment,
                        new String[][]{
                                {
                                        input.getPfs()
                                }
                        }
                ),

                "ip_version",

                OnnxTensor.createTensor(
                        environment,
                        new String[][]{
                                {
                                        input.getIpVersion()
                                }
                        }
                )
        );
    }

    // =============================================================
    // CLOSE INPUT TENSORS
    // =============================================================

    private void closeInputs(
            Map<String, OnnxTensor> inputs
    ) {

        for (OnnxTensor tensor : inputs.values()) {

            try {
                tensor.close();
            } catch (Exception ignored) {
            }
        }
    }

    // =============================================================
    // CLOSE MODELS
    // =============================================================

    @PreDestroy
    public void closeModels() {

        try {
            trafficSession.close();
        } catch (Exception ignored) {
        }

        try {
            securitySession.close();
        } catch (Exception ignored) {
        }

        try {
            riskSession.close();
        } catch (Exception ignored) {
        }

        System.out.println("ONNX models closed.");
    }

    // =============================================================
    // TRAFFIC RESULT
    // =============================================================

    private static class TrafficResult {

        String label;
        double confidence;

        TrafficResult(
                String label,
                double confidence
        ) {

            this.label = label;
            this.confidence = confidence;
        }
    }

    // =============================================================
    // SECURITY RESULT
    // =============================================================

    private static class SecurityResult {

        String label;
        double confidence;

        SecurityResult(
                String label,
                double confidence
        ) {

            this.label = label;
            this.confidence = confidence;
        }
    }
}