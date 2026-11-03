#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# =============================================================================
# ACCRUALWCL - Local Development Startup Script
# =============================================================================
# Questo script avvia il frontend Angular in modalità sviluppo locale
# =============================================================================

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Directory dello script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}ACCRUALWCL - Avvio Sviluppo Locale${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Verifica che proxy.conf.local.json esista
if [ ! -f "proxy.conf.local.json" ]; then
    echo -e "${RED}ERRORE: proxy.conf.local.json non trovato!${NC}"
    echo ""
    echo "Copia il file sample e personalizzalo:"
    echo "  cp proxy.conf.local.json.sample proxy.conf.local.json"
    echo ""
    exit 1
fi

# Verifica che node_modules esista
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}WARNING: node_modules non trovato. Eseguo npm install...${NC}"
    npm install
    if [ $? -ne 0 ]; then
        echo -e "${RED}ERRORE: npm install fallito!${NC}"
        exit 1
    fi
fi

# Avvia il server di sviluppo
echo -e "${GREEN}Avvio Angular Dev Server con proxy...${NC}"
echo ""
echo -e "${YELLOW}URL: http://localhost:4200/accrualwcl/${NC}"
echo ""

npm run start:local
