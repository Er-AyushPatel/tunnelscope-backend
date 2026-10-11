"""
TunnelScope — Passive IPsec Protocol Assessment & Traffic Intelligence
Heartwarming, High-Contrast Light Theme with Warm Ambient Glow
"""

import os
import sys
import tempfile
import json
from pathlib import Path

# Path resolution for engine and models
ROOT_DIR = Path(__file__).resolve().parent
ML_DIR = ROOT_DIR / "ML"
if str(ML_DIR) not in sys.path:
    sys.path.insert(0, str(ML_DIR))
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

import streamlit as st
import pandas as pd
import numpy as np
import plotly.graph_objects as go
import plotly.express as px
import joblib

# Set Streamlit Page Configuration
st.set_page_config(
    page_title="TunnelScope — IPsec Protocol Intelligence",
    page_icon="🛡️",
    layout="wide",
    initial_sidebar_state="expanded",
)

# ── INJECT HEARTWARMING LIGHT THEME CSS ─────────────────────────────
st.markdown("""
<style>
    @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap');

    html, body, [class*="css"] {
        font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
        color: #0f172a;
    }

    /* Overall Ambient Heartwarming Light Background */
    .stApp {
        background-color: #fafaf9;
        background-image: 
            radial-gradient(at 0% 0%, rgba(254, 243, 199, 0.55) 0px, transparent 48%),
            radial-gradient(at 100% 0%, rgba(224, 231, 255, 0.6) 0px, transparent 50%),
            radial-gradient(at 50% 100%, rgba(254, 226, 226, 0.4) 0px, transparent 50%),
            radial-gradient(at 0% 100%, rgba(236, 253, 245, 0.45) 0px, transparent 45%);
        background-attachment: fixed;
    }

    /* Main Container Padding */
    .block-container {
        padding-top: 1.8rem;
        padding-bottom: 3.5rem;
        max-width: 1400px;
    }

    /* Elevated Pure White Cards with Soft Ambient Shadow */
    div[data-testid="stVerticalBlockBorderWrapper"] {
        background: #ffffff !important;
        border: 1px solid rgba(226, 232, 240, 0.88) !important;
        border-radius: 18px !important;
        padding: 22px 26px !important;
        margin-bottom: 20px !important;
        box-shadow: 0 4px 24px -2px rgba(15, 23, 42, 0.05), 0 2px 6px -1px rgba(15, 23, 42, 0.02) !important;
        transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease !important;
    }
    div[data-testid="stVerticalBlockBorderWrapper"]:hover {
        border-color: rgba(99, 102, 241, 0.35) !important;
        box-shadow: 0 12px 30px -4px rgba(15, 23, 42, 0.08) !important;
        transform: translateY(-2px);
    }

    /* Metric Boxes (KPIs) */
    .kpi-container {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 16px;
        margin-bottom: 22px;
    }
    .kpi-box {
        background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
        border: 1px solid rgba(226, 232, 240, 0.9);
        border-radius: 16px;
        padding: 18px 20px;
        box-shadow: 0 2px 10px rgba(15, 23, 42, 0.03);
        transition: all 0.2s ease;
    }
    .kpi-box:hover {
        border-color: #c7d2fe;
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(99, 102, 241, 0.08);
    }
    .kpi-label {
        font-size: 0.74rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        color: #64748b;
        margin-bottom: 6px;
    }
    .kpi-value {
        font-size: 1.65rem;
        font-weight: 800;
        color: #0f172a;
        line-height: 1.2;
    }
    .kpi-sub {
        font-size: 0.78rem;
        color: #94a3b8;
        font-weight: 500;
        margin-top: 4px;
    }

    /* Hero Header Banner */
    .hero-banner {
        background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
        border: 1px solid rgba(99, 102, 241, 0.18);
        border-radius: 20px;
        padding: 26px 32px;
        margin-bottom: 24px;
        box-shadow: 0 8px 30px -4px rgba(99, 102, 241, 0.07);
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 16px;
    }

    /* Status Pills / Badges (Light Palette) */
    .badge {
        display: inline-flex;
        align-items: center;
        padding: 4px 12px;
        border-radius: 9999px;
        font-size: 0.76rem;
        font-weight: 700;
        letter-spacing: 0.03em;
        text-transform: uppercase;
    }
    .badge-critical {
        background: #fef2f2;
        color: #b91c1c;
        border: 1px solid #fecaca;
    }
    .badge-high {
        background: #fff7ed;
        color: #c2410c;
        border: 1px solid #fed7aa;
    }
    .badge-medium {
        background: #fffbeb;
        color: #b45309;
        border: 1px solid #fde68a;
    }
    .badge-low {
        background: #f0fdf4;
        color: #15803d;
        border: 1px solid #bbf7d0;
    }
    .badge-strong {
        background: #ecfdf5;
        color: #047857;
        border: 1px solid #a7f3d0;
    }
    .badge-info {
        background: #eef2ff;
        color: #4338ca;
        border: 1px solid #c7d2fe;
    }

    /* Modern Segmented Pill Tabs Styling */
    .stTabs [data-baseweb="tab-list"] {
        gap: 8px;
        background: #f1f5f9;
        padding: 6px;
        border-radius: 14px;
        border: 1px solid rgba(226, 232, 240, 0.8);
        margin-bottom: 22px;
    }
    .stTabs [data-baseweb="tab"] {
        height: 42px;
        border-radius: 10px;
        color: #64748b;
        font-weight: 600;
        padding: 0 20px;
        background: transparent;
        border: none;
        transition: all 0.2s ease;
    }
    .stTabs [data-baseweb="tab"]:hover {
        color: #0f172a;
    }
    .stTabs [data-baseweb="tab-highlight"] {
        display: none !important;
    }
    .stTabs [aria-selected="true"] {
        background: #ffffff !important;
        color: #4f46e5 !important;
        font-weight: 700 !important;
        box-shadow: 0 2px 10px rgba(15, 23, 42, 0.08);
    }

    /* Monospace Text */
    .mono {
        font-family: 'JetBrains Mono', monospace;
    }

    /* Sidebar Styling */
    section[data-testid="stSidebar"] {
        background-color: #fbfbf9;
        border-right: 1px solid #e2e8f0;
    }

    /* Streamlit Expander */
    .streamlit-expanderHeader {
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        font-weight: 600;
        color: #0f172a;
    }
</style>
""", unsafe_allow_html=True)

# ── LOAD MACHINE LEARNING ARTIFACTS ────────────────────────────────
@st.cache_resource
def load_models():
    """Load pretrained GradientBoosting and DecisionTree models."""
    base_dirs = [
        ML_DIR / "models",
        ROOT_DIR / "models",
        ROOT_DIR / "ML" / "models"
    ]
    model_dir = next((d for d in base_dirs if d.exists()), base_dirs[0])
    
    traffic_p = model_dir / "traffic_classifier.joblib"
    if not traffic_p.exists():
        traffic_p = model_dir / "traffic_model.joblib"

    sec_p = model_dir / "security_classifier.joblib"
    if not sec_p.exists():
        sec_p = model_dir / "security_model.joblib"

    risk_p = model_dir / "risk_regressor.joblib"
    if not risk_p.exists():
        risk_p = model_dir / "risk_model.joblib"
    
    try:
        traffic = joblib.load(traffic_p) if traffic_p.exists() else None
        security = joblib.load(sec_p) if sec_p.exists() else None
        risk = joblib.load(risk_p) if risk_p.exists() else None
        return traffic, security, risk
    except Exception as e:
        st.error(f"Error loading models: {e}")
        return None, None, None

models = load_models()
traffic_model, security_model, risk_model = models

# ── LOAD TUNNELSCOPE CORE FUNCTIONS ───────────────────────────────
try:
    from tunnelscope.engine import assess, UNDETERMINED
    from tunnelscope.feature_extractor import extract, BEHAVIOUR_FEATURES, CONFIG_FEATURES
    HAS_ENGINE = True
except Exception as e:
    HAS_ENGINE = False
    st.error(f"Engine import error: {e}")

# ── QUANTUM READINESS EVALUATOR ───────────────────────────────────
def evaluate_quantum_readiness(dh_group: str, has_addke: bool, cipher_mode: str) -> tuple[str, str, str]:
    """
    Evaluates quantum readiness on a clear 3-tier scale: Low, Medium, High.
    Returns: (Level [Low|Medium|High], BadgeStyle, DirectSummary)
    """
    dh_str = str(dh_group or "").lower()
    
    # Tier 1: Post-Quantum KEM (ML-KEM or RFC 9370 ADDKE) -> High
    if has_addke or "ml-kem" in dh_str:
        return (
            "High",
            "badge-strong",
            "Quantum Resilient — Protected against Harvest-Now-Decrypt-Later (HNDL) attacks via Post-Quantum Key Exchange."
        )
    
    # Tier 2: Strong classical parameters (4096-bit MODP or P-384 / P-521) -> Medium
    elif "4096" in dh_str or "384" in dh_str or "521" in dh_str or "8192" in dh_str:
        return (
            "Medium",
            "badge-medium",
            "Moderate Quantum Readiness — High classical security floor, but lacks hybrid post-quantum key encapsulation."
        )
    
    # Tier 3: Classical legacy or standard DH (<= 2048-bit MODP, P-256) -> Low
    else:
        return (
            "Low",
            "badge-critical",
            "Low Quantum Readiness — Classical key exchange only. Highly vulnerable to Harvest-Now-Decrypt-Later (HNDL) interception."
        )

# ── HELPER: LIGHT RISK GAUGE CHART ────────────────────────────────
def create_risk_gauge(score: int, band: str):
    if score >= 65:
        bar_color = "#e11d48"  # Rose Red
    elif score >= 35:
        bar_color = "#d97706"  # Warm Amber
    else:
        bar_color = "#059669"  # Emerald

    fig = go.Figure(go.Indicator(
        mode="gauge+number",
        value=score,
        domain={'x': [0, 1], 'y': [0, 1]},
        title={'text': f"<b>RISK POSTURE: {band}</b>", 'font': {'size': 13, 'color': '#475569', 'family': 'Plus Jakarta Sans'}},
        number={'suffix': "/100", 'font': {'size': 38, 'color': '#0f172a', 'family': 'Plus Jakarta Sans', 'weight': 'bold'}},
        gauge={
            'axis': {'range': [0, 100], 'tickwidth': 1, 'tickcolor': "#cbd5e1", 'tickfont': {'color': '#64748b', 'size': 10}},
            'bar': {'color': bar_color, 'thickness': 0.3},
            'bgcolor': "#f1f5f9",
            'borderwidth': 0,
            'steps': [
                {'range': [0, 35], 'color': "rgba(16, 185, 129, 0.12)"},
                {'range': [35, 65], 'color': "rgba(245, 158, 11, 0.12)"},
                {'range': [65, 100], 'color': "rgba(244, 63, 94, 0.12)"},
            ],
            'threshold': {
                'line': {'color': bar_color, 'width': 3},
                'thickness': 0.8,
                'value': score
            }
        }
    ))
    fig.update_layout(
        height=210,
        margin=dict(l=15, r=15, t=32, b=10),
        paper_bgcolor='rgba(0,0,0,0)',
        plot_bgcolor='rgba(0,0,0,0)',
        font=dict(family="Plus Jakarta Sans")
    )
    return fig

# ── HELPER: RESIDUE DISTRIBUTION PLOT ─────────────────────────────
def create_residue_chart(residue_dict: dict, cipher_mode: str):
    x_classes = [f"Residue {k}" for k in range(16)]
    y_counts = [residue_dict.get(k, 0) for k in range(16)]
    colors = ["#4f46e5" if c > 0 else "#e2e8f0" for c in y_counts]
    
    fig = go.Figure(data=[
        go.Bar(
            x=x_classes,
            y=y_counts,
            marker_color=colors,
            marker_line_width=0,
            hoverinfo="x+y",
        )
    ])
    fig.update_layout(
        title=dict(
            text=f"Length Residue mod 16 (Lattice Mode: <b>{cipher_mode}</b>)",
            font=dict(size=13, color="#0f172a")
        ),
        xaxis=dict(tickangle=-45, tickfont=dict(size=9, color="#64748b"), gridcolor="#f1f5f9"),
        yaxis=dict(title="Packets", gridcolor="#f1f5f9", tickfont=dict(color="#64748b")),
        height=230,
        margin=dict(l=10, r=10, t=30, b=30),
        paper_bgcolor='rgba(0,0,0,0)',
        plot_bgcolor='rgba(0,0,0,0)',
    )
    return fig

# ── HELPER: TRAFFIC CLASSIFICATION BAR CHART ──────────────────────
def create_traffic_bar(labels, probs):
    df_chart = pd.DataFrame({"Class": labels, "Probability": probs}).sort_values("Probability", ascending=True)
    fig = px.bar(
        df_chart,
        x="Probability",
        y="Class",
        orientation="h",
        text=df_chart["Probability"].apply(lambda p: f"{p*100:.1f}%"),
        color="Probability",
        color_continuous_scale=[[0, "#f1f5f9"], [0.5, "#818cf8"], [1, "#4f46e5"]]
    )
    fig.update_layout(
        height=250,
        margin=dict(l=10, r=25, t=10, b=10),
        xaxis=dict(range=[0, 1.08], showgrid=True, gridcolor="#f1f5f9", tickformat=".0%", tickfont=dict(color="#64748b")),
        yaxis=dict(title="", tickfont=dict(color="#0f172a")),
        coloraxis_showscale=False,
        paper_bgcolor='rgba(0,0,0,0)',
        plot_bgcolor='rgba(0,0,0,0)',
        font=dict(family="Plus Jakarta Sans", color="#0f172a")
    )
    fig.update_traces(textposition='outside', textfont=dict(color="#0f172a", size=11))
    return fig

# ── SIDEBAR CONTROLS ──────────────────────────────────────────────
with st.sidebar:
    st.markdown("""
    <div style="display:flex; align-items:center; gap:12px; margin-bottom:16px;">
        <span style="font-size:2.2rem;">🛡️</span>
        <div>
            <h2 style="margin:0; font-size:1.35rem; font-weight:800; color:#0f172a; letter-spacing:-0.02em;">TunnelScope</h2>
            <span style="font-size:0.75rem; color:#4f46e5; font-weight:700; text-transform:uppercase; letter-spacing:0.06em;">Protocol Intelligence</span>
        </div>
    </div>
    """, unsafe_allow_html=True)
    
    st.markdown("---")
    st.markdown("##### 📁 Input Capture")
    
    data_dir = ML_DIR / "data" if (ML_DIR / "data").exists() else ROOT_DIR / "data"
    
    # Auto-discover all .pcap and .pcapng files in data_dir
    discovered_files = sorted(list(data_dir.glob("*.pcap*")), key=lambda f: f.name.lower()) if data_dir.exists() else []
    file_map = {f.name: f for f in discovered_files}
    
    capture_source = st.radio(
        "Select Capture Source:",
        ["Choose from Capture Library", "Upload Custom .PCAP"],
        index=0,
        key="capture_source_radio"
    )
    
    active_pcap_path = None
    uploaded_name = None
    
    if capture_source == "Choose from Capture Library":
        if file_map:
            def format_pcap_label(filename):
                size_kb = file_map[filename].stat().st_size / 1024
                if filename == "sample_weak.pcap":
                    return f"🔴 {filename} (Deprecated 3DES · {size_kb:.0f} KB)"
                elif filename == "sample_strong.pcap":
                    return f"🟢 {filename} (Modern AES-GCM · {size_kb:.0f} KB)"
                elif filename == "sample_ipsec.pcap":
                    return f"🟡 {filename} (Standard IPsec · {size_kb:.0f} KB)"
                else:
                    return f"📄 {filename} ({size_kb:.1f} KB)"
            
            file_names_list = list(file_map.keys())
            default_idx = file_names_list.index("sample_weak.pcap") if "sample_weak.pcap" in file_names_list else 0

            chosen_file = st.selectbox(
                f"Available Files in ML/data/ ({len(file_map)} found):",
                options=file_names_list,
                index=default_idx,
                format_func=format_pcap_label,
                key="library_file_selectbox",
                help="Add any .pcap or .pcapng file to the ML/data/ folder and it will automatically appear here!"
            )
            selected_path = file_map[chosen_file]
            active_pcap_path = str(selected_path)
            uploaded_name = chosen_file
        else:
            st.info("No .pcap files found in `ML/data/`. Please upload one below.")
    else:
        uploaded_file = st.file_uploader("Upload .pcap or .pcapng file", type=["pcap", "pcapng"], key="custom_pcap_uploader")
        if uploaded_file is not None:
            with tempfile.NamedTemporaryFile(delete=False, suffix=".pcap") as tmp:
                tmp.write(uploaded_file.read())
                active_pcap_path = tmp.name
                uploaded_name = uploaded_file.name

    st.markdown("---")
    st.markdown("##### ⚙️ Engine Settings")
    show_ml = st.checkbox("Enable ML Corroboration Layer", value=True)
    
    st.markdown("---")
    st.markdown("""
    <div style="background:#f8fafc; padding:14px 16px; border-radius:12px; font-size:0.8rem; color:#475569; line-height:1.5; border:1px solid #e2e8f0;">
        <b>🔒 Passive Analysis:</b><br/>
        Zero decryption needed. Infers cipher modes strictly via packet length residue analysis mod 16 and IKEv2 cleartext handshakes.
    </div>
    """, unsafe_allow_html=True)

# ── HERO HEADER ───────────────────────────────────────────────────
st.markdown(f"""
<div class="hero-banner">
    <div>
        <div style="display:flex; align-items:center; gap:10px; margin-bottom:8px;">
            <span class="badge badge-info">PROTOCOL SECURITY AUDITOR</span>
            <span class="badge badge-low">RFC 7296 / RFC 9370</span>
        </div>
        <h1 style="margin:0; font-size:1.85rem; font-weight:800; color:#0f172a; letter-spacing:-0.02em;">TunnelScope — Network Security Intelligence</h1>
        <p style="margin:6px 0 0 0; font-size:0.92rem; color:#64748b;">
            Deterministic protocol parsing, modular lattice mathematics, and ML classification for encrypted IPsec tunnels.
        </p>
    </div>
    <div style="text-align:right;">
        <span style="font-size:0.75rem; color:#94a3b8; font-weight:700; text-transform:uppercase;">Active Capture</span><br/>
        <span class="mono" style="font-weight:700; color:#4f46e5; font-size:1rem;">{uploaded_name or 'No capture selected'}</span>
    </div>
</div>
""", unsafe_allow_html=True)

# ── NAVIGATION TABS ───────────────────────────────────────────────
tab_pcap, tab_simulator, tab_benchmarks = st.tabs([
    "🔍 PCAP Protocol Inspector", 
    "🧪 Live Tunnel Simulator", 
    "📊 Benchmarks & Architecture"
])

# ════════════════════════════════════════════════════════════════════
# TAB 1: PCAP PROTOCOL INSPECTOR
# ════════════════════════════════════════════════════════════════════
with tab_pcap:
    if not active_pcap_path or not os.path.exists(active_pcap_path):
        st.warning("Please upload a .pcap file or select a preset from the sidebar to inspect.")
    else:
        with st.spinner("Dissecting PCAP frames & computing lattice metrics..."):
            try:
                result = assess(active_pcap_path)
                feats = extract(active_pcap_path) if show_ml else None
            except Exception as e:
                st.error(f"Error analyzing capture: {e}")
                result = None

        if result:
            risk_info = result.get("risk") or {}
            risk_score = risk_info.get("score", 0)
            risk_band = risk_info.get("band", "UNKNOWN")
            neg = result.get("negotiation") or {}
            
            # Evaluate Quantum Readiness
            qr_level, qr_badge, qr_summary = evaluate_quantum_readiness(
                dh_group=neg.get("dh_group"),
                has_addke=bool(neg.get("additional_ke")),
                cipher_mode=neg.get("encryption_algo")
            )

            # Safely format negotiation metrics
            rtt = neg.get('ike_handshake_time_ms')
            rtt_str = f"{rtt:.1f} ms" if isinstance(rtt, (int, float)) else "N/A (ESP-only)"
            integ = neg.get('integrity') or 'AEAD'
            key_bits = neg.get('key_length_bits')
            
            if key_bits:
                cipher_sub = f"{key_bits} bits | {integ}"
            elif neg.get('is_aead'):
                cipher_sub = "AEAD (Integrated MAC)"
            elif integ and integ != "UNDETERMINED":
                cipher_sub = f"Integrity: {integ}"
            else:
                cipher_sub = "ESP-only capture (No cleartext IKE)"

            # ── TOP KPI ROW ──────────────────────────────────────
            st.markdown(f"""
            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-label">Observed Packets</div>
                    <div class="kpi-value">{result.get('packets_total', 0):,}</div>
                    <div class="kpi-sub">Total frames in capture</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-label">Security Associations</div>
                    <div class="kpi-value">{result.get('esp_flows', 0)} SAs</div>
                    <div class="kpi-sub">Active ESP SPI streams</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-label">Negotiated Cipher</div>
                    <div class="kpi-value" style="font-size:1.35rem; color:#4f46e5;">{neg.get('encryption_algo', 'UNDETERMINED')}</div>
                    <div class="kpi-sub">{cipher_sub}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-label">Key Exchange Primitive</div>
                    <div class="kpi-value" style="font-size:1.25rem;">{neg.get('dh_group', 'UNDETERMINED')}</div>
                    <div class="kpi-sub">Handshake RTT: {rtt_str}</div>
                </div>
            </div>
            """, unsafe_allow_html=True)

            # ── EXECUTIVE RISK & QUANTUM READINESS OVERVIEW ──────
            c_gauge, c_quantum = st.columns([1, 1.4])
            
            with c_gauge:
                with st.container(border=True):
                    st.plotly_chart(create_risk_gauge(risk_score, risk_band), use_container_width=True, key="pcap_risk_gauge_main")
                    st.markdown(f"""
                    <div style="text-align:center; margin-top:-8px;">
                        <span style="font-size:0.8rem; color:#64748b;">Evaluation Basis: <b>Deterministic Rules Engine</b></span>
                    </div>
                    """, unsafe_allow_html=True)

            with c_quantum:
                with st.container(border=True):
                    st.markdown(f"""
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                        <h4 style="margin:0; font-size:1.15rem; font-weight:700; color:#0f172a;">⚛️ Quantum Readiness</h4>
                        <span class="badge {qr_badge}" style="font-size:0.85rem; padding:6px 14px;">READINESS: {qr_level.upper()}</span>
                    </div>
                    """, unsafe_allow_html=True)
                    
                    if qr_level == "Low":
                        st.markdown(f"""
                        <div style="background:#fef2f2; border:1px solid #fecaca; border-radius:12px; padding:16px; margin:10px 0;">
                            <span class="badge badge-critical">VULNERABILITY: HARVEST-NOW-DECRYPT-LATER</span>
                            <p style="margin:8px 0 0 0; font-size:0.88rem; color:#991b1b; line-height:1.5;">
                                <b>Rating: Low</b> — {qr_summary}
                            </p>
                        </div>
                        """, unsafe_allow_html=True)
                    elif qr_level == "Medium":
                        st.markdown(f"""
                        <div style="background:#fffbeb; border:1px solid #fde68a; border-radius:12px; padding:16px; margin:10px 0;">
                            <span class="badge badge-medium">PARTIAL QUANTUM RESISTANCE</span>
                            <p style="margin:8px 0 0 0; font-size:0.88rem; color:#92400e; line-height:1.5;">
                                <b>Rating: Medium</b> — {qr_summary}
                            </p>
                        </div>
                        """, unsafe_allow_html=True)
                    else:
                        st.markdown(f"""
                        <div style="background:#ecfdf5; border:1px solid #a7f3d0; border-radius:12px; padding:16px; margin:10px 0;">
                            <span class="badge badge-strong">QUANTUM RESILIENT</span>
                            <p style="margin:8px 0 0 0; font-size:0.88rem; color:#065f46; line-height:1.5;">
                                <b>Rating: High</b> — {qr_summary}
                            </p>
                        </div>
                        """, unsafe_allow_html=True)
                    
                    st.markdown(f"""
                    <div style="display:flex; justify-content:space-between; margin-top:14px; font-size:0.84rem; color:#64748b;">
                        <span>RFC 9370 Additional KE: <b>{'Offered' if neg.get('additional_ke') else 'None'}</b></span>
                        <span>PFS State: <b>{neg.get('pfs', 'UNDETERMINED')}</b></span>
                    </div>
                    """, unsafe_allow_html=True)

            # ── SECTION: SECURITY FINDINGS ────────────────────────
            with st.container(border=True):
                findings = result.get("findings") or []
                st.markdown(f"### 🛡️ Direct Security Findings ({len(findings)})")
                
                if not findings:
                    st.success("✅ Clean assessment: No cryptographic vulnerabilities detected.")
                else:
                    for f in findings:
                        sev = f.get("severity", "INFO")
                        badge_class = {
                            "CRITICAL": "badge-critical",
                            "HIGH": "badge-high",
                            "MEDIUM": "badge-medium",
                            "LOW": "badge-low",
                            "INFO": "badge-info"
                        }.get(sev, "badge-info")
                        
                        with st.expander(f"[{sev}] {f.get('id')}: {f.get('title')}", expanded=(sev in ["CRITICAL", "HIGH"])):
                            st.markdown(f"""
                            <p style="color:#334155; margin:0 0 8px 0; font-size:0.92rem;">{f.get('detail')}</p>
                            <div style="background:#f8fafc; border-left:3px solid #4f46e5; padding:10px 14px; border-radius:6px; font-size:0.85rem; color:#475569; border:1px solid #e2e8f0; border-left-width:3px;">
                                <b>Evidence:</b> <span class="mono" style="color:#0f172a;">{f.get('evidence')}</span>
                            </div>
                            """, unsafe_allow_html=True)

            # ── SECTION: MODULAR LATTICE RESIDUE MATHEMATICS ──────
            with st.container(border=True):
                st.markdown("### 📐 Modular Lattice Residue Analysis")
                st.markdown("""
                <p style="color:#64748b; font-size:0.88rem; margin-bottom:16px;">
                    Mathematically proves whether the tunnel uses <b>CBC block alignment</b> (lengths collapse strictly into 1 residue class mod 16) or <b>AEAD counter mode</b> (4-aligned and distributed across 4 residue classes mod 16) without decrypting payloads.
                </p>
                """, unsafe_allow_html=True)

                sas = result.get("sas") or []
                if sas:
                    sa_tabs = st.tabs([f"SA 0x{sa['spi']}" for sa in sas])
                    for idx, (sa, sa_tab) in enumerate(zip(sas, sa_tabs)):
                        with sa_tab:
                            col_l, col_r = st.columns([1.2, 1])
                            res_info = sa.get("residue") or {}
                            mode = res_info.get("mode", "UNDETERMINED")
                            raw_conf = res_info.get("confidence")
                            conf_val = float(raw_conf) if isinstance(raw_conf, (int, float)) else 0.0
                            raw_dur = sa.get('duration_sec')
                            dur_val = float(raw_dur) if isinstance(raw_dur, (int, float)) else 0.0
                            classes = res_info.get("residue_classes") or {}
                            
                            with col_l:
                                st.plotly_chart(
                                    create_residue_chart(classes, mode), 
                                    use_container_width=True, 
                                    key=f"residue_chart_{sa.get('spi', 'sa')}_{idx}"
                                )
                            with col_r:
                                st.markdown(f"""
                                <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; padding:18px;">
                                    <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
                                        <span style="font-size:0.85rem; color:#64748b;">Inferred Mode:</span>
                                        <span class="badge {'badge-strong' if mode == 'AEAD' else 'badge-medium'}">{mode}</span>
                                    </div>
                                    <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
                                        <span style="font-size:0.85rem; color:#64748b;">Lattice Confidence:</span>
                                        <b style="color:#0f172a;">{conf_val*100:.1f}%</b>
                                    </div>
                                    <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
                                        <span style="font-size:0.85rem; color:#64748b;">Flow Duration:</span>
                                        <span style="color:#0f172a; font-weight:600;">{dur_val:.2f} sec</span>
                                    </div>
                                    <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
                                        <span style="font-size:0.85rem; color:#64748b;">Distinct Packet Lengths:</span>
                                        <span style="color:#0f172a; font-weight:600;">{res_info.get('distinct_lengths', 0)}</span>
                                    </div>
                                    <hr style="margin:12px 0; border:0; border-top:1px solid #e2e8f0;"/>
                                    <span style="font-size:0.75rem; color:#64748b; font-weight:700;">MATHEMATICAL NOTES:</span>
                                    <ul style="margin:6px 0 0 0; padding-left:16px; font-size:0.8rem; color:#475569;">
                                        {''.join(f"<li>{n}</li>" for n in res_info.get('notes', []))}
                                    </ul>
                                </div>
                                """, unsafe_allow_html=True)

            # ── SECTION: ML TRAFFIC CLASSIFICATION ────────────────
            if show_ml and traffic_model and feats and feats.get("results"):
                with st.container(border=True):
                    st.markdown("### 🤖 ML Traffic Corroboration Engine")
                    st.markdown("""
                    <p style="color:#64748b; font-size:0.88rem;">
                        Gradient Boosting model predicting encrypted tunnel application types from packet timing, rate, and burstiness profiles.
                    </p>
                    """, unsafe_allow_html=True)

                    ml_rows = [r["behaviour"] for r in feats["results"]]
                    X_beh = pd.DataFrame(ml_rows)[BEHAVIOUR_FEATURES].astype(np.float32)
                    pred_labels = traffic_model.predict(X_beh)
                    pred_probs = traffic_model.predict_proba(X_beh)
                    classes_list = list(traffic_model.classes_)

                    c_ml_1, c_ml_2 = st.columns(2)
                    for idx, (r, lbl, prob_row) in enumerate(zip(feats["results"], pred_labels, pred_probs)):
                        col_target = c_ml_1 if idx % 2 == 0 else c_ml_2
                        with col_target:
                            st.markdown(f"**SA 0x{r['spi']} — Classified Application: `{lbl.upper()}`**")
                            st.plotly_chart(
                                create_traffic_bar(classes_list, prob_row), 
                                use_container_width=True, 
                                key=f"traffic_bar_{r.get('spi', 'r')}_{idx}"
                            )

            # ── EXPORT AUDIT DELIVERABLES ─────────────────────────
            with st.container(border=True):
                st.markdown("### 📥 Export Audit Reports")
                col_exp_1, col_exp_2 = st.columns(2)
                
                with col_exp_1:
                    json_str = json.dumps(result, indent=2)
                    st.download_button(
                        label="📄 Download Full JSON Audit Report",
                        data=json_str,
                        file_name=f"tunnelscope_report_{Path(uploaded_name).stem}.json",
                        mime="application/json",
                        use_container_width=True,
                        key="download_json_audit_report",
                    )
                
                with col_exp_2:
                    key_bits_desc = f"{key_bits} bits" if key_bits else "N/A"
                    md_report = f"""# TunnelScope Security Audit Report
Capture: {uploaded_name}
Risk Score: {risk_score}/100 ({risk_band})
Quantum Readiness: {qr_level} ({qr_summary})

## Negotiation Primitives
- Encryption: {neg.get('encryption_algo', 'UNDETERMINED')} ({key_bits_desc})
- Diffie-Hellman Group: {neg.get('dh_group', 'UNDETERMINED')}
- Integrity Transform: {neg.get('integrity', 'UNDETERMINED')}

## Security Findings
""" + "\n".join([f"- [{f['severity']}] {f['id']}: {f['title']}\n  Detail: {f['detail']}" for f in findings])
                    
                    st.download_button(
                        label="📝 Download Summary Markdown Report",
                        data=md_report,
                        file_name=f"tunnelscope_summary_{Path(uploaded_name).stem}.md",
                        mime="text/markdown",
                        use_container_width=True,
                        key="download_md_summary_report",
                    )

# ════════════════════════════════════════════════════════════════════
# TAB 2: LIVE TUNNEL SIMULATOR & PREDICTOR
# ════════════════════════════════════════════════════════════════════
with tab_simulator:
    with st.container(border=True):
        st.markdown("### 🧪 Live Cryptographic & Traffic Simulator")
        st.markdown("""
        <p style="color:#64748b; font-size:0.9rem;">
            Adjust cryptographic parameters and traffic profiles to test real-time model inferences and quantum posture without a PCAP.
        </p>
        """, unsafe_allow_html=True)

        col_sim_cfg, col_sim_beh = st.columns(2)

        with col_sim_cfg:
            st.markdown("##### 🔐 Cryptographic Configuration")
            sim_mode = st.selectbox("Mode", ["Tunnel", "Transport"], index=0)
            sim_enc = st.selectbox("Encryption Algorithm", [
                "AES-256-GCM", "AES-128-GCM", "AES-256-CBC", "AES-128-CBC", 
                "3DES", "DES", "ChaCha20-Poly1305"
            ], index=0)
            sim_dh = st.selectbox("Diffie-Hellman Group", [
                "ecp256(19)", "ecp384(20)", "modp2048(14)", "modp3072(15)", 
                "modp4096(16)", "modp1024(2)", "modp768(1)", "ML-KEM-768(36)"
            ], index=0)
            sim_pfs = st.selectbox("Perfect Forward Secrecy (PFS)", ["On", "Off"], index=0)
            sim_ipv = st.selectbox("IP Version", ["IPv4", "IPv6"], index=0)

        with col_sim_beh:
            st.markdown("##### ⏱️ Traffic Behavior Metrics")
            sim_size = st.slider("Average Packet Size (Bytes)", 64, 1500, 420)
            sim_rate = st.slider("Packet Rate (Packets / sec)", 1, 300, 35)
            sim_dur = st.slider("Session Duration (Seconds)", 5, 3600, 120)
            sim_burst = st.slider("Burstiness Index [0 = smooth, 1 = bursty]", 0.0, 1.0, 0.35)
            sim_rtt = st.slider("IKE Handshake Round-Trip (ms)", 10, 500, 85)

        st.markdown("---")
        
        if st.button("🚀 Run Live Model Inference", type="primary", use_container_width=True):
            if not security_model or not traffic_model or not risk_model:
                st.error("Models not found or failed to load.")
            else:
                # Predict Security & Risk
                cfg_df = pd.DataFrame([{
                    "mode": sim_mode,
                    "encryption_algo": sim_enc,
                    "dh_group": sim_dh,
                    "pfs": sim_pfs,
                    "ip_version": sim_ipv
                }])
                sec_pred = security_model.predict(cfg_df)[0]
                sec_conf = security_model.predict_proba(cfg_df).max()
                risk_pred = risk_model.predict(cfg_df)[0]

                # Predict Traffic
                beh_df = pd.DataFrame([{
                    "avg_packet_size_bytes": sim_size,
                    "packet_rate_per_sec": sim_rate,
                    "session_duration_sec": sim_dur,
                    "burstiness_index": sim_burst,
                    "ike_handshake_time_ms": sim_rtt
                }]).astype(np.float32)
                traffic_pred = traffic_model.predict(beh_df)[0]
                traffic_conf = traffic_model.predict_proba(beh_df).max()

                # Quantum Readiness in Simulator
                sim_qr, sim_qr_badge, _ = evaluate_quantum_readiness(sim_dh, False, sim_enc)

                res_c1, res_c2, res_c3, res_c4 = st.columns(4)
                with res_c1:
                    badge_c = "badge-strong" if sec_pred == "Strong" else ("badge-medium" if sec_pred == "Medium" else "badge-critical")
                    st.markdown(f"""
                    <div class="kpi-box" style="border-top:3px solid #4f46e5;">
                        <div class="kpi-label">Security Verdict</div>
                        <div class="kpi-value"><span class="badge {badge_c}">{sec_pred}</span></div>
                        <div class="kpi-sub">Confidence: <b>{sec_conf*100:.1f}%</b></div>
                    </div>
                    """, unsafe_allow_html=True)
                with res_c2:
                    st.markdown(f"""
                    <div class="kpi-box" style="border-top:3px solid #d97706;">
                        <div class="kpi-label">Predicted Risk Score</div>
                        <div class="kpi-value">{risk_pred:.2f} <span style="font-size:1rem; color:#94a3b8;">/ 10</span></div>
                        <div class="kpi-sub">Quantitative Regression Model</div>
                    </div>
                    """, unsafe_allow_html=True)
                with res_c3:
                    st.markdown(f"""
                    <div class="kpi-box" style="border-top:3px solid #0284c7;">
                        <div class="kpi-label">Quantum Readiness</div>
                        <div class="kpi-value"><span class="badge {sim_qr_badge}">{sim_qr.upper()}</span></div>
                        <div class="kpi-sub">Key Encapsulation Rating</div>
                    </div>
                    """, unsafe_allow_html=True)
                with res_c4:
                    st.markdown(f"""
                    <div class="kpi-box" style="border-top:3px solid #059669;">
                        <div class="kpi-label">Classified Traffic</div>
                        <div class="kpi-value" style="font-size:1.35rem; color:#059669;">{traffic_pred.upper()}</div>
                        <div class="kpi-sub">Confidence: <b>{traffic_conf*100:.1f}%</b></div>
                    </div>
                    """, unsafe_allow_html=True)

# ════════════════════════════════════════════════════════════════════
# TAB 3: BENCHMARKS & ARCHITECTURE
# ════════════════════════════════════════════════════════════════════
with tab_benchmarks:
    with st.container(border=True):
        st.markdown("### 📊 Production Model Benchmarks")
        
        b_col1, b_col2, b_col3 = st.columns(3)
        with b_col1:
            st.markdown("""
            <div class="kpi-box">
                <div class="kpi-label">Traffic Classifier</div>
                <div class="kpi-value" style="font-size:1.5rem; color:#4f46e5;">94.08% Acc</div>
                <div class="kpi-sub">GradientBoosting · 8 Classes · Macro F1 0.9418</div>
            </div>
            """, unsafe_allow_html=True)
        with b_col2:
            st.markdown("""
            <div class="kpi-box">
                <div class="kpi-label">Security Verdict</div>
                <div class="kpi-value" style="font-size:1.5rem; color:#059669;">100% Extreme</div>
                <div class="kpi-sub">DecisionTree · Rules Extracted · Macro F1 0.8931</div>
            </div>
            """, unsafe_allow_html=True)
        with b_col3:
            st.markdown("""
            <div class="kpi-box">
                <div class="kpi-label">Risk Regressor</div>
                <div class="kpi-value" style="font-size:1.5rem; color:#d97706;">1.02 MAE</div>
                <div class="kpi-sub">GradientBoosting · Continuous 0–10 score</div>
            </div>
            """, unsafe_allow_html=True)

        st.markdown("---")
        st.markdown("##### 📜 Extracted Decision Tree Rules")
        rules_path = ML_DIR / "models" / "security_rules.txt" if (ML_DIR / "models" / "security_rules.txt").exists() else ROOT_DIR / "models" / "security_rules.txt"
        if rules_path.exists():
            st.code(rules_path.read_text(), language="text")
        else:
            st.info("Train models via python ML/src/train_all_models.py to view security_rules.txt")
