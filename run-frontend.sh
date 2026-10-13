#!/bin/bash
# ==============================================================================
# Credit Book - Frontend Launch Script for macOS
# ==============================================================================

echo "=========================================================="
echo " Starting Credit Book Frontend (Vite + React 18)          "
echo " Dev Server: http://localhost:5173                        "
echo "=========================================================="

cd "$(dirname "$0")/frontend" || exit 1
npm run dev
