import java.util.HashMap;
import java.util.Map;

public class BankSystem {
	private static final int PORT = 8080;
	private static final String SYSTEM_ID = "BANK";

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
	}

	public void start() {
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
				case "DEPOSIT":
					handleDeposit(message, response);
					break;
				case "WITHDRAW":
					handleWithdraw(message, response);
					break;
				case "TRANSFER":
					handleTransfer(message, response);
					break;
				case "PAY_BILL":
					handlePayBill(message, response);
					break;
				case "CHECK_BALANCE":
					handleCheckBalance(message, response);
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
				response.setMessageType("ERROR");
				response.addData("Error", "Invalid account ID: " + accountId);
				return;
		}
		if (account == null) {
			response.setMessageType("ERROR");
			response.addData("Error", "Account not found: " + accountId);
			return;
		}
		try {
			account.deposit(amount);
			response.setMessageType("SUCCESS");
			response.addData("newBalance", account.getBalance());
		} catch (Exception e) {
			response.setMessageType("DEPOSIT_FAILED");
			response.addData("Error", e.getMessage());
		}
	}

	private void handleWithdraw(Message request, Message response) {
		String accountId = (String) request.getData("accountId");
		double amount = (double) request.getData("amount");

		if (accountId.charAt(0) != 'C') {
			response.setMessageType("WITHDRAW_REJECTED");
			response.addData("Reason", "Withdrawals are only allowed from Checking accounts.");
			return;
		}
		CheckingAccount account = checkingAccounts.get(accountId);
		if (account == null) {
			response.setMessageType("ERROR");
			response.addData("Error", "Account not found: " + accountId);
			return;
		}
		try {
			account.withdraw(amount);
			response.setMessageType("SUCCESS");
			response.addData("newBalance", account.getBalance());
		} catch (Exception e) {
			response.setMessageType("WITHDRAW_FAILED");
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
			response.setMessageType("ERROR");
			response.addData("Error", "One or both accounts not found: " + fromAccountId + ", " + toAccountId);
			return;
		}

		try {
			fromAccount.transfer(amount, toAccount);
			response.setMessageType("SUCCESS");
			response.addData("newBalance", fromAccount.getBalance());
		} catch (Exception e) {
			response.setMessageType("TRANSFER_FAILED");
			response.addData("Error", e.getMessage());
		}
	}

	private void handlePayBill(Message request, Message response) {
		String bankAccountId = (String) request.getData("bankAccountId");
		String utilAccountId = (String) request.getData("utilAccountId");
		double amount = (double) request.getData("amount");

		CheckingAccount account = checkingAccounts.get(bankAccountId);
		if (account == null) {
			response.setMessageType("ERROR");
			response.addData("Error", "Account not found: " + bankAccountId);
			return;
		}

		if (account.getBalance() < amount) {
			response.setMessageType("PAYMENT_REJECTED");
			response.addData("Reason", "Insufficient funds to pay bill.");
			return;
		}

		Message billRequest = new Message();
		billRequest.setSenderId(SYSTEM_ID);
		billRequest.setReceiverId("UTIL");
		billRequest.setMessageType("PAY_BILL");
		billRequest.addData("accountId", utilAccountId);
		billRequest.addData("amount", amount);

		try {
			Message billResponse = client.sendMessage(billRequest);

			if (!billResponse.getMessageType().equals("SUCCESS")) {
				response.setMessageType("PAYMENT_FAILED");
				response.addData("Error", "Failed to pay bill: " + billResponse.getData("Error"));
				response.addData("Reason", "Bill payment failed: " + billResponse.getData("Reason"));
				return;
			} else {
				account.withdraw(amount);
				response.setMessageType("SUCCESS");
				response.addData("newBalance", account.getBalance());
				response.addData("paymentDetails", billResponse.getData("paymentDetails"));
				return;
			}
		} catch (Exception e) {
			response.setMessageType("PAYMENT_FAILED");
			response.addData("Error", e.getMessage());
		}
	}

	private void handleCheckBalance(Message request, Message response) {
		String accountId = (String) request.getData("accountId");

		BankAccount account = getAccount(accountId);
		if (account == null) {
			response.setMessageType("ERROR");
			response.addData("Error", "Account not found: " + accountId);
			return;
		}
		response.setMessageType("SUCCESS");
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
}
