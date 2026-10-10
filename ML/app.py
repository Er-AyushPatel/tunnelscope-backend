"""
TunnelScope — Streamlit entrypoint forwarder
"""
import sys
from pathlib import Path

root_app = Path(__file__).resolve().parent.parent / "app.py"
if root_app.exists():
    with open(root_app, encoding="utf-8") as f:
        exec(f.read())
else:
    import streamlit as st
    st.error("Root app.py not found.")
