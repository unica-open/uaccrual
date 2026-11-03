#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Carica JAVA_HOME da buildfiles/local.properties
source ./set-java-home.sh

./mvn -DskipTests clean install



