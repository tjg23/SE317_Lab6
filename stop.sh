#!/bin/bash

# Navigate to parent directory
cd "$(dirname "$0")"

# Read PIDs from file
if [ -f running_pids.txt ]; then
    PIDS=$(cat running_pids.txt)
    for PID in $PIDS; do
        echo "Stopping process with PID: $PID"
        kill $PID 2>/dev/null || echo "Process $PID already stopped"
    done
    rm running_pids.txt
else
    echo "No running processes found"
    # Try to find and kill processes by class name
    BANK_PID=$(ps -ef | grep "BankSystem" | grep -v grep | awk '{print $2}')
    UTILITY_PID=$(ps -ef | grep "UtilitySystem" | grep -v grep | awk '{print $2}')
    
    if [ ! -z "$BANK_PID" ]; then
        echo "Stopping Bank System with PID: $BANK_PID"
        kill $BANK_PID
    fi
    
    if [ ! -z "$UTILITY_PID" ]; then
        echo "Stopping Utility System with PID: $UTILITY_PID"
        kill $UTILITY_PID
    fi
fi

echo "All systems stopped."
