#!/bin/bash

# Navigate to parent directory
cd "$(dirname "$0")"

# Create output directories if they don't exist
mkdir -p atm/bin
mkdir -p bank/bin
mkdir -p utility/bin
mkdir -p logs

# Compile RPC classes if jar doesn't exist
if [ ! -f rpc/rpc.jar ]; then
    echo "Compiling RPC classes..."
    mkdir -p rpc/bin
    javac -d rpc/bin rpc/src/*.java
    cd rpc
    jar cf rpc.jar -C bin .
    cd ..
fi

# Compile Bank System
echo "Compiling Bank System..."
javac -cp "rpc/rpc.jar" -d bank/bin bank/src/*.java
# javac -cp "bank/lib/*" -d bank/bin bank/src/*.java

# Compile Utility System
echo "Compiling Utility System..."
javac -cp "rpc/rpc.jar" -d utility/bin utility/src/*.java
# javac -cp "utility/lib/*" -d utility/bin utility/src/*.java

# Compile ATM Application
echo "Compiling ATM Application..."
javac -cp "rpc/rpc.jar" -d atm/bin atm/src/*.java
# javac -cp "atm/lib/*" -d atm/bin atm/src/*.java

echo "All projects compiled successfully."
