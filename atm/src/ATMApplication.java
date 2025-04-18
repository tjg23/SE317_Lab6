import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ATMApplication {
    private Client client;
    private Scanner scanner;
    private String username; // Current logged-in user

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
        while (!login()) {
            System.out.println("Login failed. Please try again.");
        }

        boolean running = true;
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

    // Handle user login
    private boolean login() {
        System.out.print("Enter username: ");
        username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Message request = new Message();
        request.setSenderID("ATM");
        request.setReceiverID("BANK");
        request.setMessageType("LOGIN");
        request.addData("username", username);
        request.addData("password", password);

        try {
            Message response = client.sendMessage(request);
            if ("LOGIN_SUCCESS".equals(response.getMessageType())) {
                System.out.println("Login successful. Welcome, " + username + "!");
                return true;
            } else {
                System.out.println("Login failed: " + response.getData("reason"));
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error communicating with bank: " + e.getMessage());
            return false;
        }
    }

    // Handle user logout
    private void logout() {
        Message request = new Message();
        request.setSenderID("ATM");
        request.setReceiverID("BANK");
        request.setMessageType("LOGOUT");
        request.addData("username", username);

        try {
            Message response = client.sendMessage(request);
            if ("LOGOUT_SUCCESS".equals(response.getMessageType())) {
                System.out.println("Logged out successfully.");
            } else {
                System.out.println("Logout failed: " + response.getData("reason"));
            }
        } catch (Exception e) {
            System.out.println("Error communicating with bank: " + e.getMessage());
        }
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
        Message request = new Message();
        request.setSenderID("ATM");
        request.setReceiverID("CHECKING");
        request.setMessageType("CHECK_BALANCE");
        request.addData("username", username);

        try {
            Message response = client.sendMessage(request);
            if ("BALANCE_RESPONSE".equals(response.getMessageType())) {
                System.out.println("Checking Balance: $" + response.getData("balance"));
            } else {
                System.out.println("Error: " + response.getData("reason"));
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
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            Message request = new Message();
            request.setSenderID("ATM");
            request.setReceiverID("CHECKING");
            request.setMessageType("DEPOSIT");
            request.addData("username", username);
            request.addData("amount", String.valueOf(amount));

            Message response = client.sendMessage(request);
            if ("DEPOSIT_ACCEPTED".equals(response.getMessageType())) {
                System.out.println("Deposit successful!");
                System.out.println("New balance: $" + response.getData("newBalance"));
            } else {
                System.out.println("Deposit failed: " + response.getData("reason"));
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
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            Message request = new Message();
            request.setSenderID("ATM");
            request.setReceiverID("CHECKING");
            request.setMessageType("WITHDRAW");
            request.addData("username", username);
            request.addData("amount", String.valueOf(amount));

            Message response = client.sendMessage(request);
            if ("WITHDRAW_ACCEPTED".equals(response.getMessageType())) {
                System.out.println("Withdrawal successful!");
                System.out.println("New balance: $" + response.getData("newBalance"));
            } else {
                System.out.println("Withdrawal failed: " + response.getData("reason"));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        } catch (Exception e) {
            System.out.println("Error communicating with bank: " + e.getMessage());
        }
    }

    // Check savings account balance
    private void handleSavingsBalance() {
        Message request = new Message();
        request.setSenderID("ATM");
        request.setReceiverID("BANK");
        request.setMessageType("CHECK_BALANCE");
        request.addData("username", username);

        try {
            Message response = client.sendMessage(request);
            if ("BALANCE_RESPONSE".equals(response.getMessageType())) {
                System.out.println("Savings Balance: $" + response.getData("balance"));
            } else {
                System.out.println("Error: " + response.getData("reason"));
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
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            Message request = new Message();
            request.setSenderID("ATM");
            request.setReceiverID("BANK");
            request.setMessageType("DEPOSIT");
            request.addData("username", username);
            request.addData("amount", String.valueOf(amount));

            Message response = client.sendMessage(request);
            if ("DEPOSIT_ACCEPTED".equals(response.getMessageType())) {
                System.out.println("Deposit successful!");
                System.out.println("New balance: $" + response.getData("newBalance"));
            } else {
                System.out.println("Deposit failed: " + response.getData("reason"));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        } catch (Exception e) {
            System.out.println("Error communicating with bank: " + e.getMessage());
        }
    }

    // Transfer from savings to checking
    private void handleSavingsTransfer() {
        System.out.print("Enter transfer amount to Checking: $");
        String amountStr = scanner.nextLine();

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            Message request = new Message();
            request.setSenderID("ATM");
            request.setReceiverID("BANK");
            request.setMessageType("TRANSFER_TO_CHECKING");
            request.addData("username", username);
            request.addData("amount", String.valueOf(amount));

            Message response = client.sendMessage(request);
            if ("TRANSFER_ACCEPTED".equals(response.getMessageType())) {
                System.out.println("Transfer successful!");
                System.out.println("New Savings balance: $" + response.getData("newSavingsBalance"));
                System.out.println("New Checking balance: $" + response.getData("newCheckingBalance"));
            } else {
                System.out.println("Transfer failed: " + response.getData("reason"));
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
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            Message request = new Message();
            request.setSenderID("ATM");
            request.setReceiverID("UTILITY");
            request.setMessageType("PAY_BILL");
            request.addData("username", username);
            request.addData("utilityAccount", utilityAccount);
            request.addData("amount", String.valueOf(amount));

            Message response = client.sendMessage(request);
            if ("PAYMENT_ACCEPTED".equals(response.getMessageType())) {
                System.out.println("Utility bill payment successful!");
                System.out.println("New Checking balance: $" + response.getData("newBalance"));
            } else {
                System.out.println("Payment failed: " + response.getData("reason"));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        } catch (Exception e) {
            System.out.println("Error communicating with utility company: " + e.getMessage());
        }
    }
}