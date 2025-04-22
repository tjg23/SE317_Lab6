#!/bin/bash

# Navigate to parent directory
cd "$(dirname "$0")"

# Create log directory if it doesn't exist
mkdir _logs

# Recompile the RPC library
javac -d rpc/bin rpc/src/*.java
jar cf lib/rpc.jar -C rpc/bin .

# Compile Bank System
echo "Compiling Bank System..."
javac -cp "lib/*" -d bank/bin bank/src/*.java
# javac -cp "bank/lib/*" -d bank/bin bank/src/*.java

# Compile Utility System
echo "Compiling Utility System..."
javac -cp "lib/*" -d utility/bin utility/src/*.java
# javac -cp "utility/lib/*" -d utility/bin utility/src/*.java

# Compile ATM Application
echo "Compiling ATM Application..."
javac -cp "lib/*" -d atm/bin atm/src/*.java
# javac -cp "atm/lib/*" -d atm/bin atm/src/*.java

echo "All projects compiled successfully."
