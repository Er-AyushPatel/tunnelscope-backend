
package com.tunnelscope.backend.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TsharkPcapParser {

    private static final int MAX_PACKETS = 200_000;

    public record Packet(
            double timestamp,
            long length,
            String sourceIp,
            String destinationIp,
            String protocol,
            Integer sourcePort,
            Integer destinationPort,
            String ipProtocol
    ) {}

    public record Session(
            String sourceIp,
            String destinationIp,
            String protocol,
            Integer sourcePort,
            Integer destinationPort,
            long packets,
            long bytes,
            double durationSec,
            double avgPacketSizeBytes,
            double packetRatePerSec,
            double burstinessIndex,
            double ikeHandshakeTimeMs
    ) {}

    public List<Session> parse(Path pcapFile)
            throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder(
                "tshark",
                "-n",
                "-r", pcapFile.toAbsolutePath().toString(),
                "-T", "fields",
                "-E", "separator=\t",
                "-E", "quote=n",
                "-E", "occurrence=f",
                "-e", "frame.time_epoch",
                "-e", "frame.len",
                "-e", "ip.src",
                "-e", "ipv6.src",
                "-e", "ip.dst",
                "-e", "ipv6.dst",
                "-e", "_ws.col.Protocol",
                "-e", "tcp.srcport",
                "-e", "udp.srcport",
                "-e", "tcp.dstport",
                "-e", "udp.dstport",
                "-e", "ip.proto"
        );

        builder.redirectErrorStream(true);
        Process process = builder.start();

        List<Packet> packets = new ArrayList<>();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (packets.size() >= MAX_PACKETS) {
                    process.destroyForcibly();
                    throw new IOException(
                            "PCAP exceeds the supported limit of "
                                    + MAX_PACKETS + " packets.");
                }

                // TShark can emit diagnostic text on failure.
                Packet packet = parseLine(line);

                if (packet != null) {
                    packets.add(packet);
                } else if (output.length() < 4000) {
                    output.append(line).append('\n');
                }
            }
        }

        boolean finished = process.waitFor(60, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("TShark parsing timed out.");
        }

        if (process.exitValue() != 0) {
            throw new IOException(
                    "TShark could not parse the capture. "
                            + output.toString().trim());
        }

        if (packets.isEmpty()) {
            throw new IOException(
                    "No supported IP packets were found in this capture.");
        }

        return buildSessions(packets);
    }

    private Packet parseLine(String line) {
        String[] f = line.split("\t", -1);

        if (f.length < 12) {
            return null;
        }

        try {
            double timestamp = Double.parseDouble(f[0]);
            long length = Long.parseLong(f[1]);

            String src = firstNonBlank(f[2], f[3]);
            String dst = firstNonBlank(f[4], f[5]);

            if (src == null || dst == null) {
                return null;
            }

            String protocol = normalizeProtocol(f[6]);
            Integer srcPort = parseInteger(firstNonBlank(f[7], f[8]));
            Integer dstPort = parseInteger(firstNonBlank(f[9], f[10]));

            return new Packet(
                    timestamp, length, src, dst, protocol,
                    srcPort, dstPort, f[11]);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private List<Session> buildSessions(List<Packet> packets) {
        Map<String, List<Packet>> grouped = new LinkedHashMap<>();

        for (Packet p : packets) {
            String left = endpoint(p.sourceIp(), p.sourcePort());
            String right = endpoint(p.destinationIp(), p.destinationPort());

            boolean reverse = left.compareTo(right) > 0;
            String key = reverse
                    ? right + "|" + left + "|" + p.protocol()
                    : left + "|" + right + "|" + p.protocol();

            grouped.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(p);
        }

        List<Session> sessions = new ArrayList<>();

        for (List<Packet> group : grouped.values()) {
            group.sort(Comparator.comparingDouble(Packet::timestamp));

            Packet first = group.get(0);
            Packet last = group.get(group.size() - 1);

            long bytes = group.stream().mapToLong(Packet::length).sum();
            double duration = Math.max(
                    0.0, last.timestamp() - first.timestamp());

            double avgSize = (double) bytes / group.size();
            double rate = duration > 0
                    ? group.size() / duration
                    : group.size();

            double burstiness = calculateBurstiness(group);

            sessions.add(new Session(
                    first.sourceIp(),
                    first.destinationIp(),
                    first.protocol(),
                    first.sourcePort(),
                    first.destinationPort(),
                    group.size(),
                    bytes,
                    duration,
                    avgSize,
                    rate,
                    burstiness,
                    0.0
            ));
        }

        return sessions;
    }

    private double calculateBurstiness(List<Packet> packets) {
        if (packets.size() < 3) {
            return 0.0;
        }

        List<Double> gaps = new ArrayList<>();

        for (int i = 1; i < packets.size(); i++) {
            gaps.add(Math.max(
                    0.0,
                    packets.get(i).timestamp()
                            - packets.get(i - 1).timestamp()));
        }

        double mean = gaps.stream()
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);

        if (mean == 0.0) {
            return 0.0;
        }

        double variance = gaps.stream()
                .mapToDouble(gap -> (gap - mean) * (gap - mean))
                .average().orElse(0.0);

        // Coefficient of variation; this is a proxy, not a trained
        // model feature definition unless training used the same formula.
        return Math.sqrt(variance) / mean;
    }

    private String endpoint(String ip, Integer port) {
        return ip + ":" + (port == null ? "-" : port);
    }

    private String normalizeProtocol(String protocol) {
        if (protocol == null || protocol.isBlank()) {
            return "IP";
        }

        String p = protocol.toUpperCase(Locale.ROOT);

        if (p.contains("IKE")) return "IKE";
        if (p.contains("ESP")) return "ESP";
        if (p.contains("TCP")) return "TCP";
        if (p.contains("UDP")) return "UDP";

        return protocol;
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank()
                    ? null : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
