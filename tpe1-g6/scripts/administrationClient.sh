#!/bin/bash

# Ensure the script exits if any command fails
set -e

SERVICE="administrationClient"
SERVER_ADDRESS=${1}
ACTION=${2}
# Optional parameters
DOCTOR=${3-""}
LEVEL=${4-""}
AVAILABILITY=${5-""}

export JAVA_OPTS="-Dservice=$SERVICE $SERVER_ADDRESS $ACTION $DOCTOR $LEVEL $AVAILABILITY"

# Script should be placed in the project root directory
TARGET="tpe1-g6-client-2024.1Q"
BUILD_PATH="../client/target"
TARGET_DIR="${BUILD_PATH:?}/${TARGET:?}"
TAR_FILE="${BUILD_PATH:?}/${TARGET:?}-bin.tar.gz"

if [ ! -d "$TARGET_DIR" ]; then
  tar -xzf "$TAR_FILE" -C $BUILD_PATH
fi

sudo chmod +x "$BUILD_PATH/$TARGET/run-client.sh"
cd $BUILD_PATH/$TARGET
./run-client.sh