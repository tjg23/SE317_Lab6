#!/bin/bash

# Navigate to parent directory
cd "$(dirname "$0")"
ROOT_DIR=$(pwd)

# Create logs directory if it doesn't exist
mkdir -p _logs

# Start Bank System
echo "Starting Bank System..."
cd "$ROOT_DIR/bank"
java -cp "bin:$ROOT_DIR/lib/*" BankSystem >"$ROOT_DIR/_logs/bank.log" 2>&1 &
BANK_PID=$!
echo "Bank System started with PID: $BANK_PID"
cd "$ROOT_DIR"

# Wait for Bank to initialize
echo "Waiting for Bank System to initialize..."
sleep 1

# Start Utility System
echo "Starting Utility System..."
cd "$ROOT_DIR/utility"
java -cp "bin:$ROOT_DIR/lib/*" UtilitySystem >"$ROOT_DIR/_logs/utility.log" 2>&1 &
UTILITY_PID=$!
echo "Utility System started with PID: $UTILITY_PID"
cd "$ROOT_DIR"

# Wait for Utility to initialize
echo "Waiting for Utility System to initialize..."
sleep 1

# Start ATM Application in foreground
echo "Starting ATM Application..."
java -cp "atm/bin:lib/*" ATMApplication

echo "ATM Application has exited."
echo "To stop Bank and Utility systems, run: ./stop.sh"

# Create a file with PIDs for later cleanup
echo "$BANK_PID $UTILITY_PID" >_logs/pids.txt
