#!/bin/bash

# Navigate to parent directory
cd "$(dirname "$0")"

# Create logs directory if it doesn't exist
mkdir -p logs

# Start Bank System
echo "Starting Bank System..."
java -cp "bank/bin:rpc/rpc.jar:bank/lib/*" BankSystem > logs/bank.log 2>&1 &
BANK_PID=$!
echo "Bank System started with PID: $BANK_PID"

# Wait for Bank to initialize
echo "Waiting for Bank System to initialize..."
sleep 5

# Start Utility System
echo "Starting Utility System..."
java -cp "utility/bin:rpc/rpc.jar:utility/lib/*" UtilitySystem > logs/utility.log 2>&1 &
UTILITY_PID=$!
echo "Utility System started with PID: $UTILITY_PID"

# Wait for Utility to initialize
echo "Waiting for Utility System to initialize..."
sleep 5

# Start ATM Application in foreground
echo "Starting ATM Application..."
java -cp "atm/bin:rpc/rpc.jar:atm/lib/*" ATMApplication

echo "All systems started successfully."
echo "Bank System PID: $BANK_PID"
echo "Utility System PID: $UTILITY_PID"
echo "ATM Application has exited."
echo "To stop Bank and Utility systems, run: ./stop-system.sh"

# Create a file with PIDs for later cleanup
echo "$BANK_PID $UTILITY_PID" > running_pids.txt
