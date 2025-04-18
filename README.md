# ATM System - SE 317 Lab 6

This project implements a distributed ATM system for managing bank accounts (checking and savings) and paying utility bills, as part of SE 317 Lab 6. The system consists of an ATM client, a Bank Server, and a Utility Server, with SQLite databases for persistence.

## Prerequisites

To deploy and run the project, ensure you have the following:

- **Java**: Version 11 or higher (JDK installed).
- **SQLite JDBC Driver**: Download `sqlite-jdbc-3.42.0.jar` (or later) from Maven Repository or include it in your project.
- **Operating System**: Windows, macOS, or Linux.
- **Command-Line Interface**: For compiling and running the Java programs.
- **Network**: Localhost network for TCP socket communication (default port: 12345 for Bank Server, 12346 for Utility Server).
- **Optional**: IDE like IntelliJ IDEA or Eclipse for easier compilation and debugging.

## Project Structure

- `ATMApplication.java`: The ATM client interface, allowing users to log in, manage bank accounts, and pay utility bills.
- `BankServer.java`: The server handling bank account operations and interacting with `bank.db`. (Must be implemented separately or provided.)
- `UtilityServer.java`: The server managing utility accounts and bills, interacting with `utility.db`. (Must be implemented separately or provided.)
- `SubsystemClient.java`: Handles TCP socket communication between the ATM and servers. (Included in `ATMApplication.java` or separate file.)
- `Message.java`: Defines the message format for client-server communication. (Included in `ATMApplication.java` or separate file.)
- `bank.db`: SQLite database for bank data (users, accounts, transactions, utility mappings).
- `utility.db`: SQLite database for utility data (accounts, bills).
- `lib/`: Directory for the SQLite JDBC driver (`sqlite-jdbc-3.42.0.jar`).

## Setup Instructions

1. **Clone or Download the Project**:

   - If using a repository, clone it: `git clone <repository-url>`.
   - Alternatively, download and extract the project files to a directory (e.g., `atm-system/`).

2. **Place the SQLite JDBC Driver**:

   - Download `sqlite-jdbc-3.42.0.jar` and place it in the `lib/` directory of the project.
   - If `lib/` doesn’t exist, create it: `mkdir lib`.

3. **Set Up SQLite Databases**:

   - Create `bank.db` and `utility.db` in the project root directory using SQLite (e.g., via `sqlite3` command or a GUI like DB Browser for SQLite).

   - Initialize `bank.db` with the following schema (example):

     ```sql
     CREATE TABLE users (username TEXT PRIMARY KEY, password TEXT);
     CREATE TABLE accounts (account_id TEXT PRIMARY KEY, username TEXT, type TEXT, balance REAL, FOREIGN KEY(username) REFERENCES users(username));
     CREATE TABLE transactions (transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, account_id TEXT, type TEXT, amount REAL, date TEXT);
     CREATE TABLE utility_mappings (username TEXT, utility_account TEXT, FOREIGN KEY(username) REFERENCES users(username));
     ```

   - Initialize `utility.db` with the following schema (example):

     ```sql
     CREATE TABLE utility_accounts (account_id TEXT PRIMARY KEY, username TEXT, password TEXT);
     CREATE TABLE bills (bill_id INTEGER PRIMARY KEY AUTOINCREMENT, account_id TEXT, amount REAL, due_date TEXT, status TEXT);
     ```

   - Populate `bank.db` with sample data (e.g., a user with checking and savings accounts) and `utility.db` with a utility account.

4. **Verify Project Files**:

   - Ensure `ATMApplication.java`, `BankServer.java`, `UtilityServer.java`, `SubsystemClient.java`, and `Message.java` are in the project root or appropriate directories.
   - If `SubsystemClient` and `Message` are embedded in `ATMApplication.java` (as provided), no separate files are needed.

## Compilation

Compile the Java files using the command line or an IDE.

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

- Import the project into your IDE (e.g., IntelliJ IDEA, Eclipse).
- Add `sqlite-jdbc-3.42.0.jar` to the project’s library/dependencies.
- Build the project using the IDE’s build/run feature.

## Running the Project

The project requires running the Bank Server, Utility Server, and ATM client in sequence.

1. **Start the Utility Server**:

   - Run the Utility Server to listen for requests (e.g., on port 12346):

     ```bash
     java -cp ".;lib/sqlite-jdbc-3.42.0.jar" UtilityServer
     ```

   - Ensure `utility.db` is in the project root.

   - The server should initialize and wait for connections from the Bank Server.

2. **Start the Bank Server**:

   - Run the Bank Server to listen for ATM and Utility Server requests (e.g., on port 12345):

     ```bash
     java -cp ".;lib/sqlite-jdbc-3.42.0.jar" BankServer
     ```

   - Ensure `bank.db` is in the project root.

   - The server should connect to `utility.db` via TCP (port 12346) and wait for ATM connections.

3. **Run the ATM Client**:

   - Run the ATM client to connect to the Bank Server:

     ```bash
     java -cp ".;lib/sqlite-jdbc-3.42.0.jar" ATMApplication
     ```

   - The ATM will prompt for a username and password.

4. **Order of Execution**:

   - Start the Utility Server first, then the Bank Server, and finally the ATM client.
   - Ensure all servers are running before launching the ATM client.

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

## Troubleshooting

- **“ClassNotFoundException: org.sqlite.JDBC”**:
  - Ensure `sqlite-jdbc-3.42.0.jar` is in `lib/` and included in the classpath.
- **“Connection refused”**:
  - Verify the Bank Server is running on `localhost:12345` and the Utility Server on `localhost:12346`.
  - Start servers before the ATM client.
- **“No such table”**:
  - Check that `bank.db` and `utility.db` exist and have the correct schema.
  - Reinitialize databases if needed.
- **Invalid Input Errors**:
  - Enter valid numbers for amounts (e.g., `100.50`, not `abc`).
  - Use correct menu options (e.g., `1`, not `a`).
- **Server Not Responding**:
  - Ensure servers are running and ports are not blocked by a firewall.

## Notes

- The project assumes `BankServer.java` and `UtilityServer.java` are implemented to handle messages from `ATMApplication` and interact with `bank.db` and `utility.db` via JDBC.
- The `SubsystemClient` in `ATMApplication.java` uses TCP sockets to communicate with the Bank Server (port 12345).
- Database triggers or server logic enforce rules (e.g., $5000 daily deposit limit, $500 daily withdrawal limit for checking, $100 daily transfer limit for savings, no overdraft).
- For testing, capture screenshots of login, menus, operations (e.g., deposit, withdraw, bill payment), and error messages.
- Sample database files and server implementations are not included; create them based on the assignment requirements.

## Contact

For issues or questions, contact the project developer or refer to the SE 317 Lab 6 assignment details.

Good luck with your ATM System deployment!