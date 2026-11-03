#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# AI generated 2026-09-22
# reviewed by Alessandra O
# Stampa la versione del componente leggendola dal package.json, che ne e' l'unica fonte:
# la stessa da cui scripts/generate-version.js ricava src/version.ts.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGE_FILE="$SCRIPT_DIR/package.json"

if [ ! -f "$PACKAGE_FILE" ]; then
  echo "Errore: $PACKAGE_FILE non trovato." >&2
  exit 1
fi

VERSION=$(sed -n 's/.*"version"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$PACKAGE_FILE" | head -n 1)

if [ -z "$VERSION" ]; then
  echo "Errore: versione non trovata in $PACKAGE_FILE." >&2
  exit 1
fi

echo "$VERSION"
