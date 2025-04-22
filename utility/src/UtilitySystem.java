import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilitySystem {
	private static final int PORT = 8082;
	private static final String SYSTEM_ID = "UTIL";

	private static final String DB_URL = "jdbc:sqlite:utility.db";

	private Map<String, UtilityAccount> utilityAccounts;
	private Server server;

	public static void main(String[] args) throws Exception {
		UtilitySystem utilitySystem = new UtilitySystem();
		utilitySystem.start();
	}

	public UtilitySystem() {
		initializeDatabase();
		this.utilityAccounts = loadUtilityAccounts();
		this.server = new Server(PORT, SYSTEM_ID, this::handleMessage);
	}

	private void start() {
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
				case SIGNUP:
					handleSignup(message, response);
					break;
				case LOGIN:
					handleLogin(message, response);
					break;
				case PAY_BILL:
					handlePayBill(message, response);
					break;
				case VIEW_NEXT_BILL:
					handleViewBill(message, response);
					break;
				case VIEW_BILL_HISTORY:
					handleBillHistory(message, response);
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

	private void handleSignup(Message message, Message response) {
		String username = (String) message.getData("username");
		String password = (String) message.getData("password");

		try {
			UtilityAccount account = new UtilityAccount(username, password);
			utilityAccounts.put(account.getAccountNumber(), account);
			response.setMessageType(Message.Type.SUCCESS);
			response.addData("accountNumber", account.getAccountNumber());
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handleLogin(Message message, Message response) {
		String nameOrNumber = (String) message.getData("nameOrNumber");
		String password = (String) message.getData("password");

		try {
			UtilityAccount account = UtilityAccount.logIn(nameOrNumber, password);
			if (account != null) {
				utilityAccounts.put(account.getAccountNumber(), account);
				response.setMessageType(Message.Type.SUCCESS);
				response.addData("accountNumber", account.getAccountNumber());
			} else {
				response.setMessageType(Message.Type.ERROR);
				response.addData("Error", "Invalid username or password");
			}
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handlePayBill(Message message, Message response) {
		String accountId = (String) message.getData("accountId");
		double amount = (double) message.getData("amount");

		try {
			UtilityAccount account = utilityAccounts.get(accountId);
			if (account != null) {
				try {
					account.payBill(amount);
					response.setMessageType(Message.Type.SUCCESS);
				} catch (Exception e) {
					response.setMessageType(Message.Type.ERROR);
					response.addData("Error", e.getMessage());
				}
			} else {
				response.setMessageType(Message.Type.ERROR);
				response.addData("Error", "Account not found");
			}
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handleViewBill(Message message, Message response) {
		String accountId = (String) message.getData("accountId");

		try {
			UtilityAccount account = utilityAccounts.get(accountId);
			if (account != null) {
				Bill bill = account.getNextBill();
				if (bill != null) {
					response.setMessageType(Message.Type.SUCCESS);
					response.addData("billAmount", bill.getAmount());
					response.addData("billDueDate", bill.getDueDate());
				} else {
					response.setMessageType(Message.Type.ERROR);
					response.addData("Error", "No bills available");
				}
			} else {
				response.setMessageType(Message.Type.ERROR);
				response.addData("Error", "Account not found");
			}
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private void handleBillHistory(Message message, Message response) {
		String accountId = (String) message.getData("accountId");

		try {
			UtilityAccount account = utilityAccounts.get(accountId);
			if (account != null) {
				List<Bill> paidBills = account.getPaidBills();
				if (paidBills != null && !paidBills.isEmpty()) {
					response.setMessageType(Message.Type.SUCCESS);
					for (int i = 0; i < paidBills.size() && i < 3; i++) {
						Bill bill = paidBills.get(i);
						String billTag = String.format("bills[%d]", i);
						response.addData(billTag + ".id", bill.getBillId());
						response.addData(billTag + ".amount", bill.getAmount());
						response.addData(billTag + ".dueDate", bill.getDueDate());
						response.addData(billTag + ".paidDate", bill.getPaidDate());
					}
				} else {
					response.setMessageType(Message.Type.ERROR);
					response.addData("Error", "No paid bills available");
				}
			} else {
				response.setMessageType(Message.Type.ERROR);
				response.addData("Error", "Account not found");
			}
		} catch (Exception e) {
			response.setMessageType(Message.Type.ERROR);
			response.addData("Error", e.getMessage());
		}
	}

	private Map<String, UtilityAccount> loadUtilityAccounts() {
		Map<String, UtilityAccount> accounts = new HashMap<>();
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT * FROM accounts");
			while (rs.next()) {
				String accountNumber = rs.getString("account_number");
				String username = rs.getString("username");
				String password = rs.getString("password");
				UtilityAccount account = new UtilityAccount(accountNumber, username, password);
				account.loadBills(); // Load bills from database
				accounts.put(accountNumber, account);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return accounts;
	}

	public static void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {
			// Create accounts table
			stmt.execute("""
					CREATE TABLE IF NOT EXISTS accounts (
					    account_number TEXT PRIMARY KEY,
					    username TEXT UNIQUE NOT NULL,
					    password TEXT NOT NULL
					)
					""");

			// Create bills table
			stmt.execute("""
					CREATE TABLE IF NOT EXISTS bills (
					    bill_id INTEGER PRIMARY KEY AUTOINCREMENT,
					    account_number TEXT NOT NULL,
					    amount REAL NOT NULL,
					    due_date TEXT NOT NULL,
					    paid_date TEXT,
					    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
					)
					""");

			System.out.println("Database and tables created successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
