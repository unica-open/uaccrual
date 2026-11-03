#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Carica JAVA_HOME da buildfiles/local.properties
source ./set-java-home.sh

# Script per risolvere le dipendenze Maven
cd "$(dirname "$0")"

# Esegui Maven per risolvere le dipendenze
./mvn dependency:resolve -U