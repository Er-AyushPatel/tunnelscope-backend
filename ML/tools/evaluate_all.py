"""
TunnelScope — Full Evaluation & Benchmark Suite
Evaluates both CSV datasets (demo_weak, demo_strong, synthetic_test)
and PCAP captures (sample_weak, sample_strong, sample_ipsec).
"""

import sys
import warnings
from pathlib import Path
import numpy as np
import pandas as pd
import joblib
from sklearn.metrics import accuracy_score, f1_score, mean_absolute_error, r2_score, classification_report

# Suppress version warnings for clean presentation
warnings.filterwarnings("ignore")

# Feature contracts
CONFIG_FEATURES = ["mode", "encryption_algo", "dh_group", "pfs", "ip_version"]
BEHAVIOUR_FEATURES = [
    "avg_packet_size_bytes",
    "packet_rate_per_sec",
    "session_duration_sec",
    "burstiness_index",
    "ike_handshake_time_ms",
]

def print_banner(title: str):
    print("\n" + "=" * 74)
    print(f" {title}")
    print("=" * 74)

def evaluate_csv_benchmarks(data_dir: Path, models_dir: Path):
    print_banner("1. CSV BENCHMARK DATASET EVALUATION")

    traffic_m = joblib.load(models_dir / "traffic_classifier.joblib")
    security_m = joblib.load(models_dir / "security_classifier.joblib")
    risk_m = joblib.load(models_dir / "risk_regressor.joblib")

    datasets = [
        ("demo_weak_tunnels.csv", "Weak Tunnels Demo Baseline (30 tunnels)"),
        ("demo_strong_tunnels.csv", "Strong Tunnels Demo Baseline (30 tunnels)"),
        ("synthetic_ipsec_dataset_test.csv", "Held-out Test Evaluation Set (1,200 tunnels)"),
    ]

    for filename, desc in datasets:
        p = data_dir / filename
        if not p.exists():
            continue
        df = pd.read_csv(p)
        print(f"\n[*] Dataset: {filename} — {desc}")
        print(f"    Total samples: {len(df)}")

        # Traffic classification
        X_beh = df[BEHAVIOUR_FEATURES].astype(np.float32)
        y_traffic_pred = traffic_m.predict(X_beh)
        traffic_acc = accuracy_score(df["traffic_type"], y_traffic_pred)
        traffic_f1 = f1_score(df["traffic_type"], y_traffic_pred, average="macro")
        print(f"    [Traffic Classifier]   Accuracy: {traffic_acc*100:.2f}% | Macro F1: {traffic_f1:.4f}")

        # Security verdict
        X_cfg = df[CONFIG_FEATURES]
        y_sec_pred = security_m.predict(X_cfg)
        sec_acc = accuracy_score(df["security_verdict"], y_sec_pred)
        sec_f1 = f1_score(df["security_verdict"], y_sec_pred, average="macro")
        verdict_counts = dict(pd.Series(y_sec_pred).value_counts())
        print(f"    [Security Verdict]     Accuracy: {sec_acc*100:.2f}% | Macro F1: {sec_f1:.4f} | Predicted: {verdict_counts}")

        # Risk score
        y_risk_pred = risk_m.predict(X_cfg)
        mae = mean_absolute_error(df["risk_score"], y_risk_pred)
        r2 = r2_score(df["risk_score"], y_risk_pred) if len(df) > 5 else 0.0
        print(f"    [Risk Regressor]       MAE: {mae:.3f} | Pred Mean: {y_risk_pred.mean():.2f}/10 (Actual: {df['risk_score'].mean():.2f}/10)")


def evaluate_pcaps(data_dir: Path, models_dir: Path):
    print_banner("2. PCAP NETWORK CAPTURE PASSIVE EVALUATION")
    from tunnelscope.engine import assess
    from tunnelscope.feature_extractor import extract

    traffic_m = joblib.load(models_dir / "traffic_classifier.joblib")
    security_m = joblib.load(models_dir / "security_classifier.joblib")
    risk_m = joblib.load(models_dir / "risk_regressor.joblib")

    pcaps = [
        ("sample_weak.pcap", "Weak / Deprecated IPsec Capture (3DES + modp1024)"),
        ("sample_strong.pcap", "Modern / Robust IPsec Capture (AES-256-GCM + modp2048)"),
        ("sample_ipsec.pcap", "Default Sample Capture"),
    ]

    for pcap_name, desc in pcaps:
        pcap_path = data_dir / pcap_name
        if not pcap_path.exists():
            continue

        print(f"\n[*] Analyzing PCAP: {pcap_name}")
        print(f"    Description: {desc}")

        # Run passive engine
        result = assess(str(pcap_path))
        print(f"    Observed Packets:       {result['packets_total']}")
        print(f"    ESP Associations:       {result['esp_flows']}")
        neg = result.get('negotiation', {})
        print(f"    IKE Negotiation:        Cipher={neg.get('encryption_algo')} | DH Group={neg.get('dh_group')}")
        print(f"    Rules Engine Findings:  {len(result['findings'])} security findings triggered")
        for f in result["findings"]:
            print(f"       -> [{f['severity']:<8}] {f['id']}: {f['title']}")
        print(f"    Deterministic Risk:     {result['risk']['score']}/100 ({result['risk']['band']})")

        # ML extraction and inference
        feats = extract(str(pcap_path))
        if feats["results"]:
            rows = [r["behaviour"] for r in feats["results"]]
            X_beh = pd.DataFrame(rows)[BEHAVIOUR_FEATURES].astype(np.float32)
            lbls = traffic_m.predict(X_beh)
            confs = traffic_m.predict_proba(X_beh).max(axis=1)
            print("    ML Traffic Classification:")
            for r, lbl, c in zip(feats["results"], lbls, confs):
                print(f"       -> SA 0x{r['spi']}: predicted '{lbl}' (confidence: {c:.2f})")

def main():
    root = Path(__file__).resolve().parent.parent
    data_dir = root / "data"
    models_dir = root / "models"

    print("TunnelScope — Automated Test & Benchmark Runner")
    evaluate_csv_benchmarks(data_dir, models_dir)
    evaluate_pcaps(data_dir, models_dir)
    print_banner("ALL EVALUATIONS COMPLETED SUCCESSFULLY")

if __name__ == "__main__":
    main()
