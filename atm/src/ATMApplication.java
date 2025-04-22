import java.util.Scanner;

public class ATMApplication {
	private Client client;
	private Scanner scanner;
	private String username; // Current logged-in user
	private String checkingAccountId;
	private String savingAccountId;

	public static void main(String[] args) {
		ATMApplication atm = new ATMApplication();
		atm.start();
	}

	public ATMApplication() {
		client = new Client();
		scanner = new Scanner(System.in);
	}

	public void start() {
		System.out.println("Welcome to ATM System");

		// Require login before showing the main menu
		int w;
		boolean running = true;

		while ((w = welcome()) != 1 && running) {
			switch (w) {
				case 0:
					System.out.println("Error. Please try again.");
					break;
				default:
					running = false;
					break;
			}
		}

		while (running) {
			displayMainMenu();
			int choice = readUserChoice();

			switch (choice) {
				case 1:
					handleCheckingAccount();
					break;
				case 2:
					handleSavingsAccount();
					break;
				case 3:
					handleUtilityPayment();
					break;
				case 0:
					logout();
					running = false;
					break;
				default:
					System.out.println("Invalid choice. Please try again.");
			}
		}

		System.out.println("Thank you for using ATM System. Goodbye!");
	}

	private int welcome() {
		System.out.println("\n=== Welcome ===");
		System.out.println("1. Login");
		System.out.println("2. Sign Up");
		System.out.print("Enter choice: ");

		int choice = readUserChoice();
		switch (choice) {
			case 1:
				return login();
			case 2:
				return signup();
			default:
				System.out.println("Invalid choice. Please try again.");
				return -1;
		}
	}

	// Handle user login
	private int login() {
		System.out.print("Enter name: ");
		username = scanner.nextLine();
		System.out.print("Enter PIN: ");
		String pin = scanner.nextLine();

		Message request = new Message("ATM", "BANK", Message.Type.LOGIN, null);
		request.addData("name", username);
		request.addData("pin", pin);

		try {
			Message response = client.sendMessage(request);
			if (response.getMessageType().equals(Message.Type.SUCCESS)) {
				checkingAccountId = (String) response.getData("checkingAccountId");
				savingAccountId = (String) response.getData("savingAccountId");
				System.out.println("Login successful. Welcome, " + username + "!");
				return 1;
			} else {
				System.out.println("Login failed: " + response.getData("Error"));
				return 0;
			}
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
			return 0;
		}
	}

	private int signup() {
		System.out.print("Enter name: ");
		username = scanner.nextLine();
		System.out.print("Enter PIN: ");
		String pin = scanner.nextLine();

		Message request = new Message("ATM", "BANK", Message.Type.SIGNUP, null);
		request.addData("name", username);
		request.addData("pin", pin);

		try {
			Message response = client.sendMessage(request);
			if (response.getMessageType().equals(Message.Type.SUCCESS)) {
				checkingAccountId = (String) response.getData("checkingAccountId");
				savingAccountId = (String) response.getData("savingAccountId");
				System.out.println("Signup successful. Welcome, " + username + "!");
				return 1;
			} else {
				System.out.println("Signup failed: " + response.getData("Error"));
				return 0;
			}
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
			return 0;
		}
	}

	// Handle user logout
	private void logout() {
		username = null;
	}

	// Display the main menu
	private void displayMainMenu() {
		System.out.println("\n=== ATM Menu ===");
		System.out.println("1. Checking Account");
		System.out.println("2. Savings Account");
		System.out.println("3. Utility Payment");
		System.out.println("0. Exit");
		System.out.print("Enter choice: ");
	}

	// Read user choice with validation
	private int readUserChoice() {
		try {
			return Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			return -1; // Invalid input
		}
	}

	// Handle checking account operations
	private void handleCheckingAccount() {
		boolean checkingRunning = true;
		while (checkingRunning) {
			System.out.println("\n=== Checking Account Menu ===");
			System.out.println("1. Check Balance");
			System.out.println("2. Deposit");
			System.out.println("3. Withdraw");
			System.out.println("4. Transfer to Savings");
			System.out.println("0. Back to Main Menu");
			System.out.print("Enter choice: ");

			int choice = readUserChoice();
			switch (choice) {
				case 1:
					handleCheckingBalance();
					break;
				case 2:
					handleCheckingDeposit();
					break;
				case 3:
					handleCheckingWithdraw();
					break;
				case 4:
					handleCheckingTransfer();
					break;
				case 0:
					checkingRunning = false;
					break;
				default:
					System.out.println("Invalid choice. Please try again.");
			}
		}
	}

	// Handle savings account operations
	private void handleSavingsAccount() {
		boolean savingsRunning = true;
		while (savingsRunning) {
			System.out.println("\n=== Savings Account Menu ===");
			System.out.println("1. Check Balance");
			System.out.println("2. Deposit");
			System.out.println("3. Transfer to Checking");
			System.out.println("0. Back to Main Menu");
			System.out.print("Enter choice: ");

			int choice = readUserChoice();
			switch (choice) {
				case 1:
					handleSavingsBalance();
					break;
				case 2:
					handleSavingsDeposit();
					break;
				case 3:
					handleSavingsTransfer();
					break;
				case 0:
					savingsRunning = false;
					break;
				default:
					System.out.println("Invalid choice. Please try again.");
			}
		}
	}

	// Check checking account balance
	private void handleCheckingBalance() {
		// TODO: fix message data
		Message request = new Message("ATM", "BANK", Message.Type.VIEW_BALANCE, null);
		request.addData("accountId", checkingAccountId);

		try {
			Message response = client.sendMessage(request);
			if (response.getMessageType().equals(Message.Type.SUCCESS)) {
				System.out.println("Checking Balance: $" + response.getData("balance"));
			} else {
				System.out.println("Error: " + response.getData("Error"));
			}
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
		}
	}

	// Deposit to checking account
	private void handleCheckingDeposit() {
		System.out.print("Enter deposit amount: $");
		String amountStr = scanner.nextLine();

		try {
			Double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				System.out.println("Amount must be positive.");
				return;
			}

			// TODO: fix message data
			Message request = new Message("ATM", "BANK", Message.Type.DEPOSIT, null);
			request.addData("accountId", checkingAccountId);
			request.addData("amount", amount);

			Message response = client.sendMessage(request);
			if (response.getMessageType().equals(Message.Type.SUCCESS)) {
				System.out.println("Deposit successful!");
				System.out.println("New balance: $" + response.getData("newBalance"));
			} else {
				System.out.println("Deposit failed: " + response.getData("Error"));
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid amount format.");
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
		}
	}

	// Withdraw from checking account
	private void handleCheckingWithdraw() {
		System.out.print("Enter withdrawal amount: $");
		String amountStr = scanner.nextLine();

		try {
			Double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				System.out.println("Amount must be positive.");
				return;
			}

			// TODO: fix message data
			Message request = new Message("ATM", "BANK", Message.Type.WITHDRAW, null);
			request.addData("accountId", checkingAccountId);
			request.addData("amount", amount);

			Message response = client.sendMessage(request);
			switch (response.getMessageType()) {
				case SUCCESS:
					System.out.println("Withdrawal successful!");
					System.out.println("New balance: $" + response.getData("newBalance"));
					break;
				case DECLINED:
					System.out.println("Withdrawal declined: " + response.getData("Reason"));
					break;
				case ERROR:
					System.out.println("Withdrawal failed: " + response.getData("Error"));
					break;
				default:
					System.out.println("Unknown response type.");
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid amount format.");
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
		}
	}

	// Check savings account balance
	private void handleSavingsBalance() {
		Message request = new Message("ATM", "BANK", Message.Type.VIEW_BALANCE, null);
		request.addData("accountId", savingAccountId);

		try {
			Message response = client.sendMessage(request);
			if (response.getMessageType().equals(Message.Type.SUCCESS)) {
				System.out.println("Savings Balance: $" + response.getData("balance"));
			} else {
				System.out.println("Error: " + response.getData("Error"));
			}
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
		}
	}

	// Deposit to savings account
	private void handleSavingsDeposit() {
		System.out.print("Enter deposit amount: $");
		String amountStr = scanner.nextLine();

		try {
			Double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				System.out.println("Amount must be positive.");
				return;
			}

			// TODO: fix message data
			Message request = new Message("ATM", "BANK", Message.Type.DEPOSIT, null);
			request.addData("accountId", savingAccountId);
			request.addData("amount", amount);

			Message response = client.sendMessage(request);
			if (response.getMessageType().equals(Message.Type.SUCCESS)) {
				System.out.println("Deposit successful!");
				System.out.println("New balance: $" + response.getData("newBalance"));
			} else {
				System.out.println("Deposit failed: " + response.getData("Error"));
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid amount format.");
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
		}
	}

	private void handleSavingsTransfer() {
		String sourceAccountId = savingAccountId;
		String targetAccountId = checkingAccountId;

		System.out.println("Transferring from Savings to Checking...");
		handleTransfer(sourceAccountId, targetAccountId);
	}

	private void handleCheckingTransfer() {
		String sourceAccountId = checkingAccountId;
		String targetAccountId = savingAccountId;

		System.out.println("Transferring from Checking to Savings...");
		handleTransfer(sourceAccountId, targetAccountId);
	}

	// Transfer from savings to checking
	private void handleTransfer(String sourceAccountId, String targetAccountId) {
		System.out.print("Enter transfer amount to Checking: $");
		String amountStr = scanner.nextLine();

		try {
			Double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				System.out.println("Amount must be positive.");
				return;
			}

			// TODO: fix message data
			Message request = new Message("ATM", "BANK", Message.Type.TRANSFER, null);
			request.addData("sourceAccountId", sourceAccountId);
			request.addData("targetAccountId", targetAccountId);
			request.addData("amount", amount);

			Message response = client.sendMessage(request);
			if (response.getMessageType().equals(Message.Type.SUCCESS)) {
				System.out.println("Transfer successful!");
				System.out.println("New Source balance: $" + response.getData("newSourceBalance"));
				System.out.println("New Destination balance: $" + response.getData("newTargetBalance"));
			} else {
				System.out.println("Transfer failed: " + response.getData("Error"));
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid amount format.");
		} catch (Exception e) {
			System.out.println("Error communicating with bank: " + e.getMessage());
		}
	}

	// Handle utility bill payment
	private void handleUtilityPayment() {
		System.out.print("Enter utility account number: ");
		String utilityAccount = scanner.nextLine();
		System.out.print("Enter payment amount: $");
		String amountStr = scanner.nextLine();

		try {
			Double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				System.out.println("Amount must be positive.");
				return;
			}

			// TODO: fix message data
			Message request = new Message("ATM", "BANK", Message.Type.PAY_BILL, null);
			request.addData("bankAccountId", checkingAccountId);
			request.addData("utilAccountId", utilityAccount);
			request.addData("amount", amount);

			Message response = client.sendMessage(request);
			switch (response.getMessageType()) {
				case SUCCESS:
					System.out.println("Payment successful!");
					System.out.println("New Checking balance: $" + response.getData("newBalance"));
					break;
				case DECLINED:
					System.out.println("Payment declined: " + response.getData("Reason"));
					break;
				case ERROR:
					System.out.println("Payment failed: " + response.getData("Error"));
					break;
				default:
					System.out.println("Unknown response type.");
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid amount format.");
		} catch (Exception e) {
			System.out.println("Error communicating with utility company: " + e.getMessage());
		}
	}
}