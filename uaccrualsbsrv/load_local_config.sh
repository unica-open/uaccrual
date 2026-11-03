#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Script generico per leggere configurazioni da un file locale

CONFIG_FILE=".local/config"

if [ -f "$CONFIG_FILE" ]; then
  # Legge le configurazioni dal file
  source "$CONFIG_FILE"
else
  echo "Errore: File $CONFIG_FILE non trovato."
  echo "Creare questo file e specificare le configurazioni necessarie."
  echo "Ad esempio:"
  echo "  echo 'export JAVA_HOME=\"/c/Program Files/Java/jdk-17.0.12\"' > $CONFIG_FILE"
  exit 1
fi