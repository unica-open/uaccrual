#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Carica JAVA_HOME da buildfiles/local.properties
if [ -f buildfiles/local.properties ]; then
    export JAVA_HOME="$(grep '^java.home=' buildfiles/local.properties | cut -d'=' -f2-)"
fi
