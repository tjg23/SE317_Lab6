# ATM System - SE 317 Lab 6

This project implements a distributed system for managing bank accounts (checking and savings) and paying utility bills, as part of SE 317 Lab 6. The system consists of an ATM client, a Bank Server, and a Utility Server, with SQLite databases for persistence.

## Prerequisites

To deploy and run the project, ensure you have the following:

- **Java**: Version 17 (JDK installed).
- **Operating System**: Unix
- **Command-Line Interface**: For compiling and running the Java programs.
- **Network**: Localhost network for TCP socket communication (default port: 8080 for the ATM, 8081 for Bank Server, 8082 for Utility Server).
- **Optional**: IDE like VSCode or IntelliJ IDEA for easier compilation and debugging.

## Project Structure

- `rpc/`: Common functionality for inter-process communication, to be compiled as a library for the other projects
	- `Message.java`: Defines the message format for client-server communication.
	- `Server.java`: Manages the TCP socket server to accept Messages from clients.
	- `Client.java`: Used to send Messages to a Server.
- `atm/`: Frontend application for system I/O via the terminal
	- `ATMApplication.java`: The ATM client interface, allowing users to log in, manage bank accounts, and pay utility bills.
- `bank/`: Bank system functionality, including checking & saving account management and TCP server
	- `BankAccount.java`: Base abstract class for common data & methods between checking & saving accounts
		- Extended by `CheckingAccount.java` and `SavingAccount.java` for differing implementation details
	- `BankSystem.java`: The Bank server, implementing account management via RPC
- `utility/`: Implementation of the Utility company system
	- `Bill.java`: Contains bill information, such as amount and due date
	- `UtilityAccount.java`: A customer's account with the utility company
	- `UtilitySystem.java`: Server for handling remote communication with the utility company.

## Setup Instructions

1. **Clone the Project**:

   - Clone the project repository: `git clone <https://github.com/tjg23/SE317_Lab6.git>`.

2. **Add the SQLite JDBC Driver**:

   - Download `sqlite-jdbc-3.42.0.jar` and place it in the `lib/` directory of both the `bank` and `utility` projects.

### Building & Running

On Unix systems (MacOS / Linux), you can simply use the provided bash scripts to compile and start the system.

```bash
# Compilation
sh build.sh

# Running
sh start.sh
```

#### Manual Compilation

1. Build the shared `rpc` library (as needed)

```bash
# Make the output directory if it doesn't exist
mkdir -p rpc/bin

javac -d rpc/bin rpc/src/*.java

jar cf lib/rpc.jar -C rpc/bin .
```

2. Build each main project component

```bash
javac -cp "lib/*" -d bank/bin bank/src/*.java
javac -cp "lib/*" -d utility/bin utility/src/*.java
javac -cp "lib/*" -d atm/bin atm/src/*.java
```

#### Running

```bash
# Start the bank server in the background
java -cp "bank/bin" BankSystem >_logs/bank.log 2>&1 &

# Start the utility company server in the background
java -cp "utility/bin" UtilitySystem >_logs/utility.log 2>&1 &

# Wait a few seconds, then start the ATM application in the foreground
java -cp "atm/bin" ATMApplication
```

## Usage Instructions

1. **Login**:

   - Enter a valid username and password (stored in `bank.db`’s `users` table).
   - Example: `username: user1`, `password: pass123`.
   - If login fails, re-enter credentials.

2. **Main Menu**:

   - Choose an option:
     - `1`: Checking Account (balance, deposit, withdraw).
     - `2`: Savings Account (balance, deposit, transfer to checking).
     - `3`: Utility Payment (pay a bill).
     - `0`: Exit (logout and close the application).
   - Enter the number and press Enter.

3. **Checking Account Menu**:

   - `1`: Check balance.
   - `2`: Deposit (up to $5000/day, e.g., enter `1000` for $1000).
   - `3`: Withdraw (up to $500/day, e.g., enter `200` for $200).
   - `0`: Return to main menu.

4. **Savings Account Menu**:

   - `1`: Check balance.
   - `2`: Deposit (up to $5000/day).
   - `3`: Transfer to checking (up to $100/day).
   - `0`: Return to main menu.

5. **Utility Payment**:

   - Enter the utility account number (from `utility.db`’s `utility_accounts` table).
   - Enter the payment amount (paid from checking account).
   - Example: `utility account: 123456`, `amount: 50`.

6. **Exit**:

   - Choose `0` from the main menu to logout and close the ATM.
   - Stop the Bank Server and Utility Server manually (e.g., Ctrl+C in their terminals).

## Notes

- The `Client` in `ATMApplication.java` uses TCP sockets to communicate with the Bank Server (port 8081).
- Database triggers or server logic enforce rules (e.g., $5000 daily deposit limit, $500 daily withdrawal limit for checking, $100 daily transfer limit for savings, no overdraft).

## Contact

For issues or questions, contact us at <sclover@iastate.edu> / <tjgorton@iastate.edu>