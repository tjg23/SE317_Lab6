import java.time.LocalDate;
import java.util.Scanner;

public class ATMApplication {
	private Client client;
	private Scanner scanner;

	private String username; // Current logged-in user
	private String checkingAccountId;
	private String savingAccountId;
	private String utilityAccountId;

	private CheckingMenu checkingMenu = new CheckingMenu();
	private SavingsMenu savingsMenu = new SavingsMenu();
	private UtilityMenu utilityMenu = new UtilityMenu();

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
					checkingMenu.open();
					break;
				case 2:
					savingsMenu.open();
					break;
				case 3:
					utilityMenu.open();
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

	private class CheckingMenu {
		private void display() {
			System.out.println("\n=== Checking Account Menu ===");
			System.out.println("1. Check Balance");
			System.out.println("2. Deposit");
			System.out.println("3. Withdraw");
			System.out.println("4. Transfer to Savings");
			System.out.println("0. Back to Main Menu");
		}

		private int handleChoice(int choice) {
			switch (choice) {
				case 1:
					handleBalance();
					break;
				case 2:
					handleDeposit();
					break;
				case 3:
					handleWithdraw();
					break;
				case 4:
					handleTransfer();
					break;
				case 0:
					return 0; // Back to main menu
				default:
					System.out.println("Invalid choice. Please try again.");
			}
			return 1;
		}

		public void open() {
			boolean checkingRunning = true;
			while (checkingRunning) {
				display();
				int choice = readUserChoice();
				if (handleChoice(choice) == 0) {
					checkingRunning = false; // Close checking menu
				}
			}
		}

		private void handleBalance() {
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

		private void handleDeposit() {
			System.out.print("Enter deposit amount: $");
			String amountStr = scanner.nextLine();

			try {
				Double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					System.out.println("Amount must be positive.");
					return;
				}

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

		private void handleWithdraw() {
			System.out.print("Enter withdrawal amount: $");
			String amountStr = scanner.nextLine();

			try {
				Double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					System.out.println("Amount must be positive.");
					return;
				}

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

		private void handleTransfer() {
			String sourceAccountId = checkingAccountId;
			String targetAccountId = savingAccountId;

			System.out.println("Transferring from Checking to Savings...");
			handleAccountTransfer(sourceAccountId, targetAccountId);
		}
	}

	private class SavingsMenu {
		private void display() {
			System.out.println("\n=== Savings Account Menu ===");
			System.out.println("1. Check Balance");
			System.out.println("2. Deposit");
			System.out.println("3. Transfer to Checking");
			System.out.println("0. Back to Main Menu");
		}

		private int handleChoice(int choice) {
			switch (choice) {
				case 1:
					handleBalance();
					break;
				case 2:
					handleDeposit();
					break;
				case 3:
					handleTransfer();
					break;
				case 0:
					return 0; // Back to main menu
				default:
					System.out.println("Invalid choice. Please try again.");
			}
			return 1;
		}

		public void open() {
			boolean savingsRunning = true;
			while (savingsRunning) {
				display();
				int choice = readUserChoice();
				if (handleChoice(choice) == 0) {
					savingsRunning = false; // Close savings menu
				}
			}
		}

		private void handleBalance() {
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

		private void handleDeposit() {
			System.out.print("Enter deposit amount: $");
			String amountStr = scanner.nextLine();

			try {
				Double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					System.out.println("Amount must be positive.");
					return;
				}

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

		private void handleTransfer() {
			String sourceAccountId = savingAccountId;
			String targetAccountId = checkingAccountId;

			System.out.println("Transferring from Savings to Checking...");
			handleAccountTransfer(sourceAccountId, targetAccountId);
		}
	}

	private void handleAccountTransfer(String sourceAccountId, String targetAccountId) {
		System.out.print("Enter transfer amount to Checking: $");
		String amountStr = scanner.nextLine();

		try {
			Double amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				System.out.println("Amount must be positive.");
				return;
			}

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

	private class UtilityMenu {
		private void displayPaymentMenu() {
			System.out.println("\n=== Utility Payment Menu ===");
			System.out.println("1. View Bill History");
			System.out.println("2. View Next Bill");
			System.out.println("3. Pay Bill");
			System.out.println("0. Back to Main Menu");
		}

		private void displayWelcomeMenu() {
			System.out.println("\n=== Utility Welcome Menu ===");
			System.out.println("1. Login");
			System.out.println("2. Sign Up");
			System.out.println("0. Back to Main Menu");
		}

		private int handlePaymentChoice(int choice) {
			switch (choice) {
				case 1:
					handleBillHistory();
					break;
				case 2:
					handleViewBill();
					break;
				case 3:
					handlePayment();
					break;
				case 0:
					return 0; // Back to main menu
				default:
					System.out.println("Invalid choice. Please try again.");
			}
			return 1;
		}

		private int handleWelcomeChoice(int choice) {
			switch (choice) {
				case 1:
					handleLogin();
					break;
				case 2:
					handleSignup();
					break;
				case 0:
					return 0; // Back to main menu
				default:
					System.out.println("Invalid choice. Please try again.");
			}
			return 1;
		}

		public void open() {
			boolean loggedIn = false;
			while (!loggedIn) {
				displayWelcomeMenu();
				int choice = readUserChoice();
				int result = handleWelcomeChoice(choice);
				switch (result) {
					case 1:
						loggedIn = true;
						break;
					case 0:
						return; // Back to main menu
				}
			}

			boolean utilityRunning = true;
			while (utilityRunning) {
				displayPaymentMenu();
				int choice = readUserChoice();
				if (handlePaymentChoice(choice) == 0) {
					utilityRunning = false; // Close utility menu
				}
			}
		}

		private void handleSignup() {
			System.out.print("Choose account username: ");
			String username = scanner.nextLine();
			System.out.print("Choose account password: ");
			String password = scanner.nextLine();

			Message request = new Message("ATM", "UTIL", Message.Type.SIGNUP, null);
			request.addData("username", username);
			request.addData("password", password);

			try {
				Message response = client.sendMessage(request);
				if (response.getMessageType().equals(Message.Type.SUCCESS)) {
					utilityAccountId = (String) response.getData("accountNumber");
					System.out.println("Utility account created successfully!");
					System.out.println("Account Number: " + response.getData("accountNumber"));
				} else {
					System.out.println("Signup failed: " + response.getData("Error"));
				}
			} catch (Exception e) {
				System.out.println("Error communicating with utility company: " + e.getMessage());
			}
		}

		private void handleLogin() {
			System.out.print("Enter account number or username: ");
			String nameOrNumber = scanner.nextLine();
			System.out.print("Enter password: ");
			String password = scanner.nextLine();

			Message request = new Message("ATM", "UTIL", Message.Type.LOGIN, null);
			request.addData("nameOrNumber", nameOrNumber);
			request.addData("password", password);

			try {
				Message response = client.sendMessage(request);
				if (response.getMessageType().equals(Message.Type.SUCCESS)) {
					utilityAccountId = (String) response.getData("accountNumber");
					System.out.println("Login successful!");
					System.out.println("Account Number: " + response.getData("accountNumber"));
				} else {
					System.out.println("Login failed: " + response.getData("Error"));
				}
			} catch (Exception e) {
				System.out.println("Error communicating with utility company: " + e.getMessage());
			}
		}

		private void handleViewBill() {
			Message request = new Message("ATM", "UTIL", Message.Type.VIEW_NEXT_BILL, null);
			request.addData("accountId", utilityAccountId);

			try {
				Message response = client.sendMessage(request);
				if (response.getMessageType().equals(Message.Type.SUCCESS)) {
					System.out.format("Next Bill: %s\t(Due %tD)\n", response.getData("billAmount"),
							(LocalDate) response.getData("billDueDate"));
				} else {
					System.out.println("Error: " + response.getData("Error"));
				}
			} catch (Exception e) {
				System.out.println("Error communicating with utility company: " + e.getMessage());
			}
		}

		private void handleBillHistory() {
			Message request = new Message("ATM", "UTIL", Message.Type.VIEW_BILL_HISTORY, null);
			request.addData("accountId", utilityAccountId);

			try {
				Message response = client.sendMessage(request);
				if (response.getMessageType().equals(Message.Type.SUCCESS)) {
					System.out.println("Bill History: ");
					for (int i = 0; i < 3; i++) {
						String billTag = String.format("bills[%d]", i);
						String billId = (String) response.getData(billTag + ".id");
						Double billAmount = (Double) response.getData(billTag + ".amount");
						LocalDate billDueDate = (LocalDate) response.getData(billTag + ".dueDate");
						LocalDate billPaidDate = (LocalDate) response.getData(billTag + ".paidDate");
						if (billId != null) {
							System.out.format("Bill ID: %s\tAmount: $%.2f\tDue: %tD\tPaid: %tD\n", billId, billAmount, billDueDate,
									billPaidDate);
						} else {
							System.out.println("No more bills available.");
							break;
						}
					}
				} else {
					System.out.println("Error: " + response.getData("Error"));
				}
			} catch (Exception e) {
				System.out.println("Error communicating with utility company: " + e.getMessage());
			}
		}

		private void handlePayment() {
			System.out.print("Enter payment amount: $");
			String amountStr = scanner.nextLine();

			try {
				Double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					System.out.println("Amount must be positive.");
					return;
				}

				Message request = new Message("ATM", "BANK", Message.Type.PAY_BILL, null);
				request.addData("bankAccountId", checkingAccountId);
				request.addData("utilAccountId", utilityAccountId);
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
}