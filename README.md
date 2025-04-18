# ATM System - SE 317 Lab 6

This project implements a distributed ATM system for managing bank accounts (checking and savings) and paying utility bills, as part of SE 317 Lab 6. The system consists of an ATM client, a Bank Server, and a Utility Server, with SQLite databases for persistence.

## Prerequisites

To deploy and run the project, ensure you have the following:

- **Java**: Version 17 (JDK installed).
- **Operating System**: Unix
- **Command-Line Interface**: For compiling and running the Java programs.
- **Network**: Localhost network for TCP socket communication (default port: 8080 for the ATM, 8081 for Bank Server, 8082 for Utility Server).
- **Optional**: IDE like Eclipse, VSCode, or IntelliJ IDEA for easier compilation and debugging.

## Project Structure

- `ATMApplication.java`: The ATM client interface, allowing users to log in, manage bank accounts, and pay utility bills.
- `Client.java`: Handles TCP socket communication between the ATM and servers. (Included in `ATMApplication.java` or separate file.)
- `Message.java`: Defines the message format for client-server communication. (Included in `ATMApplication.java` or separate file.)
- `bank.db`: SQLite database for bank data (users, accounts, transactions, utility mappings).
- `utility.db`: SQLite database for utility data (accounts, bills).

## Setup Instructions

1. **Clone or Download the Project**:

   - If using a repository, clone it: `git clone <https://github.com/tjg23/SE317_Lab6.git>`.

2. **Place the SQLite JDBC Driver**:

   - Download `sqlite-jdbc-3.42.0.jar` and place it in the `lib/` directory of the project.
   - If `lib/` doesn’t exist, create it: `mkdir lib`.

### Command-Line Compilation

1. Navigate to the project directory:

   ```bash
   cd atm-system
   ```

2. Compile all Java files, including the SQLite JDBC driver in the classpath:

   ```bash
   javac -cp ".;lib/sqlite-jdbc-3.42.0.jar" *.java
   ```

   - On Windows, use `;` as the classpath separator (as shown).
   - On macOS/Linux, use `:` (e.g., `javac -cp ".:lib/sqlite-jdbc-3.42.0.jar" *.java`).

### IDE Compilation

   ```bash
   sh build.sh
   ```

## Running the Project

   ```bash
   sh start.sh
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

- The project assumes `BankServer.java` and `UtilityServer.java` are implemented to handle messages from `ATMApplication` and interact with `bank.db` and `utility.db` via JDBC.
- The `Client` in `ATMApplication.java` uses TCP sockets to communicate with the Bank Server (port 8081).
- Database triggers or server logic enforce rules (e.g., $5000 daily deposit limit, $500 daily withdrawal limit for checking, $100 daily transfer limit for savings, no overdraft).
- For testing, capture screenshots of login, menus, operations (e.g., deposit, withdraw, bill payment), and error messages.
- Sample database files and server implementations are not included; create them based on the assignment requirements.

## Contact

For issues or questions, contact us at <sclover@iastate.edu> / <tjgorton@iastate.edu>