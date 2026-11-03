#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# AI generated 2026-09-22
# reviewed by Alessandra O
# Stampa la versione dell'artefatto leggendola dal pom.xml, che ne e' l'unica fonte.
# Si prende la prima <version> dopo </parent>: quella del progetto, non quella del parent Spring Boot.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
POM_FILE="$SCRIPT_DIR/pom.xml"

if [ ! -f "$POM_FILE" ]; then
  echo "Errore: $POM_FILE non trovato." >&2
  exit 1
fi

VERSION=$(sed -n '/<\/parent>/,$ { s/.*<version>\(.*\)<\/version>.*/\1/p; }' "$POM_FILE" | head -n 1)

if [ -z "$VERSION" ]; then
  echo "Errore: versione non trovata in $POM_FILE." >&2
  exit 1
fi

echo "$VERSION"
