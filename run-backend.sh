#!/bin/bash
# ==============================================================================
# Credit Book - Backend Launch Script for macOS
# ==============================================================================

# Automatically resolve JAVA_HOME for macOS (Homebrew or standard JVM directories)
if [ -z "$JAVA_HOME" ]; then
  if [ -d "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
  elif [ -d "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
  elif [ -x "/usr/libexec/java_home" ]; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home)
  fi
fi

echo "=========================================================="
echo " Starting Credit Book Backend (Spring Boot 3 + Java 21)   "
echo " JAVA_HOME: $JAVA_HOME"
echo " Server Port: ${PORT:-8080}"
echo "=========================================================="

cd "$(dirname "$0")/backend" || exit 1
./mvnw spring-boot:run
