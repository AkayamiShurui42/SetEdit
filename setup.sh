#!/bin/bash

# ==============================================================================
# SetEditLocker Setup Script - Verified State System
# ==============================================================================
# This script ensures that the environment is correctly configured for building
# and installing SetEditLocker with the necessary privileged authority.
# ==============================================================================

set -e

# --- Colors for Output ---
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SetEditLocker: Verified State System Initializing ===${NC}"

# 1. Verify Environment: Termux
if [ -d "/data/data/com.termux" ]; then
    echo -e "${GREEN}[OK]${NC} Environment: Termux detected."
else
    echo -e "${YELLOW}[WARN]${NC} Environment: Non-Termux environment detected. Proceeding with caution."
fi

# 2. Verify Java Authority (Mathematical Precision)
REQUIRED_JAVA_VERSION="21"
if command -v java >/dev/null 2>&1; then
    JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VER" -ge "$REQUIRED_JAVA_VERSION" ]; then
        echo -e "${GREEN}[OK]${NC} Java: Version $JAVA_VER detected (Required: >= $REQUIRED_JAVA_VERSION)."
    else
        echo -e "${RED}[ERROR]${NC} Java: Version $JAVA_VER detected. Required version is $REQUIRED_JAVA_VERSION."
        exit 1
    fi
else
    echo -e "${RED}[ERROR]${NC} Java: Not found. Please install openjdk-21."
    exit 1
fi

# 3. Verify Execution Authority (The Foundation)
# Objective verification of Root/Shizuku/ADB
AUTHORITY_FOUND=false

echo -n "Checking Execution Authority..."

# Check for Root
if command -v su >/dev/null 2>&1; then
    if su -c "id" >/dev/null 2>&1; then
        echo -e "\n${GREEN}[OK]${NC} Authority: Root access verified."
        AUTHORITY_FOUND=true
    fi
fi

# Check for Shizuku (rish)
if [ "$AUTHORITY_FOUND" = false ]; then
    if command -v rish >/dev/null 2>&1; then
        echo -e "\n${GREEN}[OK]${NC} Authority: Shizuku (rish) detected."
        AUTHORITY_FOUND=true
    else
        # Check for rish dex/sh files in home
        if [ -f "$HOME/rish" ] && [ -f "$HOME/rish_shizuku.dex" ]; then
            echo -e "\n${GREEN}[OK]${NC} Authority: Shizuku (rish) scripts found in home."
            AUTHORITY_FOUND=true
        fi
    fi
fi

# Check for ADB
if [ "$AUTHORITY_FOUND" = false ]; then
    if command -v adb >/dev/null 2>&1; then
        if adb devices | grep -q "device$"; then
            echo -e "\n${GREEN}[OK]${NC} Authority: ADB device connected."
            AUTHORITY_FOUND=true
        fi
    fi
fi

if [ "$AUTHORITY_FOUND" = false ]; then
    echo -e "\n${RED}[ERROR]${NC} Authority: No privileged execution authority (Root/Shizuku/ADB) verified."
    echo -e "         SetEditLocker requires elevated permissions to function."
    exit 1
fi

# 4. Verify Project Integrity
if [ -f "gradlew" ]; then
    echo -e "${GREEN}[OK]${NC} Project: Gradle wrapper found."
    chmod +x gradlew
else
    echo -e "${RED}[ERROR]${NC} Project: gradlew not found. Are you in the project root?"
    exit 1
fi

# 5. Final State Verification
echo -e "${BLUE}=== System State Verified ===${NC}"
echo -e "Environment is ready for building."
echo -e "Run ${YELLOW}./gradlew assembleDebug${NC} to start the build."
