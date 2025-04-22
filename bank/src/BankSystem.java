import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class BankSystem {
	private static final int PORT = 8081;
	private static final String SYSTEM_ID = "BANK";
	private static final String DB_URL = "jdbc:sqlite:bank.db";

	private Map<String, CheckingAccount> checkingAccounts;
	private Map<String, SavingAccount> savingAccounts;
	private Map<String, User> users;

	private Server server;
	private Client client;

	public static void main(String[] args) throws Exception {
		BankSystem bankSystem = new BankSystem();
		bankSystem.start();
	}

	public BankSystem() {
		intitializeDatabase();
		this.checkingAccounts = loadCheckingAccounts();
		this.savingAccounts = loadSavingAccounts();
		this.users = loadUsers();
		this.server = new Server(PORT, SYSTEM_ID, this::handleMessage);
		this.client = new Client();
	}

	public void start() {
		server.start();
	}

	private Message handleMessage(Message message) {
		Message.Type messageType = message.getMessageType();
		Message response = new Message();
		response.setReceiverId(message.getSenderId());
		response.setSenderId(SYSTEM_ID);
		response.setCorrelationId(message.getCorrelationId());

		try {
			switch (messageType) {
				case DEPOSIT:
					handleDeposit(message, response);
					break;
				case WITHDRAW:
					handleWithdraw(message, response);
					break;
				case TRANSFER:
					handleTransfer(message, response);
					break;
				case PAY_BILL:
					handlePayBill(message, response);
					break;
				case VIEW_BALANCE:
					handleCheckBalance(message, response);
					break;
				case LOGIN:
					handleLogin(message, response);
					break;
				case SIGNUP:
					handleSignup(message, response);
					break;
				default:
					response.setMessageType(Message.Type.ERROR);
					response.addData("Error", "Unsupported message type: " + messageType);
					break;
			}
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}

		return response;
	}

	private void handleLogin(Message request, Message response) {
		String name = (String) request.getData("name");
		String pin = (String) request.getData("pin");

		User user = users.get(pin);
		if (user != null && user.getName().equals(name)) {
			response.setMessageType(Message.Type.SUCCESS);
			response.addData("checkingAccountId", user.getCheckingAccount().getAccountNumber());
			response.addData("savingAccountId", user.getSavingAccount().getAccountNumber());
		} else {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "Invalid login credentials.");
		}
	}

	private void handleSignup(Message request, Message response) {
		String name = (String) request.getData("name");
		String pin = (String) request.getData("pin");

		if (users.containsKey(pin)) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "PIN is taken");
			return;
		}

		CheckingAccount checkingAccount = new CheckingAccount(0);
		checkingAccount.saveAccount("Checking");
		SavingAccount savingAccount = new SavingAccount(0);
		savingAccount.saveAccount("Saving");
		User user = new User(name, pin, checkingAccount, savingAccount);
		user.saveUser();
		users.put(pin, user);
		checkingAccounts.put(checkingAccount.getAccountNumber(), checkingAccount);
		savingAccounts.put(savingAccount.getAccountNumber(), savingAccount);

		response.setMessageType(Message.Type.SUCCESS);
		response.addData("checkingAccountId", user.getCheckingAccount().getAccountNumber());
		response.addData("savingAccountId", user.getSavingAccount().getAccountNumber());
	}

	private void handleDeposit(Message request, Message response) {
		String accountId = (String) request.getData("accountId");
		double amount = (double) request.getData("amount");

		BankAccount account;
		switch (accountId.charAt(0)) {
			case 'C':
				account = checkingAccounts.get(accountId);
				break;
			case 'S':
				account = savingAccounts.get(accountId);
				break;
			default:
				response.setMessageType(Message.Type.ERROR);
				response.addData("Error", "Invalid account ID: " + accountId);
				return;
		}
		if (account == null) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "Account not found: " + accountId);
			return;
		}
		try {
			account.deposit(amount);
			response.setMessageType(Message.Type.SUCCESS);
			response.addData("newBalance", account.getBalance());
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handleWithdraw(Message request, Message response) {
		String accountId = (String) request.getData("accountId");
		double amount = (double) request.getData("amount");

		if (accountId.charAt(0) != 'C') {
			response.setMessageType(Message.Type.DECLINED);
			response.addData("Reason", "Withdrawals are only allowed from Checking accounts.");
			return;
		}
		CheckingAccount account = checkingAccounts.get(accountId);
		if (account == null) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "Account not found: " + accountId);
			return;
		}
		try {
			account.withdraw(amount);
			response.setMessageType(Message.Type.SUCCESS);
			response.addData("newBalance", account.getBalance());
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handleTransfer(Message request, Message response) {
		String sourceAccountId = (String) request.getData("sourceAccountId");
		String targetAccountId = (String) request.getData("targetAccountId");
		double amount = (double) request.getData("amount");

		BankAccount sourceAccount = getAccount(sourceAccountId);
		BankAccount targetAccount = getAccount(targetAccountId);
		if (sourceAccount == null || targetAccount == null) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "One or both accounts not found: " + sourceAccountId + ", " + targetAccountId);
			return;
		}

		try {
			sourceAccount.transfer(amount, targetAccount);
			response.setMessageType(Message.Type.SUCCESS);
			response.addData("newSourceBalance", sourceAccount.getBalance());
			response.addData("newTargetBalance", targetAccount.getBalance());
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handlePayBill(Message request, Message response) {
		String bankAccountId = (String) request.getData("bankAccountId");
		String utilAccountId = (String) request.getData("utilAccountId");
		double amount = (double) request.getData("amount");

		CheckingAccount account = checkingAccounts.get(bankAccountId);
		if (account == null) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "Account not found: " + bankAccountId);
			return;
		}

		if (account.getBalance() < amount) {
			response.setMessageType(Message.Type.DECLINED);
			response.addData("Reason", "Insufficient funds to pay bill.");
			return;
		}

		Message billRequest = new Message();
		billRequest.setSenderId(SYSTEM_ID);
		billRequest.setReceiverId("UTIL");
		billRequest.setMessageType(Message.Type.PAY_BILL);
		billRequest.addData("accountId", utilAccountId);
		billRequest.addData("amount", amount);

		try {
			Message billResponse = client.sendMessage(billRequest);

			if (!billResponse.getMessageType().equals(Message.Type.SUCCESS)) {
				response.setMessageType(Message.Type.ERROR);
				response.addData("Error", "Failed to pay bill: " + billResponse.getData("Error"));
				return;
			} else {
				account.withdraw(amount);
				response.setMessageType(Message.Type.SUCCESS);
				response.addData("newBalance", account.getBalance());
				response.addData("paymentDetails", billResponse.getData("paymentDetails"));
				return;
			}
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handleCheckBalance(Message request, Message response) {
		String accountId = (String) request.getData("accountId");

		BankAccount account = getAccount(accountId);
		if (account == null) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "Account not found: " + accountId);
			return;
		}
		response.setMessageType(Message.Type.SUCCESS);
		response.addData("balance", account.getBalance());
	}

	private Map<String, CheckingAccount> loadCheckingAccounts() {
		Map<String, CheckingAccount> accounts = new HashMap<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {
			String sql = "SELECT * FROM accounts WHERE accountType = 'Checking'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String accountNumber = rs.getString("accountNumber");
				double balance = rs.getDouble("balance");
				accounts.put(accountNumber, new CheckingAccount(accountNumber, balance));
			}
		} catch (SQLException e) {
			System.out.println("Error loading checking accounts: " + e.getMessage());
		}
		return accounts;
	}

	private Map<String, SavingAccount> loadSavingAccounts() {
		Map<String, SavingAccount> accounts = new HashMap<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {
			String sql = "SELECT * FROM accounts WHERE accountType = 'Saving'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String accountNumber = rs.getString("accountNumber");
				double balance = rs.getDouble("balance");
				accounts.put(accountNumber, new SavingAccount(accountNumber, balance));
			}
		} catch (SQLException e) {
			System.out.println("Error loading checking accounts: " + e.getMessage());
		}
		return accounts;
	}

	private Map<String, User> loadUsers() {
		Map<String, User> users = new HashMap<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {
			String sql = "SELECT * FROM users";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String pin = rs.getString("pin");
				String name = rs.getString("name");
				String checkingAccountNum = rs.getString("checkingAccount");
				CheckingAccount checkingAccount = checkingAccounts.get(checkingAccountNum);
				String savingAccountNum = rs.getString("savingAccount");
				SavingAccount savingAccount = savingAccounts.get(savingAccountNum);
				users.put(pin, new User(name, pin, checkingAccount, savingAccount));
			}
		} catch (SQLException e) {
			System.out.println("Error loading users: " + e.getMessage());
		}
		return users;
	}

	private BankAccount getAccount(String accountId) {
		if (checkingAccounts.containsKey(accountId)) {
			return checkingAccounts.get(accountId);
		} else if (savingAccounts.containsKey(accountId)) {
			return savingAccounts.get(accountId);
		} else {
			return null;
		}
	}

	private void intitializeDatabase() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {
			String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
					"accountNumber TEXT PRIMARY KEY, " +
					"balance REAL, " +
					"dailyWithdrawals REAL, " +
					"dailyTransfers REAL, " +
					"dailyDeposits REAL, " +
					"accountType TEXT)";
			stmt.execute(sql);
			String sql2 = "CREATE TABLE IF NOT EXISTS users (" +
					"pin TEXT PRIMARY KEY, " +
					"name TEXT, " +
					"checkingAccount TEXT, " +
					"savingAccount TEXT)";
			stmt.execute(sql2);
		} catch (SQLException e) {
			System.out.println("Database initialization error: " + e.getMessage());
		}
	}
}
