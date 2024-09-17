#!/bin/bash

# Ensure the script exits if any command fails
set -e

# Define the default values for the properties
DEFAULT_SERVER_ADDRESS="-DserverAddress=localhost:50051"
DEFAULT_ACTION="-Daction=status"
SERVICE="healthCheck"

# Read the properties from command-line arguments or use default values
SERVER_ADDRESS=${1:-$DEFAULT_SERVER_ADDRESS}
ACTION=${2:-$DEFAULT_ACTION}

# Print the properties being used
echo "Calling service: $SERVICE"
echo "Using server address: $SERVER_ADDRESS"
echo "Using action: $ACTION"

# Script should be placed in the project root directory
TARGET="tpe1-g6-client-2024.1Q"
BUILD_PATH="../client/target"
TARGET_DIR="${BUILD_PATH:?}/${TARGET:?}"
TAR_FILE="${BUILD_PATH:?}/${TARGET:?}-bin.tar.gz"

# Check if the target directory exists and delete it if it does
if [ -d "$TARGET_DIR" ]; then
  rm -rf "$TARGET_DIR"
fi

tar -xzf "$TAR_FILE" -C $BUILD_PATH

# Run the client with the specified properties
sudo chmod +x "$BUILD_PATH/$TARGET/run-client.sh"
export JAVA_OPTS="$SERVER_ADDRESS $ACTION -Dservice=$SERVICE"
cd $BUILD_PATH/$TARGET
./run-client.sh