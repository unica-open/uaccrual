#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Script per leggere una proprietà da un file di configurazione

PROPERTY_FILE="buildfiles/local.properties"
PROPERTY_NAME="$1"

if [ -z "$PROPERTY_NAME" ]; then
  echo "Utilizzo: $0 <nome_proprietà>"
  echo "  Ad esempio: $0 java.home"
  exit 1
fi

if [ -f "$PROPERTY_FILE" ]; then
  # Cerca la proprietà nel file e stampa il valore
  PROPERTY_VALUE=$(grep "^${PROPERTY_NAME}=" "$PROPERTY_FILE" | cut -d'=' -f2-)

  if [ -n "$PROPERTY_VALUE" ]; then
    echo "$PROPERTY_VALUE"
  else
    echo "Proprietà '$PROPERTY_NAME' non trovata in $PROPERTY_FILE"
    exit 1
  fi
else
  echo "Errore: File $PROPERTY_FILE non trovato."
  echo "Creare questo file e specificare le configurazioni necessarie."
  exit 1
fi