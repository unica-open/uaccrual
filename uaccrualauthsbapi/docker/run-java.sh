#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# ===================================================================================
# Generic startup script for running arbitrary Java applications with
# ===================================================================================

# Set defaults.
DEBUG_SUSPEND=n
JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-docker}

usage() {
  cat <<EOF
Usage: $(basename "$0") [OPTIONS]

  Available options:
  -d, --debug       Enable remote debugging
  -h, --help        Show this help message and exit
  -s, --suspend     Wait for debugger to attach (implies -d)

  Environment variables:
  DEBUG_PORT        Port for remote debugging (default: 5005)
  DEBUG_SUSPEND     Suspend execution until debugger attaches (y/n, default: n)
  JAVA_OPTS         Additional Java options (default: -Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200)
  SPRING_PROFILES_ACTIVE  Spring profiles to activate (default: prod)

EOF
}

enable_debug() {
  DEBUG_PORT=${DEBUG_PORT:-5005}
  JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
}

# Parse options
while [[ $# -gt 0 ]]; do
  case $1 in
    -d|--debug)
      enable_debug
      shift
      ;;
    -s|--suspend)
      DEBUG_SUSPEND=y
      enable_debug
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

# Construct the command to execute
CMD="java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar /deployments/accrualauthsbapi.jar"

echo "Running: $CMD"
exec $CMD