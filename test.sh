#!/bin/bash

# Navigate to the script directory (root directory)
cd "$(dirname "$0")"
ROOT_DIR=$(pwd)

echo "=== Running Bank Tests ==="

# Change to the bank directory for test execution
cd "$ROOT_DIR/bank" || {
	echo "Error: bank directory not found"
	exit 1
}

# Identify test files
TEST_FILES=$(find test -name "*Test.java" 2>/dev/null)

if [ -z "$TEST_FILES" ]; then
	echo "No test files found in bank/test directory"
	exit 1
fi

# Compile tests if needed
echo "Compiling test files..."
javac -cp "bin:$ROOT_DIR/_lib/*:test" $TEST_FILES -d bin

# Run each test
echo "Running tests..."
for test in $TEST_FILES; do
	# Extract class name without .java extension and test/ prefix
	TEST_CLASS=$(echo $test | sed 's/\.java$//' | sed 's/test\///g')
	echo "Running test: $TEST_CLASS"
	java -cp "bin:$ROOT_DIR/_lib/*" org.junit.runner.JUnitCore $TEST_CLASS
done

echo "=== Bank Tests Complete ==="

# Return to the root directory
cd "$ROOT_DIR"
