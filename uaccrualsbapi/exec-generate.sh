#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Carica JAVA_HOME da buildfiles/local.properties
source ./set-java-home.sh

./mvn io.swagger.codegen.v3:swagger-codegen-maven-plugin:generate@jaxrs_exposed_api 