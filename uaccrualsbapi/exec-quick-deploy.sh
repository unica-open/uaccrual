#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Script per il deploy rapido dell'applicazione in un container Docker

echo "Starting quick deployment process..."

# Compila e crea il pacchetto
echo
echo "Building package..."
./exec-package.sh
if [ $? -ne 0 ]; then
    echo "Error during packaging. Exiting."
    exit 1
fi


VERSION=$(./get_version.sh) || exit 1
JAR=target/accrualsbapi-${VERSION}.jar

[ ! -f "$JAR" ] && echo "Jar $JAR non trovato: eseguire prima il package" && exit 1

echo
echo "Looking for accrualsbapi container..."

# Cerca il container (anche se stoppato)
CONTAINER_NAME=$(docker ps -a --filter "name=accrualsbapi" --format "{{.Names}}" | head -n 1)

if [ -z "$CONTAINER_NAME" ]; then
    echo "No container with 'accrualsbapi' in the name found."
    echo "Please run exec-docker-image.sh first to create the container."
    exit 1
fi

echo "Found container: $CONTAINER_NAME"

# Verifica se è in esecuzione, altrimenti avvialo
CONTAINER_RUNNING=$(docker ps --filter "name=$CONTAINER_NAME" --format "{{.Names}}")
if [ -z "$CONTAINER_RUNNING" ]; then
    echo "Container is stopped. Starting it..."
    docker start "$CONTAINER_NAME"
    sleep 2
fi

echo
echo "Copying JAR to container..."

# Copia il file JAR nel container
docker cp "$JAR" "$CONTAINER_NAME:/deployments/accrualsbapi.jar"
if [ $? -ne 0 ]; then
    echo "Error copying JAR to container. Exiting."
    exit 1
fi

echo
echo "Restarting container..."

# Riavvia il container
docker restart "$CONTAINER_NAME"
if [ $? -ne 0 ]; then
    echo "Error restarting container. Exiting."
    exit 1
fi

echo
echo "Quick deployment completed successfully!"
echo "Container '$CONTAINER_NAME' has been restarted with the new JAR."
