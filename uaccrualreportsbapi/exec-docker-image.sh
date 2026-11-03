#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

set -e  # Esce immediatamente in caso di errore

# Ottieni la directory dello script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "========================================="
echo "ACCURALREPORTSBAPI DOCKER IMAGE BUILD"
echo "========================================="

# Build Docker
echo ""
echo "Building Docker image..."
cd "$SCRIPT_DIR"
docker compose down --remove-orphans || true
docker compose build --no-cache

# Avvio container temporaneo per poi stopparlo
echo ""
echo "Creating container (will be stopped)..."
docker-compose up -d
sleep 2
docker-compose stop

echo ""
echo "========================================="
echo "DOCKER IMAGE CREATED!"
echo "Immagine pronta, container stoppato."
echo ""
echo "ORA ESEGUI:"
echo "  exec-quick-deploy.sh (per copiare il JAR nel container e avviarlo)"
echo "========================================="
