# 🛡️ TunnelScope — Network Security Intelligence

[![Python 3.10+](https://img.shields.io/badge/python-3.10+-blue.svg)](https://www.python.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> **Inspect encrypted VPN tunnels without breaking privacy or decrypting a single packet.**

---

## 💡 What is TunnelScope? (In Simple Words)

Imagine you send a letter inside a **locked titanium box** through the mail:
* Regular network tools try to pick the lock or smash the box open (decryption).
* **TunnelScope does something smarter:** It never touches the lock. Instead, it looks at the outside of the box — the lock brand, how heavy the package is, and how often packages arrive.

From just the outside, TunnelScope answers three big questions:
1. **Is the lock strong or outdated?** (Cryptographic Security Audit)
2. **Can a future quantum computer crack it?** (Quantum Readiness Rating)
3. **What are you sending?** A phone call? A video? A website visit? (AI Traffic Classification)

---

## 🚀 Key Features

### 1. 🔍 Passive Protocol Inspector
* Checks network captures (`.pcap`) against official cybersecurity standards (NIST & RFC).
* Immediately flags dangerous, ancient settings (like **3DES** or **1024-bit Diffie-Hellman**).
* Calculates an instant **Risk Score from 0 to 100**.

### 2. ⚛️ Quantum Readiness Audit
* Evaluates whether your tunnel is vulnerable to **"Harvest-Now-Decrypt-Later" (HNDL)** attacks — where hackers record encrypted data today to crack it later with quantum computers.
* Gives a clear 3-tier rating: **Low**, **Medium**, or **High**.

### 3. 📐 Length Residue Math (Zero Decryption)
* How do we know what cipher mode is used without decrypting? **Math!**
* Older block ciphers (CBC) force packet sizes to snap into fixed block grids (mod 16).
* Stream ciphers (AEAD / GCM) spread out smoothly.
* TunnelScope graphs this modular distribution in real time.

### 4. 🤖 AI Traffic Detective
* Even though the data is 100% encrypted, different apps produce different packet rhythms:
  * **VoIP (Audio):** Small, steady packets every 20ms.
  * **Video Streaming:** Big, bursty packet trains.
  * **DNS:** Tiny single question-and-answer pairs.
* An AI model (Gradient Boosting) predicts the application with **94%+ accuracy**.

---

## 🧪 How to Run the ML Engine & Dashboard

### 1. Run the Dashboard Locally

```bash
# 1. Clone the repository
git clone https://github.com/shivanshu1512/TunnelScope.git
cd TunnelScope

# 2. Install ML requirements
pip install -r ML/requirements.txt

# 3. Launch Streamlit
streamlit run ML/app.py
```
Open your browser at `http://localhost:8501`.

### 2. Run via Command-Line Interface (CLI)

```bash
# Audit a weak IPsec tunnel capture
python -m ML.tunnelscope.cli ML/data/sample_weak.pcap --models ML/models

# Audit a modern/strong IPsec tunnel capture
python -m ML.tunnelscope.cli ML/data/sample_strong.pcap --models ML/models
```

---

## 📁 Repository Structure

```text
TunnelScope/
├── ML/
│   ├── app.py              # Streamlit Web Dashboard
│   ├── data/               # Network captures (.pcap) & datasets (.csv)
│   ├── models/             # Trained AI models (.joblib & ONNX)
│   ├── notebooks/          # Model training & research notebooks
│   ├── src/                # Pipeline training scripts
│   ├── tools/              # Dataset generation & evaluation tools
│   ├── tunnelscope/        # Core passive protocol parsing & math engine
│   │   ├── engine.py       # Pure Python PCAP & IKE/ESP parser
│   │   ├── feature_extractor.py # Feature extraction pipeline
│   │   └── cli.py          # Terminal CLI tool
│   └── requirements.txt    # Python dependencies for ML
├── README.md               # Documentation
└── .gitignore              # Git ignore rules
```

---

## 👨‍💻 Author
Built with ❤️ by **[Shivanshu Shukla](https://github.com/shivanshu1512)**
