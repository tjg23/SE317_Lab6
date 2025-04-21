import java.sql.Connection;
import java.sql.DriverManager;
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

	private Server server;
	private Client client;

	public static void main(String[] args) throws Exception {
		BankSystem bankSystem = new BankSystem();
		bankSystem.start();
	}

	public BankSystem() {
		this.checkingAccounts = loadCheckingAccounts();
		this.savingAccounts = loadSavingAccounts();
		this.server = new Server(PORT, SYSTEM_ID, this::handleMessage);
		this.client = new Client();
		intitializeDatabase();
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
				case Message.Type.DEPOSIT:
					handleDeposit(message, response);
					break;
				case Message.Type.WITHDRAW:
					handleWithdraw(message, response);
					break;
				case Message.Type.TRANSFER:
					handleTransfer(message, response);
					break;
				case Message.Type.PAY_BILL:
					handlePayBill(message, response);
					break;
				case Message.Type.CHECK_BALANCE:
					handleCheckBalance(message, response);
					break;
				case Message.Type.LOGIN:
					handleLogin(message, response);
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
		String pin = (String) request.getData("pin");
		String name = (String) request.getData("name");

		User user = User.logIn(name, pin);
		if (user != null) {
			response.setMessageType(Message.Type.SUCCESS);
			response.addData("user", user);
		} else {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "Invalid login credentials.");
		}
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
		String fromAccountId = (String) request.getData("fromAccountId");
		String toAccountId = (String) request.getData("toAccountId");
		double amount = (double) request.getData("amount");

		BankAccount fromAccount = getAccount(fromAccountId);
		BankAccount toAccount = getAccount(toAccountId);
		if (fromAccount == null || toAccount == null) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", "One or both accounts not found: " + fromAccountId + ", " + toAccountId);
			return;
		}

		try {
			fromAccount.transfer(amount, toAccount);
			response.setMessageType(Message.Type.SUCCESS);
			response.addData("newBalance", fromAccount.getBalance());
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
		return new HashMap<>();
	}

	private Map<String, SavingAccount> loadSavingAccounts() {
		return new HashMap<>();
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
