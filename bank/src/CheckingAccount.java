import utility.Bill;

public class CheckingAccount extends BankAccount {
	private double dailyWithdrawals;
	private static final double DAILY_WITHDRAWAL_LIMIT = 500.0;

	public CheckingAccount(String accountNumber, double initialBalance) {
		super(accountNumber, initialBalance);
	}

	public CheckingAccount(double initialBalance) {
		super("Checking" + (++accountCounter), initialBalance);
	}

	public void withdraw(double amount) {
		if (amount <= 0) {
			System.out.println("Withdrawal amount must be positive.");
		} else if (amount > balance) {
			System.out.println("Insufficient funds. Cannot withdraw " + amount);
		} else if (dailyWithdrawals + amount > DAILY_WITHDRAWAL_LIMIT) {
			System.out.println("Daily withdrawal limit exceeded. Cannot withdraw " + amount);
		} else {
			balance -= amount;
			dailyWithdrawals += amount;
		}
	}

	public void transfer(double amount, SavingAccount savingAccount) {
		if (amount <= 0) {
			System.out.println("Transfer amount must be positive.");
		} else if (amount > balance) {
			System.out.println("Insufficient funds. Cannot transfer " + amount);
		} else {
			balance -= amount;
			savingAccount.deposit(amount);
			System.out.println("Transferred " + amount + " from Checking to Saving account.");
		}
	}

	public void resetDailyLimits() {
		dailyDeposits = 0;
		dailyWithdrawals = 0;
	}

	public void payUtilityBill(double amount, Bill utilityBill) {
		if (amount <= 0) {
			System.out.println("Payment amount must be positive.");
		} else if (amount > balance) {
			System.out.println("Insufficient funds. Cannot pay " + amount);
		} else if (utilityBill.getAmountDue() > amount) {
			System.out.println("Payment amount is less than the bill amount. Cannot pay " + amount);
		} else if (utilityBill.getPaidDate() != null) {
			System.out.println("Bill " + utilityBill.getBillNumber() + " has already been paid.");
		} else {
			balance -= amount;
			utilityBill.payBill(java.time.LocalDate.now().toString());
			System.out.println("Paid utility bill " + utilityBill.getBillNumber() + " of amount " + amount);
		}
	}
}
