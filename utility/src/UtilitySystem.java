import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilitySystem {
	private static final int PORT = 8081;
	private static final String SYSTEM_ID = "UTIL";

	private Map<String, UtilityAccount> utilityAccounts;
	private Server server;

	public static void main(String[] args) throws Exception {
		UtilitySystem utilitySystem = new UtilitySystem();
		utilitySystem.start();
	}

	public UtilitySystem() {
		this.utilityAccounts = loadUtilityAccounts();
		this.server = new Server(PORT, SYSTEM_ID, this::handleMessage);
	}

	private void start() {
		server.start();
	}

	private Message handleMessage(Message message) {
		String messageType = message.getMessageType();
		Message response = new Message();
		response.setReceiverId(message.getSenderId());
		response.setSenderId(SYSTEM_ID);
		response.setCorrelationId(message.getCorrelationId());

		try {
			switch (messageType) {
				case "CREATE_ACCOUNT":
					handleCreateAccount(message, response);
					break;
				case "LOGIN":
					handleLogin(message, response);
					break;
				case "PAY_BILL":
					handlePayBill(message, response);
					break;
				case "CHECK_BILL":
					handleCheckBill(message, response);
					break;
				case "BILL_HISTORY":
					handleBillHistory(message, response);
					break;
				default:
					response.setMessageType("ERROR");
					response.addData("Error", "Unsupported message type: " + messageType);
					break;
			}
		} catch (Exception e) {
			response.setMessageType("ERROR");
			response.addData("Error", e.getMessage());
		}

		return response;
	}

	private void handleCreateAccount(Message message, Message response) {
		String username = (String) message.getData("username");
		String password = (String) message.getData("password");

		try {
			UtilityAccount account = new UtilityAccount(username, password);
			utilityAccounts.put(account.getAccountNumber(), account);
			response.setMessageType("SUCCESS");
			response.addData("AccountNumber", account.getAccountNumber());
		} catch (Exception e) {
			response.setMessageType("ERROR");
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
				response.setMessageType("SUCCESS");
				response.addData("AccountNumber", account.getAccountNumber());
			} else {
				response.setMessageType("ERROR");
				response.addData("Error", "Invalid username or password");
			}
		} catch (Exception e) {
			response.setMessageType("ERROR");
			response.addData("Error", e.getMessage());
		}
	}

	private void handlePayBill(Message message, Message response) {
		String accountId = (String) message.getData("accountId");
		double amount = (double) message.getData("amount");

		try {
			UtilityAccount account = utilityAccounts.get(accountId);
			if (account != null) {
				account.payBill(amount);
				response.setMessageType("SUCCESS");
			} else {
				response.setMessageType("ERROR");
				response.addData("Error", "Account not found");
			}
		} catch (Exception e) {
			response.setMessageType("ERROR");
			response.addData("Error", e.getMessage());
		}
	}

	private void handleCheckBill(Message message, Message response) {
		String accountId = (String) message.getData("accountId");

		try {
			UtilityAccount account = utilityAccounts.get(accountId);
			if (account != null) {
				Bill bill = account.getNextBill();
				if (bill != null) {
					response.setMessageType("SUCCESS");
					response.addData("Bill", bill);
				} else {
					response.setMessageType("ERROR");
					response.addData("Error", "No bills available");
				}
			} else {
				response.setMessageType("ERROR");
				response.addData("Error", "Account not found");
			}
		} catch (Exception e) {
			response.setMessageType("ERROR");
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
					if (paidBills.size() > 3) {
						paidBills = paidBills.subList(0, 3);
					}
					response.setMessageType("SUCCESS");
					response.addData("PaidBills", paidBills);
				} else {
					response.setMessageType("ERROR");
					response.addData("Error", "No paid bills available");
				}
			} else {
				response.setMessageType("ERROR");
				response.addData("Error", "Account not found");
			}
		} catch (Exception e) {
			response.setMessageType("ERROR");
			response.addData("Error", e.getMessage());
		}
	}

	private Map<String, UtilityAccount> loadUtilityAccounts() {
		// Load utility accounts from the database or any other source
		// For simplicity, returning an empty map here
		return new HashMap<>();
	}
}
